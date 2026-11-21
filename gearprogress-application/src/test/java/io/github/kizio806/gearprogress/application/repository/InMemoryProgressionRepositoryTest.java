package io.github.kizio806.gearprogress.application.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryProgressionRepositoryTest {

  @Test
  void testRepositoryOperations() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierProgressState state =
        TierProgressState.initialUnlocked(key, UnlockMethod.ALWAYS, true, "CONFIG", Instant.now());

    InMemoryProgressionRepository repo =
        new InMemoryProgressionRepository(ProgressionSnapshot.of(Map.of(key, state)));

    var snapshot = repo.loadSnapshot().join();
    assertTrue(snapshot.isUnlocked(key));

    TierKey key2 = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierProgressState state2 =
        TierProgressState.initialLocked(key2, UnlockMethod.COMMAND, false, Instant.now());

    repo.saveAll(snapshot.withState(state2)).join();
    assertEquals(2, repo.loadSnapshot().join().states().size());

    repo.clear();
    assertEquals(0, repo.loadSnapshot().join().states().size());
    repo.close();
  }
}
