package io.github.kizio806.gearprogress.core.model.state;

/** Progression state of an equipment tier. */
public enum TierStatus {
  LOCKED,
  UNLOCKED;

  /**
   * Checks whether this status represents an unlocked tier.
   *
   * @return true if unlocked
   */
  public boolean isUnlocked() {
    return this == UNLOCKED;
  }

  /**
   * Checks whether this status represents a locked tier.
   *
   * @return true if locked
   */
  public boolean isLocked() {
    return this == LOCKED;
  }
}
