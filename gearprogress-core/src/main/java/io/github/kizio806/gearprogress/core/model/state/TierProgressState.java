package io.github.kizio806.gearprogress.core.model.state;

import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable record representing the runtime progression state of a specific equipment tier.
 *
 * @param key the unique tier key
 * @param status whether the tier is locked or unlocked
 * @param allowEnchantments whether enchantments are currently permitted for this tier
 * @param unlockedAt timestamp when unlock occurred, if unlocked
 * @param unlockMethod mechanism through which unlock occurred
 * @param unlockedBy operator or system identifier who triggered the unlock
 * @param updatedAt timestamp when state was last modified
 */
public record TierProgressState(
    TierKey key,
    TierStatus status,
    boolean allowEnchantments,
    Optional<Instant> unlockedAt,
    UnlockMethod unlockMethod,
    Optional<String> unlockedBy,
    Instant updatedAt) {

  /** Constructs a validated tier progress state. */
  public TierProgressState {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(unlockedAt, "unlockedAt must not be null");
    Objects.requireNonNull(unlockMethod, "unlockMethod must not be null");
    Objects.requireNonNull(unlockedBy, "unlockedBy must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
  }

  /**
   * Factory method to create an initial locked state for a tier.
   *
   * @param key the tier key
   * @param configuredUnlockMethod configured method
   * @param defaultAllowEnchantments initial enchantment setting
   * @param now current timestamp
   * @return new locked tier state
   */
  public static TierProgressState initialLocked(
      TierKey key,
      UnlockMethod configuredUnlockMethod,
      boolean defaultAllowEnchantments,
      Instant now) {
    return new TierProgressState(
        key,
        TierStatus.LOCKED,
        defaultAllowEnchantments,
        Optional.empty(),
        configuredUnlockMethod,
        Optional.empty(),
        now);
  }

  /**
   * Factory method to create an initial unlocked state for a tier.
   *
   * @param key the tier key
   * @param method unlock method
   * @param allowEnchantments enchantment setting
   * @param source source or operator
   * @param now current timestamp
   * @return new unlocked tier state
   */
  public static TierProgressState initialUnlocked(
      TierKey key, UnlockMethod method, boolean allowEnchantments, String source, Instant now) {
    return new TierProgressState(
        key,
        TierStatus.UNLOCKED,
        allowEnchantments,
        Optional.of(now),
        method,
        Optional.ofNullable(source),
        now);
  }

  /**
   * Returns a new state transitioned to unlocked.
   *
   * @param method method that triggered the unlock
   * @param source operator or trigger name
   * @param now timestamp of unlock
   * @return unlocked state
   */
  public TierProgressState withUnlocked(UnlockMethod method, String source, Instant now) {
    return new TierProgressState(
        this.key,
        TierStatus.UNLOCKED,
        this.allowEnchantments,
        Optional.of(now),
        method,
        Optional.ofNullable(source),
        now);
  }

  /**
   * Returns a new state transitioned to locked.
   *
   * @param source operator who locked the tier
   * @param now timestamp
   * @return locked state
   */
  public TierProgressState withLocked(String source, Instant now) {
    return new TierProgressState(
        this.key,
        TierStatus.LOCKED,
        this.allowEnchantments,
        Optional.empty(),
        this.unlockMethod,
        Optional.ofNullable(source),
        now);
  }

  /**
   * Returns a new state with updated enchantment permissions.
   *
   * @param allow whether enchantments are allowed
   * @param now timestamp
   * @return updated state
   */
  public TierProgressState withEnchantmentAllowed(boolean allow, Instant now) {
    return new TierProgressState(
        this.key, this.status, allow, this.unlockedAt, this.unlockMethod, this.unlockedBy, now);
  }

  /**
   * Returns true if this tier is currently unlocked.
   *
   * @return true if unlocked
   */
  public boolean isUnlocked() {
    return status.isUnlocked();
  }

  /**
   * Returns true if this tier is currently locked.
   *
   * @return true if locked
   */
  public boolean isLocked() {
    return status.isLocked();
  }
}
