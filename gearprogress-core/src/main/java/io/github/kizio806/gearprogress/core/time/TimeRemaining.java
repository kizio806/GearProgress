package io.github.kizio806.gearprogress.core.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value representing time remaining until a scheduled event or milestone.
 *
 * @param remaining the remaining duration (zero if expired)
 * @param isExpired whether the milestone has already passed
 */
public record TimeRemaining(Duration remaining, boolean isExpired) {

  private static final TimeRemaining EXPIRED = new TimeRemaining(Duration.ZERO, true);

  /** Constructs a validated time remaining value. */
  public TimeRemaining {
    Objects.requireNonNull(remaining, "remaining duration must not be null");
    if (remaining.isNegative()) {
      remaining = Duration.ZERO;
      isExpired = true;
    }
  }

  /**
   * Calculates time remaining between now and a target instant.
   *
   * @param target the target instant
   * @param now current instant
   * @return time remaining
   */
  public static TimeRemaining until(Instant target, Instant now) {
    Objects.requireNonNull(target, "target instant must not be null");
    Objects.requireNonNull(now, "now instant must not be null");
    if (now.isAfter(target) || now.equals(target)) {
      return EXPIRED;
    }
    return new TimeRemaining(Duration.between(now, target), false);
  }

  /**
   * Returns an expired time remaining instance.
   *
   * @return expired instance
   */
  public static TimeRemaining expired() {
    return EXPIRED;
  }

  /**
   * Formats the remaining time using human-readable representation.
   *
   * @return human formatted string
   */
  public String formatHuman() {
    return RelativeDurationParser.formatHuman(remaining);
  }
}
