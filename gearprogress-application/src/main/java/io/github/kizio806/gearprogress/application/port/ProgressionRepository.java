package io.github.kizio806.gearprogress.application.port;

import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import java.util.concurrent.CompletableFuture;

/** Outbound SPI port for loading and persisting equipment progression states. */
public interface ProgressionRepository {

  /**
   * Loads the current snapshot of all persisted progression states.
   *
   * @return future delivering the complete progression snapshot
   */
  CompletableFuture<ProgressionSnapshot> loadSnapshot();

  /**
   * Persists the updated state of an individual tier.
   *
   * @param state the updated state
   * @return future completing when write is flushed
   */
  CompletableFuture<Void> saveTierState(TierProgressState state);

  /**
   * Persists all states from the snapshot.
   *
   * @param snapshot complete snapshot
   * @return future completing when all records are persisted
   */
  CompletableFuture<Void> saveAll(ProgressionSnapshot snapshot);

  /** Closes underlying connections and flushes remaining writes. */
  void close();
}
