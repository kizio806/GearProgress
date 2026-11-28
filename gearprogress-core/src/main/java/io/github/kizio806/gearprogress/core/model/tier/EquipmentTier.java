package io.github.kizio806.gearprogress.core.model.tier;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Standard equipment material tiers recognized by progression policies. */
public enum EquipmentTier {
  WOODEN("wooden"),
  STONE("stone"),
  LEATHER("leather"),
  CHAINMAIL("chainmail"),
  IRON("iron"),
  GOLDEN("golden"),
  DIAMOND("diamond"),
  NETHERITE("netherite"),
  DEFAULT("default");

  private final String id;

  EquipmentTier(String id) {
    this.id = id;
  }

  /**
   * Returns the canonical identifier string for this tier.
   *
   * @return the lowercase tier identifier
   */
  public String id() {
    return id;
  }

  /**
   * Attempts to parse a string representation of an equipment tier.
   *
   * @param raw the raw input string
   * @return an {@link Optional} containing the matched tier, or empty if unknown
   */
  public static Optional<EquipmentTier> fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    for (EquipmentTier tier : values()) {
      if (tier.id.equals(normalized) || tier.name().toLowerCase(Locale.ROOT).equals(normalized)) {
        return Optional.of(tier);
      }
    }
    return Optional.empty();
  }

  /**
   * Resolves a tier by raw string or throws an {@link IllegalArgumentException}.
   *
   * @param raw the raw input string
   * @return the matched tier
   * @throws IllegalArgumentException if the tier is invalid or null
   */
  public static EquipmentTier fromStringOrThrow(String raw) {
    Objects.requireNonNull(raw, "tier string must not be null");
    return fromString(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown equipment tier: " + raw));
  }
}
