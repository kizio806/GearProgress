package io.github.kizio806.gearprogress.core.model.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnchantmentPolicyTest {

  @Test
  void testAllowedAndForbidden() {
    assertTrue(EnchantmentPolicy.allowed().allowEnchantments());
    assertFalse(EnchantmentPolicy.forbidden().allowEnchantments());
    assertTrue(EnchantmentPolicy.of(true).allowEnchantments());
    assertFalse(EnchantmentPolicy.of(false).allowEnchantments());
  }
}
