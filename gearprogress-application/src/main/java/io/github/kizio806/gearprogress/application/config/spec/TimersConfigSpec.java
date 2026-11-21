package io.github.kizio806.gearprogress.application.config.spec;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Timer states and reference epoch configuration mapped from timers.yml.
 *
 * @param serverStartEpoch reference starting timestamp for relative timers
 * @param pausedStates map of tier key string to boolean paused state
 * @param accumulatedSeconds map of tier key string to accumulated elapsed seconds
 */
public record TimersConfigSpec(
    Instant serverStartEpoch,
    Map<String, Boolean> pausedStates,
    Map<String, Long> accumulatedSeconds) {

  /** Constructs an immutable timers config spec. */
  public TimersConfigSpec {
    Objects.requireNonNull(serverStartEpoch, "serverStartEpoch must not be null");
    pausedStates =
        pausedStates == null ? Collections.emptyMap() : Collections.unmodifiableMap(pausedStates);
    accumulatedSeconds =
        accumulatedSeconds == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(accumulatedSeconds);
  }

  /**
   * Returns default timers spec anchored at the given timestamp.
   *
   * @param now current instant
   * @return default spec
   */
  public static TimersConfigSpec initial(Instant now) {
    return new TimersConfigSpec(now, Collections.emptyMap(), Collections.emptyMap());
  }
}
