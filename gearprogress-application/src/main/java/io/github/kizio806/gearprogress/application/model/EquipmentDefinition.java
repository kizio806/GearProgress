package io.github.kizio806.gearprogress.application.model;

import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import java.util.Objects;

/**
 * Compiled immutable definition for an equipment tier, containing its configured unlock condition,
 * restriction policy, and enchantment policy.
 *
 * @param key unique composite tier key
 * @param unlockCondition the condition required to unlock this tier
 * @param restrictionPolicy the actions restricted while this tier is locked
 * @param enchantmentPolicy whether enchantments are allowed on this tier
 */
public record EquipmentDefinition(
    TierKey key,
    UnlockCondition unlockCondition,
    RestrictionPolicy restrictionPolicy,
    EnchantmentPolicy enchantmentPolicy) {

  /** Constructs a validated equipment definition. */
  public EquipmentDefinition {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(unlockCondition, "unlockCondition must not be null");
    Objects.requireNonNull(restrictionPolicy, "restrictionPolicy must not be null");
    Objects.requireNonNull(enchantmentPolicy, "enchantmentPolicy must not be null");
  }
}
