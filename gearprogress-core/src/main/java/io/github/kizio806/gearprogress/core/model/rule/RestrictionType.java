package io.github.kizio806.gearprogress.core.model.rule;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Types of player actions that can be independently blocked for locked equipment. */
public enum RestrictionType {
  CRAFT("craft"),
  USE("use"),
  EQUIP("equip"),
  PICKUP("pickup"),
  INVENTORY_MOVE("inventory-move");

  private final String configKey;

  RestrictionType(String configKey) {
    this.configKey = configKey;
  }

  /**
   * Returns the canonical configuration key for this restriction type.
   *
   * @return the configuration key
   */
  public String configKey() {
    return configKey;
  }

  /**
   * Parses a raw string into a {@link RestrictionType}.
   *
   * @param raw the raw input
   * @return the optional matched restriction type
   */
  public static Optional<RestrictionType> fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    for (RestrictionType type : values()) {
      if (type.configKey.equals(normalized)
          || type.name().toLowerCase(Locale.ROOT).equals(normalized)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  /**
   * Resolves a restriction type or throws an {@link IllegalArgumentException}.
   *
   * @param raw the raw input
   * @return the matched restriction type
   */
  public static RestrictionType fromStringOrThrow(String raw) {
    Objects.requireNonNull(raw, "raw restriction string must not be null");
    return fromString(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown restriction type: " + raw));
  }
}
