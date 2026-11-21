package io.github.kizio806.gearprogress.core.model.state;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable complete snapshot of all equipment tier progression states.
 *
 * @param states map of tier keys to progress states
 */
public record ProgressionSnapshot(Map<TierKey, TierProgressState> states) {

  private static final ProgressionSnapshot EMPTY = new ProgressionSnapshot(Collections.emptyMap());

  /**
   * Constructs an immutable snapshot.
   *
   * @param states non-null map
   */
  public ProgressionSnapshot {
    Objects.requireNonNull(states, "states must not be null");
    states = Collections.unmodifiableMap(new LinkedHashMap<>(states));
  }

  /**
   * Returns an empty progression snapshot.
   *
   * @return empty snapshot
   */
  public static ProgressionSnapshot empty() {
    return EMPTY;
  }

  /**
   * Creates a snapshot from a map of tier states.
   *
   * @param states input states map
   * @return immutable snapshot
   */
  public static ProgressionSnapshot of(Map<TierKey, TierProgressState> states) {
    return new ProgressionSnapshot(states);
  }

  /**
   * Retrieves the progress state for a given tier key.
   *
   * @param key the tier key
   * @return optional progress state
   */
  public Optional<TierProgressState> findState(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    return Optional.ofNullable(states.get(key));
  }

  /**
   * Checks whether the specified tier is currently unlocked.
   *
   * @param key the tier key
   * @return true if present and unlocked, false otherwise
   */
  public boolean isUnlocked(TierKey key) {
    return findState(key).map(TierProgressState::isUnlocked).orElse(false);
  }

  /**
   * Checks whether enchantments are allowed for the specified tier.
   *
   * @param key the tier key
   * @return true if present and enchantments are allowed, false otherwise
   */
  public boolean isEnchantmentAllowed(TierKey key) {
    return findState(key).map(TierProgressState::allowEnchantments).orElse(false);
  }

  /**
   * Returns all states belonging to a specific category.
   *
   * @param category the category
   * @return map of tier keys to progress states in that category
   */
  public Map<TierKey, TierProgressState> statesForCategory(EquipmentCategory category) {
    Objects.requireNonNull(category, "category must not be null");
    Map<TierKey, TierProgressState> result = new LinkedHashMap<>();
    for (Map.Entry<TierKey, TierProgressState> entry : states.entrySet()) {
      if (entry.getKey().category() == category) {
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return Collections.unmodifiableMap(result);
  }

  /**
   * Returns a new snapshot with an updated or added tier state.
   *
   * @param newState the updated state
   * @return updated snapshot
   */
  public ProgressionSnapshot withState(TierProgressState newState) {
    Objects.requireNonNull(newState, "newState must not be null");
    Map<TierKey, TierProgressState> updated = new LinkedHashMap<>(states);
    updated.put(newState.key(), newState);
    return new ProgressionSnapshot(updated);
  }
}
