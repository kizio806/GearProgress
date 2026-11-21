package io.github.kizio806.gearprogress.core.model.category;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Enumeration of supported equipment progression categories. */
public enum EquipmentCategory {
  ARMOR("armor"),
  TOOLS("tools"),
  WEAPONS("weapons"),
  SHIELD("shield"),
  ELYTRA("elytra");

  private final String id;

  EquipmentCategory(String id) {
    this.id = id;
  }

  /**
   * Returns the canonical identifier string for this category.
   *
   * @return the lowercase category identifier
   */
  public String id() {
    return id;
  }

  /**
   * Attempts to parse a string representation of an equipment category.
   *
   * @param raw the raw input string
   * @return an {@link Optional} containing the matched category, or empty if unknown
   */
  public static Optional<EquipmentCategory> fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    for (EquipmentCategory category : values()) {
      if (category.id.equals(normalized)
          || category.name().toLowerCase(Locale.ROOT).equals(normalized)) {
        return Optional.of(category);
      }
    }
    return Optional.empty();
  }

  /**
   * Resolves a category by raw string or throws an {@link IllegalArgumentException}.
   *
   * @param raw the raw input string
   * @return the matched category
   * @throws IllegalArgumentException if the category is invalid or null
   */
  public static EquipmentCategory fromStringOrThrow(String raw) {
    Objects.requireNonNull(raw, "category string must not be null");
    return fromString(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown equipment category: " + raw));
  }
}
