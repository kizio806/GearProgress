package io.github.kizio806.gearprogress.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.application.repository.InMemoryProgressionRepository;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimerServiceTest {

  private EquipmentRegistry registry;
  private ProgressionService progressionService;
  private AtomicReference<Instant> clock;
  private TimerService timerService;

  @BeforeEach
  void setUp() {
    registry = new EquipmentRegistry();
    InMemoryProgressionRepository repo = new InMemoryProgressionRepository();
    clock = new AtomicReference<>(Instant.parse("2026-11-20T12:00:00Z"));
    TimePort timePort = clock::get;

    EventPublisherPort dummyPublisher =
        new EventPublisherPort() {
          @Override
          public void publishUnlock(TierKey key, UnlockMethod method, String source) {}

          @Override
          public void publishLock(TierKey key, String source) {}

          @Override
          public void publishEnchantmentAllowed(TierKey key, boolean allow, String source) {}
        };

    LifecycleReporterPort dummyReporter =
        new LifecycleReporterPort() {
          @Override
          public void info(String message) {}

          @Override
          public void warning(String message) {}

          @Override
          public void severe(String message, Throwable throwable) {}
        };

    progressionService = new ProgressionService(registry, repo, dummyPublisher, timePort);
    timerService = new TimerService(progressionService, registry, timePort, dummyReporter);
  }

  @Test
  void testTimerProgression() {
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    registry.register(
        new EquipmentDefinition(
            diamond,
            UnlockCondition.timer(Duration.ofDays(7)),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden()));

    progressionService.initialize().join();
    assertFalse(progressionService.isUnlocked(diamond));

    // Check time remaining at start: 7 days
    var remaining = timerService.timeRemaining(diamond);
    assertTrue(remaining.isPresent());
    assertEquals(Duration.ofDays(7), remaining.get().remaining());

    // Advance clock by 3 days
    clock.set(clock.get().plus(Duration.ofDays(3)));
    timerService.evaluateProgression().join();
    assertFalse(progressionService.isUnlocked(diamond));
    assertEquals(Duration.ofDays(4), timerService.timeRemaining(diamond).orElseThrow().remaining());

    // Advance clock by another 4 days (total 7 days)
    clock.set(clock.get().plus(Duration.ofDays(4)));
    timerService.evaluateProgression().join();
    assertTrue(progressionService.isUnlocked(diamond));
  }

  @Test
  void testDateProgression() {
    TierKey elytra = TierKey.of(EquipmentCategory.ELYTRA, "default");
    Instant target = Instant.parse("2026-12-01T00:00:00Z");
    registry.register(
        new EquipmentDefinition(
            elytra,
            UnlockCondition.date(target),
            RestrictionPolicy.all(),
            EnchantmentPolicy.allowed()));

    progressionService.initialize().join();
    assertFalse(progressionService.isUnlocked(elytra));

    // Before date
    timerService.evaluateProgression().join();
    assertFalse(progressionService.isUnlocked(elytra));

    // Fast forward to after date
    clock.set(target.plusSeconds(60));
    timerService.evaluateProgression().join();
    assertTrue(progressionService.isUnlocked(elytra));
  }

  @Test
  void testTimerControls() {
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    assertFalse(timerService.isPaused(diamond));
    timerService.pause(diamond);
    assertTrue(timerService.isPaused(diamond));
    timerService.resume(diamond);
    assertFalse(timerService.isPaused(diamond));

    timerService.addElapsed(diamond, Duration.ofHours(5));
    timerService.setElapsed(diamond, Duration.ofHours(10));
    timerService.resetTimer(diamond);
    assertFalse(timerService.isPaused(diamond));
  }
}
