package io.github.kizio806.gearprogress.application.model;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe registry maintaining all currently loaded {@link EquipmentDefinition}s. */
public final class EquipmentRegistry {

  private final Map<TierKey, EquipmentDefinition> definitions = new ConcurrentHashMap<>();

  /**
   * Registers or replaces an equipment definition.
   *
   * @param definition non-null definition
   */
  public void register(EquipmentDefinition definition) {
    Objects.requireNonNull(definition, "definition must not be null");
    definitions.put(definition.key(), definition);
  }

  /**
   * Replaces all currently registered definitions with the provided collection.
   *
   * @param newDefinitions new collection of definitions
   */
  public void replaceAll(Collection<EquipmentDefinition> newDefinitions) {
    Objects.requireNonNull(newDefinitions, "newDefinitions must not be null");
    definitions.clear();
    for (EquipmentDefinition def : newDefinitions) {
      register(def);
    }
  }

  /**
   * Finds a definition by tier key.
   *
   * @param key the tier key
   * @return optional definition
   */
  public Optional<EquipmentDefinition> find(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    return Optional.ofNullable(definitions.get(key));
  }

  /**
   * Returns all definitions belonging to a specific category.
   *
   * @param category the equipment category
   * @return map of definitions
   */
  public Map<TierKey, EquipmentDefinition> findByCategory(EquipmentCategory category) {
    Objects.requireNonNull(category, "category must not be null");
    Map<TierKey, EquipmentDefinition> result = new LinkedHashMap<>();
    for (Map.Entry<TierKey, EquipmentDefinition> entry : definitions.entrySet()) {
      if (entry.getKey().category() == category) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Returns an unmodifiable snapshot of all registered definitions.
   *
   * @return all definitions
   */
  public Map<TierKey, EquipmentDefinition> all() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(definitions));
  }

  /**
   * Returns the count of registered definitions.
   *
   * @return number of definitions
   */
  public int size() {
    return definitions.size();
  }

  /** Clears all registered definitions. */
  public void clear() {
    definitions.clear();
  }
}
