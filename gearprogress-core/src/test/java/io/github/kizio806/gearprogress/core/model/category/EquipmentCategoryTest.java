package io.github.kizio806.gearprogress.core.model.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EquipmentCategoryTest {

  @Test
  void testFromStringValid() {
    assertTrue(EquipmentCategory.fromString("armor").isPresent());
    assertEquals(EquipmentCategory.ARMOR, EquipmentCategory.fromString("ARMOR").get());
    assertEquals(EquipmentCategory.TOOLS, EquipmentCategory.fromString("tools").get());
    assertEquals(EquipmentCategory.WEAPONS, EquipmentCategory.fromString("weapons").get());
    assertEquals(EquipmentCategory.SHIELD, EquipmentCategory.fromString("shield").get());
    assertEquals(EquipmentCategory.ELYTRA, EquipmentCategory.fromString("elytra").get());
  }

  @Test
  void testFromStringInvalid() {
    assertFalse(EquipmentCategory.fromString("invalid").isPresent());
    assertFalse(EquipmentCategory.fromString(null).isPresent());
    assertFalse(EquipmentCategory.fromString("").isPresent());
  }

  @Test
  void testFromStringOrThrow() {
    assertEquals(EquipmentCategory.ARMOR, EquipmentCategory.fromStringOrThrow("armor"));
    assertThrows(
        IllegalArgumentException.class, () -> EquipmentCategory.fromStringOrThrow("unknown"));
    assertThrows(NullPointerException.class, () -> EquipmentCategory.fromStringOrThrow(null));
  }

  @Test
  void testId() {
    assertEquals("armor", EquipmentCategory.ARMOR.id());
    assertEquals("elytra", EquipmentCategory.ELYTRA.id());
  }
}
