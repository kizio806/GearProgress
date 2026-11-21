package io.github.kizio806.gearprogress.core.model.tier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EquipmentTierTest {

  @Test
  void testFromStringValid() {
    assertEquals(EquipmentTier.WOODEN, EquipmentTier.fromString("wooden").orElseThrow());
    assertEquals(EquipmentTier.IRON, EquipmentTier.fromString("iron").orElseThrow());
    assertEquals(EquipmentTier.DIAMOND, EquipmentTier.fromString("DIAMOND").orElseThrow());
    assertEquals(EquipmentTier.NETHERITE, EquipmentTier.fromString("netherite").orElseThrow());
  }

  @Test
  void testFromStringInvalid() {
    assertFalse(EquipmentTier.fromString("magic").isPresent());
    assertFalse(EquipmentTier.fromString(null).isPresent());
    assertFalse(EquipmentTier.fromString("   ").isPresent());
  }

  @Test
  void testFromStringOrThrow() {
    assertEquals(EquipmentTier.CHAINMAIL, EquipmentTier.fromStringOrThrow("chainmail"));
    assertThrows(IllegalArgumentException.class, () -> EquipmentTier.fromStringOrThrow("ruby"));
    assertThrows(NullPointerException.class, () -> EquipmentTier.fromStringOrThrow(null));
  }

  @Test
  void testId() {
    assertEquals("netherite", EquipmentTier.NETHERITE.id());
  }
}
