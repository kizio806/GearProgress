package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteProgressionRepositoryTest {

  @Test
  void testCrudAndRestartRecovery(@TempDir Path tempDir) throws IOException, SQLException {
    Path dbPath = tempDir.resolve("gearprogress.db");
    SQLiteProgressionRepository repo = new SQLiteProgressionRepository(dbPath);
    repo.initialize();

    TierKey iron = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    Instant now = Instant.parse("2026-11-20T12:00:00Z");

    TierProgressState ironState =
        TierProgressState.initialUnlocked(iron, UnlockMethod.ALWAYS, true, "CONFIG", now);
    TierProgressState diamondState =
        TierProgressState.initialLocked(diamond, UnlockMethod.TIMER, false, now);

    repo.saveTierState(ironState).join();
    repo.saveTierState(diamondState).join();

    ProgressionSnapshot snapshot = repo.loadSnapshot().join();
    assertTrue(snapshot.isUnlocked(iron));
    assertFalse(snapshot.isUnlocked(diamond));
    assertEquals(2, snapshot.states().size());

    TierProgressState unlockedDiamond =
        diamondState.withUnlocked(UnlockMethod.TIMER, "SYSTEM", now.plusSeconds(3600));
    repo.saveTierState(unlockedDiamond).join();

    repo.close();

    // Reopen from disk (testing restart recovery)
    SQLiteProgressionRepository reopened = new SQLiteProgressionRepository(dbPath);
    reopened.initialize();
    ProgressionSnapshot recovered = reopened.loadSnapshot().join();

    assertTrue(recovered.isUnlocked(diamond));
    assertEquals("SYSTEM", recovered.findState(diamond).orElseThrow().unlockedBy().orElseThrow());

    TierKey elytra = TierKey.of(EquipmentCategory.ELYTRA, "default");
    TierProgressState elytraState =
        TierProgressState.initialUnlocked(elytra, UnlockMethod.DATE, true, "CONFIG", now);
    reopened.saveAll(ProgressionSnapshot.of(Map.of(elytra, elytraState))).join();

    assertTrue(reopened.loadSnapshot().join().findState(elytra).isPresent());
    reopened.close();
  }

  @Test
  void testConcurrentWrites(@TempDir Path tempDir) throws IOException, SQLException {
    Path dbPath = tempDir.resolve("concurrent.db");
    SQLiteProgressionRepository repo = new SQLiteProgressionRepository(dbPath);
    repo.initialize();

    Instant now = Instant.parse("2026-11-20T12:00:00Z");
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (int i = 0; i < 50; i++) {
      TierKey key = TierKey.of(EquipmentCategory.TOOLS, "tier" + i);
      TierProgressState state =
          TierProgressState.initialLocked(key, UnlockMethod.ALWAYS, true, now);
      futures.add(repo.saveTierState(state));
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
    ProgressionSnapshot snapshot = repo.loadSnapshot().join();
    assertEquals(50, snapshot.states().size());

    repo.close();
  }
}
