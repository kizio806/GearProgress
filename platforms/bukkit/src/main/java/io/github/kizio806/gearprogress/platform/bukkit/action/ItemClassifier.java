package io.github.kizio806.gearprogress.platform.bukkit.action;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * High-performance classifier mapping Bukkit {@link Material} and {@link ItemStack}s to their
 * corresponding {@link TierKey}.
 */
public final class ItemClassifier {

  private static final Map<Material, TierKey> MATERIAL_TIER_MAP = new EnumMap<>(Material.class);

  static {
    // Armor: Leather
    register(
        EquipmentCategory.ARMOR,
        "leather",
        Material.LEATHER_HELMET,
        Material.LEATHER_CHESTPLATE,
        Material.LEATHER_LEGGINGS,
        Material.LEATHER_BOOTS);
    // Armor: Chainmail
    register(
        EquipmentCategory.ARMOR,
        "chainmail",
        Material.CHAINMAIL_HELMET,
        Material.CHAINMAIL_CHESTPLATE,
        Material.CHAINMAIL_LEGGINGS,
        Material.CHAINMAIL_BOOTS);
    // Armor: Iron
    register(
        EquipmentCategory.ARMOR,
        "iron",
        Material.IRON_HELMET,
        Material.IRON_CHESTPLATE,
        Material.IRON_LEGGINGS,
        Material.IRON_BOOTS);
    // Armor: Golden
    register(
        EquipmentCategory.ARMOR,
        "golden",
        Material.GOLDEN_HELMET,
        Material.GOLDEN_CHESTPLATE,
        Material.GOLDEN_LEGGINGS,
        Material.GOLDEN_BOOTS);
    // Armor: Diamond
    register(
        EquipmentCategory.ARMOR,
        "diamond",
        Material.DIAMOND_HELMET,
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_BOOTS);
    // Armor: Netherite
    register(
        EquipmentCategory.ARMOR,
        "netherite",
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS);
    // Armor: Turtle
    register(EquipmentCategory.ARMOR, "turtle", Material.TURTLE_HELMET);

    // Tools: Wooden
    register(
        EquipmentCategory.TOOLS,
        "wooden",
        Material.WOODEN_PICKAXE,
        Material.WOODEN_AXE,
        Material.WOODEN_SHOVEL,
        Material.WOODEN_HOE);
    // Tools: Stone
    register(
        EquipmentCategory.TOOLS,
        "stone",
        Material.STONE_PICKAXE,
        Material.STONE_AXE,
        Material.STONE_SHOVEL,
        Material.STONE_HOE);
    // Tools: Iron
    register(
        EquipmentCategory.TOOLS,
        "iron",
        Material.IRON_PICKAXE,
        Material.IRON_AXE,
        Material.IRON_SHOVEL,
        Material.IRON_HOE);
    // Tools: Golden
    register(
        EquipmentCategory.TOOLS,
        "golden",
        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_AXE,
        Material.GOLDEN_SHOVEL,
        Material.GOLDEN_HOE);
    // Tools: Diamond
    register(
        EquipmentCategory.TOOLS,
        "diamond",
        Material.DIAMOND_PICKAXE,
        Material.DIAMOND_AXE,
        Material.DIAMOND_SHOVEL,
        Material.DIAMOND_HOE);
    // Tools: Netherite
    register(
        EquipmentCategory.TOOLS,
        "netherite",
        Material.NETHERITE_PICKAXE,
        Material.NETHERITE_AXE,
        Material.NETHERITE_SHOVEL,
        Material.NETHERITE_HOE);

    // Weapons: Wooden
    register(EquipmentCategory.WEAPONS, "wooden", Material.WOODEN_SWORD);
    // Weapons: Stone
    register(EquipmentCategory.WEAPONS, "stone", Material.STONE_SWORD);
    // Weapons: Iron
    register(EquipmentCategory.WEAPONS, "iron", Material.IRON_SWORD);
    // Weapons: Golden
    register(EquipmentCategory.WEAPONS, "golden", Material.GOLDEN_SWORD);
    // Weapons: Diamond
    register(EquipmentCategory.WEAPONS, "diamond", Material.DIAMOND_SWORD);
    // Weapons: Netherite
    register(EquipmentCategory.WEAPONS, "netherite", Material.NETHERITE_SWORD);
    // Special weapons
    register(EquipmentCategory.WEAPONS, "bow", Material.BOW);
    register(EquipmentCategory.WEAPONS, "crossbow", Material.CROSSBOW);
    register(EquipmentCategory.WEAPONS, "trident", Material.TRIDENT);
    register(EquipmentCategory.WEAPONS, "mace", Material.MACE);

    // Shield
    register(EquipmentCategory.SHIELD, "default", Material.SHIELD);

    // Elytra
    register(EquipmentCategory.ELYTRA, "default", Material.ELYTRA);
  }

  private static void register(EquipmentCategory category, String tier, Material... materials) {
    TierKey key = TierKey.of(category, tier);
    for (Material material : materials) {
      MATERIAL_TIER_MAP.put(material, key);
    }
  }

  private ItemClassifier() {}

  /**
   * Resolves the {@link TierKey} for a given material.
   *
   * @param material the item material
   * @return optional TierKey
   */
  public static Optional<TierKey> classify(Material material) {
    if (material == null || isAir(material)) {
      return Optional.empty();
    }
    return Optional.ofNullable(MATERIAL_TIER_MAP.get(material));
  }

  /**
   * Resolves the {@link TierKey} for a given ItemStack.
   *
   * @param item the item stack
   * @return optional TierKey
   */
  public static Optional<TierKey> classify(ItemStack item) {
    if (item == null || item.getAmount() <= 0) {
      return Optional.empty();
    }
    return classify(item.getType());
  }

  private static boolean isAir(Material material) {
    return material == Material.AIR
        || material == Material.CAVE_AIR
        || material == Material.VOID_AIR;
  }
}
