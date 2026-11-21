package io.github.kizio806.gearprogress.core.model.tier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import org.junit.jupiter.api.Test;

class TierKeyTest {

  @Test
  void testCreateAndFormat() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, EquipmentTier.DIAMOND);
    assertEquals(EquipmentCategory.ARMOR, key.category());
    assertEquals("diamond", key.tier());
    assertEquals("armor:diamond", key.toString());
  }

  @Test
  void testParse() {
    TierKey key1 = TierKey.parse("armor:netherite");
    assertEquals(EquipmentCategory.ARMOR, key1.category());
    assertEquals("netherite", key1.tier());

    TierKey key2 = TierKey.parse("tools/iron");
    assertEquals(EquipmentCategory.TOOLS, key2.category());
    assertEquals("iron", key2.tier());
  }

  @Test
  void testParseInvalid() {
    assertThrows(NullPointerException.class, () -> TierKey.parse(null));
    assertThrows(IllegalArgumentException.class, () -> TierKey.parse("invalid"));
    assertThrows(IllegalArgumentException.class, () -> TierKey.parse(":diamond"));
    assertThrows(IllegalArgumentException.class, () -> TierKey.parse("armor:"));
  }

  @Test
  void testValidation() {
    assertThrows(NullPointerException.class, () -> new TierKey(null, "diamond"));
    assertThrows(NullPointerException.class, () -> new TierKey(EquipmentCategory.ARMOR, null));
    assertThrows(IllegalArgumentException.class, () -> new TierKey(EquipmentCategory.ARMOR, "   "));
  }

  @Test
  void testEqualsAndCompare() {
    TierKey keyA = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierKey keyB = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierKey keyC = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey keyD = TierKey.of(EquipmentCategory.TOOLS, "diamond");

    assertEquals(keyA, keyB);
    assertEquals(keyA.hashCode(), keyB.hashCode());
    assertNotEquals(keyA, keyC);
    assertTrue(keyA.compareTo(keyC) < 0);
    assertTrue(keyA.compareTo(keyD) < 0);
  }
}
