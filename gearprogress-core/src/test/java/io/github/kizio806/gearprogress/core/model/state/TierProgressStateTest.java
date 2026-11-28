package io.github.kizio806.gearprogress.core.model.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TierProgressStateTest {

  @Test
  void testInitialLockedAndTransitions() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    Instant now = Instant.parse("2026-11-20T12:00:00Z");

    TierProgressState state = TierProgressState.initialLocked(key, UnlockMethod.TIMER, false, now);
    assertTrue(state.isLocked());
    assertFalse(state.isUnlocked());
    assertFalse(state.allowEnchantments());
    assertTrue(state.unlockedAt().isEmpty());

    Instant unlockTime = Instant.parse("2026-11-20T14:00:00Z");
    TierProgressState unlocked = state.withUnlocked(UnlockMethod.TIMER, "SYSTEM", unlockTime);
    assertTrue(unlocked.isUnlocked());
    assertFalse(unlocked.isLocked());
    assertEquals(unlockTime, unlocked.unlockedAt().orElseThrow());
    assertEquals("SYSTEM", unlocked.unlockedBy().orElseThrow());
    assertEquals(UnlockMethod.TIMER, unlocked.unlockMethod());

    Instant enchantTime = Instant.parse("2026-11-20T16:00:00Z");
    TierProgressState enchanted = unlocked.withEnchantmentAllowed(true, enchantTime);
    assertTrue(enchanted.allowEnchantments());
    assertEquals(enchantTime, enchanted.updatedAt());

    Instant lockTime = Instant.parse("2026-11-20T18:00:00Z");
    TierProgressState locked = enchanted.withLocked("ADMIN", lockTime);
    assertTrue(locked.isLocked());
    assertTrue(locked.unlockedAt().isEmpty());
    assertEquals("ADMIN", locked.unlockedBy().orElseThrow());
  }

  @Test
  void testInitialUnlocked() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "leather");
    Instant now = Instant.parse("2026-11-20T12:00:00Z");
    TierProgressState state =
        TierProgressState.initialUnlocked(key, UnlockMethod.ALWAYS, true, "CONFIG", now);

    assertTrue(state.isUnlocked());
    assertTrue(state.allowEnchantments());
    assertEquals(UnlockMethod.ALWAYS, state.unlockMethod());
  }

  @Test
  void testTierStatusHelper() {
    assertTrue(TierStatus.UNLOCKED.isUnlocked());
    assertFalse(TierStatus.UNLOCKED.isLocked());
    assertTrue(TierStatus.LOCKED.isLocked());
    assertFalse(TierStatus.LOCKED.isUnlocked());
  }
}
