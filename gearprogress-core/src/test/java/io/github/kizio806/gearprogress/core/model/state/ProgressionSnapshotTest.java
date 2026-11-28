package io.github.kizio806.gearprogress.core.model.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProgressionSnapshotTest {

  @Test
  void testSnapshotOperations() {
    TierKey iron = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierKey bow = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    Instant now = Instant.parse("2026-11-20T12:00:00Z");

    TierProgressState stateIron =
        TierProgressState.initialUnlocked(iron, UnlockMethod.ALWAYS, true, "CONFIG", now);
    TierProgressState stateDiamond =
        TierProgressState.initialLocked(diamond, UnlockMethod.TIMER, false, now);

    ProgressionSnapshot snapshot =
        ProgressionSnapshot.of(Map.of(iron, stateIron, diamond, stateDiamond));

    assertTrue(snapshot.isUnlocked(iron));
    assertFalse(snapshot.isUnlocked(diamond));
    assertFalse(snapshot.isUnlocked(bow));
    assertTrue(snapshot.isEnchantmentAllowed(iron));
    assertFalse(snapshot.isEnchantmentAllowed(diamond));

    Map<TierKey, TierProgressState> armorStates =
        snapshot.statesForCategory(EquipmentCategory.ARMOR);
    assertEquals(2, armorStates.size());
    assertTrue(snapshot.statesForCategory(EquipmentCategory.WEAPONS).isEmpty());

    TierProgressState updatedDiamond = stateDiamond.withUnlocked(UnlockMethod.TIMER, "SYSTEM", now);
    ProgressionSnapshot nextSnapshot = snapshot.withState(updatedDiamond);

    assertTrue(nextSnapshot.isUnlocked(diamond));
    assertFalse(snapshot.isUnlocked(diamond)); // Immutability preserved
  }
}
