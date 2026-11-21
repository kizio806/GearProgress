package io.github.kizio806.gearprogress.application.service;

import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Objects;
import java.util.Optional;

/**
 * High-performance query service for determining whether specific player actions or enchantments
 * are blocked for an equipment item based on its compiled tier policy and progression state.
 */
public final class RestrictionQueryService {

  private final ProgressionService progressionService;
  private final EquipmentRegistry registry;

  public RestrictionQueryService(
      ProgressionService progressionService, EquipmentRegistry registry) {
    this.progressionService =
        Objects.requireNonNull(progressionService, "progressionService must not be null");
    this.registry = Objects.requireNonNull(registry, "registry must not be null");
  }

  /**
   * Checks whether the specified action is blocked for an item identified by {@link TierKey}.
   *
   * @param key the tier key of the item
   * @param action the player action being attempted
   * @return true if the action is restricted/blocked, false if allowed
   */
  public boolean isActionRestricted(TierKey key, RestrictionType action) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(action, "action must not be null");

    // If unlocked, all actions are allowed
    if (progressionService.isUnlocked(key)) {
      return false;
    }

    // Tier is locked: check restriction policy
    Optional<EquipmentDefinition> definitionOpt = registry.find(key);
    if (definitionOpt.isEmpty()) {
      return false; // Unregistered tier is unrestricted
    }

    return definitionOpt.get().restrictionPolicy().isRestricted(action);
  }

  /**
   * Checks whether enchantments are prohibited for an item identified by {@link TierKey}.
   *
   * @param key the tier key
   * @return true if enchantments are forbidden, false if allowed
   */
  public boolean isEnchantmentForbidden(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    return !progressionService.isEnchantmentAllowed(key);
  }
}
