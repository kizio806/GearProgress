package io.github.kizio806.gearprogress.application.config.spec;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Root specification of all equipment categories and tiers from equipment.yml.
 *
 * @param categories map of category name to category specification
 */
public record EquipmentConfigSpec(Map<String, CategoryConfigSpec> categories) {

  /** Constructs an immutable equipment config spec. */
  public EquipmentConfigSpec {
    Objects.requireNonNull(categories, "categories must not be null");
    categories = Collections.unmodifiableMap(categories);
  }
}
