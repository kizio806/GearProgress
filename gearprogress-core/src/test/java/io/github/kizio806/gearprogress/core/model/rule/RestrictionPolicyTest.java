package io.github.kizio806.gearprogress.core.model.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RestrictionPolicyTest {

  @Test
  void testNoneAndAll() {
    RestrictionPolicy none = RestrictionPolicy.none();
    assertFalse(none.hasAnyRestrictions());
    assertFalse(none.isRestricted(RestrictionType.CRAFT));
    assertFalse(none.isRestricted(RestrictionType.USE));

    RestrictionPolicy all = RestrictionPolicy.all();
    assertTrue(all.hasAnyRestrictions());
    assertTrue(all.isRestricted(RestrictionType.CRAFT));
    assertTrue(all.isRestricted(RestrictionType.EQUIP));
    assertTrue(all.isRestricted(RestrictionType.PICKUP));
    assertTrue(all.isRestricted(RestrictionType.INVENTORY_MOVE));
    assertTrue(all.isRestricted(RestrictionType.USE));
  }

  @Test
  void testCustomRestrictions() {
    RestrictionPolicy policy = RestrictionPolicy.of(RestrictionType.CRAFT, RestrictionType.EQUIP);
    assertTrue(policy.hasAnyRestrictions());
    assertTrue(policy.isRestricted(RestrictionType.CRAFT));
    assertTrue(policy.isRestricted(RestrictionType.EQUIP));
    assertFalse(policy.isRestricted(RestrictionType.USE));
    assertFalse(policy.isRestricted(RestrictionType.PICKUP));
  }

  @Test
  void testRestrictionTypeParsing() {
    assertEquals(
        RestrictionType.INVENTORY_MOVE, RestrictionType.fromString("inventory-move").orElseThrow());
    assertEquals(
        RestrictionType.INVENTORY_MOVE, RestrictionType.fromString("INVENTORY_MOVE").orElseThrow());
    assertEquals(RestrictionType.CRAFT, RestrictionType.fromStringOrThrow("craft"));
    assertEquals("craft", RestrictionType.CRAFT.configKey());

    assertFalse(RestrictionType.fromString("fly").isPresent());
    assertThrows(IllegalArgumentException.class, () -> RestrictionType.fromStringOrThrow("fly"));
    assertThrows(NullPointerException.class, () -> RestrictionType.fromStringOrThrow(null));
  }
}
