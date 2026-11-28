package io.github.kizio806.gearprogress.application.service;

import io.github.kizio806.gearprogress.application.config.spec.TimersConfigSpec;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing timer states, time calculations, and automated tier unlocking.
 *
 * <p><b>Pacing Engine:</b> Calculates remaining durations against server launch epochs, tracks
 * administrative pause states, and evaluates progression when background timer tasks trigger.
 *
 * <p><b>Thread Safety:</b> Internal state maps are thread-safe ({@link ConcurrentHashMap}),
 * ensuring non-blocking queries from command executors and placeholder expansions while background
 * evaluations run asynchronously.
 */
public final class TimerService {

  private final ProgressionService progressionService;
  private final EquipmentRegistry registry;
  private final TimePort timePort;
  private final LifecycleReporterPort logger;

  private Instant serverStartEpoch;
  private final Map<TierKey, Boolean> pausedStates = new ConcurrentHashMap<>();
  private final Map<TierKey, Long> accumulatedSeconds = new ConcurrentHashMap<>();

  public TimerService(
      ProgressionService progressionService,
      EquipmentRegistry registry,
      TimePort timePort,
      LifecycleReporterPort logger) {
    this.progressionService =
        Objects.requireNonNull(progressionService, "progressionService must not be null");
    this.registry = Objects.requireNonNull(registry, "registry must not be null");
    this.timePort = Objects.requireNonNull(timePort, "timePort must not be null");
    this.logger = Objects.requireNonNull(logger, "logger must not be null");
    this.serverStartEpoch = timePort.now();
  }

  /**
   * Loads timer states from configuration.
   *
   * @param spec timers spec
   */
  public void loadFromSpec(TimersConfigSpec spec) {
    Objects.requireNonNull(spec, "spec must not be null");
    this.serverStartEpoch = spec.serverStartEpoch();
    this.pausedStates.clear();
    this.accumulatedSeconds.clear();

    for (Map.Entry<String, Boolean> entry : spec.pausedStates().entrySet()) {
      try {
        pausedStates.put(TierKey.parse(entry.getKey()), entry.getValue());
      } catch (IllegalArgumentException ex) {
        logger.warning("Ignoring invalid timer key in paused state: " + entry.getKey());
      }
    }
    for (Map.Entry<String, Long> entry : spec.accumulatedSeconds().entrySet()) {
      try {
        accumulatedSeconds.put(TierKey.parse(entry.getKey()), entry.getValue());
      } catch (IllegalArgumentException ex) {
        logger.warning("Ignoring invalid timer key in accumulated seconds: " + entry.getKey());
      }
    }
  }

  /**
   * Evaluates all timer and date progression conditions against current time. Unlocks any tiers
   * whose condition has been satisfied.
   */
  public CompletableFuture<Void> evaluateProgression() {
    Instant now = timePort.now();
    java.util.List<CompletableFuture<?>> futures = new java.util.ArrayList<>();

    for (EquipmentDefinition def : registry.all().values()) {
      TierKey key = def.key();
      if (progressionService.isUnlocked(key)) {
        continue;
      }

      UnlockCondition condition = def.unlockCondition();
      if (condition instanceof UnlockCondition.Date dateCond) {
        if (!now.isBefore(dateCond.targetDate())) {
          logger.info(
              "Unlocking tier " + key + " (reached target date " + dateCond.targetDate() + ")");
          futures.add(
              progressionService
                  .unlock(key, UnlockMethod.DATE, "DATE_SCHEDULE")
                  .whenComplete(
                      (unlocked, ex) -> {
                        if (ex != null) {
                          logger.severe("Failed to unlock tier " + key + " via date schedule", ex);
                        }
                      }));
        }
      } else if (condition instanceof UnlockCondition.Timer timerCond) {
        if (isPaused(key)) {
          continue;
        }

        Duration elapsed = calculateElapsed(key, now);
        if (elapsed.compareTo(timerCond.delay()) >= 0) {
          logger.info("Unlocking tier " + key + " (timer expired after " + timerCond.delay() + ")");
          futures.add(
              progressionService
                  .unlock(key, UnlockMethod.TIMER, "TIMER_SCHEDULE")
                  .whenComplete(
                      (unlocked, ex) -> {
                        if (ex != null) {
                          logger.severe("Failed to unlock tier " + key + " via timer schedule", ex);
                        }
                      }));
        }
      }
    }
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
  }

  /**
   * Calculates the remaining time before a tier unlocks.
   *
   * @param key the tier key
   * @return optional time remaining (empty if tier is already unlocked, or has no timer/date
   *     condition)
   */
  public Optional<TimeRemaining> timeRemaining(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    if (progressionService.isUnlocked(key)) {
      return Optional.empty();
    }

    Optional<EquipmentDefinition> defOpt = registry.find(key);
    if (defOpt.isEmpty()) {
      return Optional.empty();
    }

    UnlockCondition condition = defOpt.get().unlockCondition();
    Instant now = timePort.now();

    if (condition instanceof UnlockCondition.Date dateCond) {
      return Optional.of(TimeRemaining.until(dateCond.targetDate(), now));
    } else if (condition instanceof UnlockCondition.Timer timerCond) {
      Duration elapsed = calculateElapsed(key, now);
      Duration remaining = timerCond.delay().minus(elapsed);
      if (remaining.isNegative()) {
        remaining = Duration.ZERO;
      }
      return Optional.of(new TimeRemaining(remaining, remaining.isZero()));
    }

    return Optional.empty();
  }

  private Duration calculateElapsed(TierKey key, Instant now) {
    long accumulated = accumulatedSeconds.getOrDefault(key, 0L);
    if (now.isBefore(serverStartEpoch)) {
      return Duration.ofSeconds(accumulated);
    }
    long baseSeconds = Duration.between(serverStartEpoch, now).toSeconds();
    return Duration.ofSeconds(Math.addExact(baseSeconds, accumulated));
  }

  /**
   * Checks whether a tier's timer is paused.
   *
   * @param key tier key
   * @return true if paused
   */
  public boolean isPaused(TierKey key) {
    return pausedStates.getOrDefault(key, false);
  }

  /**
   * Pauses a tier's timer.
   *
   * @param key tier key
   */
  public void pause(TierKey key) {
    pausedStates.put(key, true);
  }

  /**
   * Resumes a tier's timer.
   *
   * @param key tier key
   */
  public void resume(TierKey key) {
    pausedStates.put(key, false);
  }

  /**
   * Adds duration to a tier's accumulated elapsed time.
   *
   * @param key tier key
   * @param duration duration to add
   */
  public void addElapsed(TierKey key, Duration duration) {
    Objects.requireNonNull(duration, "duration must not be null");
    long current = accumulatedSeconds.getOrDefault(key, 0L);
    accumulatedSeconds.put(key, Math.addExact(current, duration.toSeconds()));
  }

  /**
   * Sets the accumulated elapsed time for a tier.
   *
   * @param key tier key
   * @param duration duration
   */
  public void setElapsed(TierKey key, Duration duration) {
    Objects.requireNonNull(duration, "duration must not be null");
    accumulatedSeconds.put(key, duration.toSeconds());
  }

  /**
   * Resets timer progress for a tier.
   *
   * @param key tier key
   */
  public void resetTimer(TierKey key) {
    pausedStates.remove(key);
    accumulatedSeconds.remove(key);
  }

  public Instant serverStartEpoch() {
    return serverStartEpoch;
  }
}
