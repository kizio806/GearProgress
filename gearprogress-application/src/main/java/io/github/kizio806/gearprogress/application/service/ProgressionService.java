package io.github.kizio806.gearprogress.application.service;

import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.ProgressionRepository;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Core application service coordinating equipment tier state transitions (unlock, lock, reset). */
public final class ProgressionService {

  private final EquipmentRegistry registry;
  private final ProgressionRepository repository;
  private final EventPublisherPort eventPublisher;
  private final TimePort timePort;

  private final Map<TierKey, TierProgressState> runtimeStates = new ConcurrentHashMap<>();

  public ProgressionService(
      EquipmentRegistry registry,
      ProgressionRepository repository,
      EventPublisherPort eventPublisher,
      TimePort timePort) {
    this.registry = Objects.requireNonNull(registry, "registry must not be null");
    this.repository = Objects.requireNonNull(repository, "repository must not be null");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    this.timePort = Objects.requireNonNull(timePort, "timePort must not be null");
  }

  /**
   * Initializes runtime states from the repository and registered definitions.
   *
   * @return future completing when state loading is finished
   */
  public CompletableFuture<Void> initialize() {
    return repository
        .loadSnapshot()
        .thenCompose(
            snapshot -> {
              runtimeStates.clear();
              Instant now = timePort.now();
              java.util.List<CompletableFuture<Void>> saveFutures = new java.util.ArrayList<>();

              // Seed all registered definitions
              for (EquipmentDefinition def : registry.all().values()) {
                TierKey key = def.key();
                Optional<TierProgressState> existing = snapshot.findState(key);

                if (existing.isPresent()) {
                  runtimeStates.put(key, existing.get());
                } else {
                  // Initialize according to configured unlock condition
                  boolean initialUnlocked = def.unlockCondition() instanceof UnlockCondition.Always;
                  TierProgressState state =
                      initialUnlocked
                          ? TierProgressState.initialUnlocked(
                              key,
                              UnlockMethod.ALWAYS,
                              def.enchantmentPolicy().allowEnchantments(),
                              "CONFIG",
                              now)
                          : TierProgressState.initialLocked(
                              key,
                              def.unlockCondition().method(),
                              def.enchantmentPolicy().allowEnchantments(),
                              now);

                  runtimeStates.put(key, state);
                  saveFutures.add(repository.saveTierState(state));
                }
              }
              return CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture<?>[0]));
            });
  }

  /**
   * Checks whether a tier is currently unlocked.
   *
   * @param key the tier key
   * @return true if unlocked
   */
  public boolean isUnlocked(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    TierProgressState state = runtimeStates.get(key);
    if (state != null) {
      return state.isUnlocked();
    }
    // Check if definition is configured as 'always'
    return registry
        .find(key)
        .map(def -> def.unlockCondition() instanceof UnlockCondition.Always)
        .orElse(true); // Unregistered items default to unlocked
  }

  /**
   * Checks whether enchantments are currently permitted for a tier.
   *
   * @param key the tier key
   * @return true if permitted
   */
  public boolean isEnchantmentAllowed(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    TierProgressState state = runtimeStates.get(key);
    if (state != null) {
      return state.allowEnchantments();
    }
    return registry.find(key).map(def -> def.enchantmentPolicy().allowEnchantments()).orElse(true);
  }

  /**
   * Unlocks an equipment tier.
   *
   * @param key tier key
   * @param method unlock method
   * @param source operator or trigger name
   * @return future completing with true if state transitioned, false if already unlocked
   */
  public CompletableFuture<Boolean> unlock(TierKey key, UnlockMethod method, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(method, "method must not be null");
    Objects.requireNonNull(source, "source must not be null");

    Instant now = timePort.now();
    TierProgressState current = runtimeStates.get(key);
    if (current != null && current.isUnlocked()) {
      return CompletableFuture.completedFuture(false);
    }

    boolean allowEnchant =
        current != null
            ? current.allowEnchantments()
            : registry.find(key).map(d -> d.enchantmentPolicy().allowEnchantments()).orElse(true);

    TierProgressState updated =
        (current != null)
            ? current.withUnlocked(method, source, now)
            : TierProgressState.initialUnlocked(key, method, allowEnchant, source, now);

    runtimeStates.put(key, updated);
    eventPublisher.publishUnlock(key, method, source);

    return repository.saveTierState(updated).thenApply(v -> true);
  }

  /**
   * Locks an equipment tier.
   *
   * @param key tier key
   * @param source operator or trigger name
   * @return future completing with true if transitioned, false if already locked
   */
  public CompletableFuture<Boolean> lock(TierKey key, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(source, "source must not be null");

    Instant now = timePort.now();
    TierProgressState current = runtimeStates.get(key);
    if (current != null && current.isLocked()) {
      return CompletableFuture.completedFuture(false);
    }

    boolean allowEnchant =
        current != null
            ? current.allowEnchantments()
            : registry.find(key).map(d -> d.enchantmentPolicy().allowEnchantments()).orElse(false);

    TierProgressState updated =
        (current != null)
            ? current.withLocked(source, now)
            : TierProgressState.initialLocked(
                key,
                registry
                    .find(key)
                    .map(d -> d.unlockCondition().method())
                    .orElse(UnlockMethod.COMMAND),
                allowEnchant,
                now);

    runtimeStates.put(key, updated);
    eventPublisher.publishLock(key, source);

    return repository.saveTierState(updated).thenApply(v -> true);
  }

  /**
   * Updates enchantment permission for a tier.
   *
   * @param key tier key
   * @param allow whether enchantments are allowed
   * @param source operator
   * @return future
   */
  public CompletableFuture<Void> setEnchantmentAllowed(TierKey key, boolean allow, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Instant now = timePort.now();
    TierProgressState current = runtimeStates.get(key);
    if (current == null) {
      return CompletableFuture.completedFuture(null);
    }
    TierProgressState updated = current.withEnchantmentAllowed(allow, now);
    runtimeStates.put(key, updated);
    eventPublisher.publishEnchantmentAllowed(key, allow, source);
    return repository.saveTierState(updated);
  }

  /**
   * Resets progression state for a category or all categories back to default configuration.
   *
   * @param category category to reset, or null for all
   * @param source operator
   * @return future
   */
  public CompletableFuture<Void> reset(EquipmentCategory category, String source) {
    Instant now = timePort.now();
    for (EquipmentDefinition def : registry.all().values()) {
      if (category == null || def.key().category() == category) {
        boolean initialUnlocked = def.unlockCondition() instanceof UnlockCondition.Always;
        TierProgressState resetState =
            initialUnlocked
                ? TierProgressState.initialUnlocked(
                    def.key(),
                    UnlockMethod.ALWAYS,
                    def.enchantmentPolicy().allowEnchantments(),
                    source,
                    now)
                : TierProgressState.initialLocked(
                    def.key(),
                    def.unlockCondition().method(),
                    def.enchantmentPolicy().allowEnchantments(),
                    now);

        runtimeStates.put(def.key(), resetState);
      }
    }
    return repository.saveAll(snapshot());
  }

  /**
   * Retrieves an immutable snapshot of all current states.
   *
   * @return progression snapshot
   */
  public ProgressionSnapshot snapshot() {
    return ProgressionSnapshot.of(Map.copyOf(runtimeStates));
  }

  /**
   * Retrieves state for a given tier key.
   *
   * @param key tier key
   * @return optional progress state
   */
  public Optional<TierProgressState> state(TierKey key) {
    Objects.requireNonNull(key, "key must not be null");
    return Optional.ofNullable(runtimeStates.get(key));
  }
}
