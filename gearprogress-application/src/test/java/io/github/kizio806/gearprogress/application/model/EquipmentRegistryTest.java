package io.github.kizio806.gearprogress.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import java.util.List;
import org.junit.jupiter.api.Test;

class EquipmentRegistryTest {

  @Test
  void testRegistryOperations() {
    EquipmentRegistry registry = new EquipmentRegistry();
    assertEquals(0, registry.size());

    TierKey ironArmor = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierKey ironTool = TierKey.of(EquipmentCategory.TOOLS, "iron");

    EquipmentDefinition def1 =
        new EquipmentDefinition(
            ironArmor,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed());
    EquipmentDefinition def2 =
        new EquipmentDefinition(
            diamondArmor,
            UnlockCondition.command(),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden());
    EquipmentDefinition def3 =
        new EquipmentDefinition(
            ironTool,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed());

    registry.replaceAll(List.of(def1, def2, def3));
    assertEquals(3, registry.size());

    assertTrue(registry.find(ironArmor).isPresent());
    assertFalse(registry.find(TierKey.of(EquipmentCategory.SHIELD, "default")).isPresent());

    var armorDefs = registry.findByCategory(EquipmentCategory.ARMOR);
    assertEquals(2, armorDefs.size());
    assertTrue(armorDefs.containsKey(ironArmor));
    assertTrue(armorDefs.containsKey(diamondArmor));

    assertEquals(3, registry.all().size());

    registry.clear();
    assertEquals(0, registry.size());
  }
}
