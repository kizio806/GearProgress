package io.github.kizio806.gearprogress.application.config.spec;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Raw configuration specification for an individual equipment tier.
 *
 * @param unlockMethodType type string (always, never, timer, date, command)
 * @param timerAfter duration string for timer method (e.g., "7d")
 * @param dateValue ISO-8601 string for date method
 * @param allowEnchantments whether enchantments are allowed
 * @param restrictions map of restriction names to boolean flags (craft, use, equip, pickup,
 *     inventory-move)
 */
public record TierConfigSpec(
    String unlockMethodType,
    Optional<String> timerAfter,
    Optional<String> dateValue,
    boolean allowEnchantments,
    Map<String, Boolean> restrictions) {

  /** Constructs a validated tier spec. */
  public TierConfigSpec {
    Objects.requireNonNull(unlockMethodType, "unlockMethodType must not be null");
    Objects.requireNonNull(timerAfter, "timerAfter must not be null");
    Objects.requireNonNull(dateValue, "dateValue must not be null");
    restrictions =
        restrictions == null ? Collections.emptyMap() : Collections.unmodifiableMap(restrictions);
  }
}
