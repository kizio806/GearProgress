package io.github.kizio806.gearprogress.platform.bukkit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Equipment Events Unit Tests")
class EquipmentEventsTest {

  @Test
  @DisplayName("EquipmentUnlockEvent stores properties and handles cancellation")
  void testUnlockEvent() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentUnlockEvent event = new EquipmentUnlockEvent(key, UnlockMethod.TIMER, "Timer");

    assertEquals(key, event.tierKey());
    assertEquals(EquipmentCategory.ARMOR, event.category());
    assertEquals("diamond", event.tier());
    assertEquals(UnlockMethod.TIMER, event.unlockMethod());
    assertEquals("Timer", event.source());

    assertFalse(event.isCancelled());
    event.setCancelled(true);
    assertTrue(event.isCancelled());

    assertNotNull(event.getHandlers());
    assertNotNull(EquipmentUnlockEvent.getHandlerList());
  }

  @Test
  @DisplayName("EquipmentLockEvent stores properties and handles cancellation")
  void testLockEvent() {
    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "netherite");
    EquipmentLockEvent event = new EquipmentLockEvent(key, "Admin");

    assertEquals(key, event.tierKey());
    assertEquals(EquipmentCategory.WEAPONS, event.category());
    assertEquals("netherite", event.tier());
    assertEquals("Admin", event.source());

    assertFalse(event.isCancelled());
    event.setCancelled(true);
    assertTrue(event.isCancelled());

    assertNotNull(event.getHandlers());
    assertNotNull(EquipmentLockEvent.getHandlerList());
  }
}
