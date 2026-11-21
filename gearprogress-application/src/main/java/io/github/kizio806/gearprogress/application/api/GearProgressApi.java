package io.github.kizio806.gearprogress.application.api;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Public programmatic API for interacting with the GearProgress progression engine. */
public interface GearProgressApi {

  /**
   * Checks whether the specified equipment tier is currently unlocked.
   *
   * @param category the category
   * @param tier the tier name
   * @return true if unlocked
   */
  boolean isUnlocked(EquipmentCategory category, String tier);

  /**
   * Checks whether the specified equipment tier is currently unlocked.
   *
   * @param key the tier key
   * @return true if unlocked
   */
  boolean isUnlocked(TierKey key);

  /**
   * Checks whether enchantments are allowed on equipment of this tier.
   *
   * @param category the category
   * @param tier the tier name
   * @return true if enchantments are allowed
   */
  boolean isEnchantmentAllowed(EquipmentCategory category, String tier);

  /**
   * Checks whether enchantments are allowed on equipment of this tier.
   *
   * @param key the tier key
   * @return true if enchantments are allowed
   */
  boolean isEnchantmentAllowed(TierKey key);

  /**
   * Unlocks an equipment tier.
   *
   * @param category the category
   * @param tier the tier name
   * @param reason explanation or trigger name
   * @return future delivering true if state changed, false if already unlocked
   */
  CompletableFuture<Boolean> unlock(EquipmentCategory category, String tier, String reason);

  /**
   * Unlocks an equipment tier.
   *
   * @param key the tier key
   * @param reason explanation
   * @return future delivering true if state changed, false if already unlocked
   */
  CompletableFuture<Boolean> unlock(TierKey key, String reason);

  /**
   * Locks an equipment tier.
   *
   * @param category the category
   * @param tier the tier name
   * @param reason explanation
   * @return future delivering true if state changed, false if already locked
   */
  CompletableFuture<Boolean> lock(EquipmentCategory category, String tier, String reason);

  /**
   * Locks an equipment tier.
   *
   * @param key the tier key
   * @param reason explanation
   * @return future delivering true if state changed, false if already locked
   */
  CompletableFuture<Boolean> lock(TierKey key, String reason);

  /**
   * Retrieves remaining time before this tier unlocks.
   *
   * @param category the category
   * @param tier the tier name
   * @return optional time remaining
   */
  Optional<TimeRemaining> remainingTime(EquipmentCategory category, String tier);

  /**
   * Retrieves remaining time before this tier unlocks.
   *
   * @param key the tier key
   * @return optional time remaining
   */
  Optional<TimeRemaining> remainingTime(TierKey key);

  /**
   * Retrieves the current progression state of the tier.
   *
   * @param category the category
   * @param tier the tier name
   * @return optional progress state
   */
  Optional<TierProgressState> currentState(EquipmentCategory category, String tier);

  /**
   * Retrieves the current progression state of the tier.
   *
   * @param key the tier key
   * @return optional progress state
   */
  Optional<TierProgressState> currentState(TierKey key);
}
