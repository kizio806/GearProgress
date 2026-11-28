package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ItemClassifier Unit Tests")
class ItemClassifierTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  @Test
  @DisplayName("Correctly classifies armor materials")
  void testArmorClassification() {
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "diamond"),
        ItemClassifier.classify(Material.DIAMOND_CHESTPLATE).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "netherite"),
        ItemClassifier.classify(Material.NETHERITE_BOOTS).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "iron"),
        ItemClassifier.classify(Material.IRON_HELMET).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "golden"),
        ItemClassifier.classify(Material.GOLDEN_LEGGINGS).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "leather"),
        ItemClassifier.classify(Material.LEATHER_HELMET).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "chainmail"),
        ItemClassifier.classify(Material.CHAINMAIL_CHESTPLATE).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ARMOR, "turtle"),
        ItemClassifier.classify(Material.TURTLE_HELMET).orElseThrow());
  }

  @Test
  @DisplayName("Correctly classifies tools and weapons")
  void testToolsAndWeaponsClassification() {
    assertEquals(
        TierKey.of(EquipmentCategory.TOOLS, "diamond"),
        ItemClassifier.classify(Material.DIAMOND_PICKAXE).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.TOOLS, "netherite"),
        ItemClassifier.classify(Material.NETHERITE_AXE).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.TOOLS, "stone"),
        ItemClassifier.classify(Material.STONE_SHOVEL).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.TOOLS, "wooden"),
        ItemClassifier.classify(Material.WOODEN_HOE).orElseThrow());

    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "netherite"),
        ItemClassifier.classify(Material.NETHERITE_SWORD).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "diamond"),
        ItemClassifier.classify(Material.DIAMOND_SWORD).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "bow"),
        ItemClassifier.classify(Material.BOW).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "crossbow"),
        ItemClassifier.classify(Material.CROSSBOW).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "trident"),
        ItemClassifier.classify(Material.TRIDENT).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.WEAPONS, "mace"),
        ItemClassifier.classify(Material.MACE).orElseThrow());
  }

  @Test
  @DisplayName("Correctly classifies shield and elytra")
  void testShieldAndElytraClassification() {
    assertEquals(
        TierKey.of(EquipmentCategory.SHIELD, "default"),
        ItemClassifier.classify(Material.SHIELD).orElseThrow());
    assertEquals(
        TierKey.of(EquipmentCategory.ELYTRA, "default"),
        ItemClassifier.classify(Material.ELYTRA).orElseThrow());
  }

  @Test
  @DisplayName("Returns empty optional for non-gear and air")
  void testNonGearClassification() {
    assertFalse(ItemClassifier.classify((Material) null).isPresent());
    assertFalse(ItemClassifier.classify(Material.AIR).isPresent());
    assertFalse(ItemClassifier.classify(Material.DIRT).isPresent());
    assertFalse(ItemClassifier.classify(Material.APPLE).isPresent());
  }

  @Test
  @DisplayName("Correctly classifies item stack")
  void testItemStackClassification() {
    assertFalse(ItemClassifier.classify((ItemStack) null).isPresent());

    ItemStack mockEmpty = mock(ItemStack.class);
    when(mockEmpty.getAmount()).thenReturn(0);
    assertFalse(ItemClassifier.classify(mockEmpty).isPresent());

    ItemStack mockItem = mock(ItemStack.class);
    when(mockItem.getAmount()).thenReturn(1);
    when(mockItem.getType()).thenReturn(Material.DIAMOND_SWORD);

    Optional<TierKey> result = ItemClassifier.classify(mockItem);
    assertTrue(result.isPresent());
    assertEquals(TierKey.of(EquipmentCategory.WEAPONS, "diamond"), result.get());
  }
}
