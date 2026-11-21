package io.github.kizio806.gearprogress.core.model.tier;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import java.util.Locale;
import java.util.Objects;

/**
 * Composite immutable identifier addressing a specific tier within an equipment category.
 *
 * @param category the equipment category
 * @param tier the normalized tier identifier
 */
public record TierKey(EquipmentCategory category, String tier) implements Comparable<TierKey> {

  /**
   * Constructs and validates a new {@link TierKey}.
   *
   * @param category the non-null equipment category
   * @param tier the non-blank tier identifier
   */
  public TierKey {
    Objects.requireNonNull(category, "category must not be null");
    Objects.requireNonNull(tier, "tier must not be null");
    if (tier.isBlank()) {
      throw new IllegalArgumentException("tier identifier cannot be blank");
    }
    tier = tier.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Factory method for creating a key with a category and tier enum.
   *
   * @param category the equipment category
   * @param tier the equipment tier
   * @return the composite key
   */
  public static TierKey of(EquipmentCategory category, EquipmentTier tier) {
    Objects.requireNonNull(tier, "tier must not be null");
    return new TierKey(category, tier.id());
  }

  /**
   * Factory method for creating a key with a category and raw tier name.
   *
   * @param category the equipment category
   * @param tierName the tier name
   * @return the composite key
   */
  public static TierKey of(EquipmentCategory category, String tierName) {
    return new TierKey(category, tierName);
  }

  /**
   * Parses a formatted key string such as "armor:diamond" or "tools/iron".
   *
   * @param raw the key string
   * @return the parsed TierKey
   */
  public static TierKey parse(String raw) {
    Objects.requireNonNull(raw, "raw key string must not be null");
    String delimiter = raw.contains(":") ? ":" : "/";
    int index = raw.indexOf(delimiter);
    if (index <= 0 || index >= raw.length() - 1) {
      throw new IllegalArgumentException("Invalid TierKey format (expected category:tier): " + raw);
    }
    String categoryPart = raw.substring(0, index);
    String tierPart = raw.substring(index + 1);
    EquipmentCategory category = EquipmentCategory.fromStringOrThrow(categoryPart);
    return new TierKey(category, tierPart);
  }

  @Override
  public String toString() {
    return category.id() + ":" + tier;
  }

  @Override
  public int compareTo(TierKey other) {
    int categoryComparison = this.category.compareTo(other.category);
    if (categoryComparison != 0) {
      return categoryComparison;
    }
    return this.tier.compareTo(other.tier);
  }
}
