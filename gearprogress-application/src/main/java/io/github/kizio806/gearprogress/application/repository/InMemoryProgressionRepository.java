package io.github.kizio806.gearprogress.application.repository;

import io.github.kizio806.gearprogress.application.port.ProgressionRepository;
import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of {@link ProgressionRepository} for tests and fallbacks.
 */
public final class InMemoryProgressionRepository implements ProgressionRepository {

  private final Map<TierKey, TierProgressState> store = new ConcurrentHashMap<>();

  /** Constructs an empty in-memory repository. */
  public InMemoryProgressionRepository() {}

  /**
   * Constructs an in-memory repository populated with the given snapshot.
   *
   * @param initial initial snapshot
   */
  public InMemoryProgressionRepository(ProgressionSnapshot initial) {
    Objects.requireNonNull(initial, "initial snapshot must not be null");
    store.putAll(initial.states());
  }

  @Override
  public CompletableFuture<ProgressionSnapshot> loadSnapshot() {
    return CompletableFuture.completedFuture(ProgressionSnapshot.of(Map.copyOf(store)));
  }

  @Override
  public CompletableFuture<Void> saveTierState(TierProgressState state) {
    Objects.requireNonNull(state, "state must not be null");
    store.put(state.key(), state);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> saveAll(ProgressionSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot must not be null");
    store.putAll(snapshot.states());
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void close() {
    // No-op for in-memory storage
  }

  /** Clears all stored states. */
  public void clear() {
    store.clear();
  }
}
