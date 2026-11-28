package io.github.kizio806.gearprogress.core.model.unlock;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Sealed interface representing an unlock requirement condition for an equipment tier. */
public sealed interface UnlockCondition
    permits UnlockCondition.Always,
        UnlockCondition.Never,
        UnlockCondition.Timer,
        UnlockCondition.Date,
        UnlockCondition.Command {

  /**
   * Returns the categorization method for this condition.
   *
   * @return the associated {@link UnlockMethod}
   */
  UnlockMethod method();

  /** Condition indicating the tier is always unlocked from the beginning. */
  record Always() implements UnlockCondition {
    @Override
    public UnlockMethod method() {
      return UnlockMethod.ALWAYS;
    }
  }

  /** Condition indicating the tier is never automatically unlocked. */
  record Never() implements UnlockCondition {
    @Override
    public UnlockMethod method() {
      return UnlockMethod.NEVER;
    }
  }

  /**
   * Condition indicating the tier unlocks automatically after a relative duration.
   *
   * @param delay the relative duration required before unlock
   */
  record Timer(Duration delay) implements UnlockCondition {
    public Timer {
      Objects.requireNonNull(delay, "delay must not be null");
      if (delay.isNegative() || delay.isZero()) {
        throw new IllegalArgumentException("timer delay must be positive");
      }
    }

    @Override
    public UnlockMethod method() {
      return UnlockMethod.TIMER;
    }
  }

  /**
   * Condition indicating the tier unlocks automatically at an absolute point in time.
   *
   * @param targetDate the absolute unlock instant
   */
  record Date(Instant targetDate) implements UnlockCondition {
    public Date {
      Objects.requireNonNull(targetDate, "targetDate must not be null");
    }

    @Override
    public UnlockMethod method() {
      return UnlockMethod.DATE;
    }
  }

  /** Condition indicating the tier may only be unlocked manually via admin command. */
  record Command() implements UnlockCondition {
    @Override
    public UnlockMethod method() {
      return UnlockMethod.COMMAND;
    }
  }

  /**
   * Creates an always-unlocked condition.
   *
   * @return the condition
   */
  static Always always() {
    return new Always();
  }

  /**
   * Creates a never-unlocked condition.
   *
   * @return the condition
   */
  static Never never() {
    return new Never();
  }

  /**
   * Creates a timer-based condition with the specified relative duration.
   *
   * @param delay the duration
   * @return the condition
   */
  static Timer timer(Duration delay) {
    return new Timer(delay);
  }

  /**
   * Creates an absolute date-based condition.
   *
   * @param targetDate the date
   * @return the condition
   */
  static Date date(Instant targetDate) {
    return new Date(targetDate);
  }

  /**
   * Creates a command-only condition.
   *
   * @return the condition
   */
  static Command command() {
    return new Command();
  }
}
