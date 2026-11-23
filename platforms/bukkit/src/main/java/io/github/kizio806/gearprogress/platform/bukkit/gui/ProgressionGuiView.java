package io.github.kizio806.gearprogress.platform.bukkit.gui;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Builds and displays the interactive progression GUI for viewing unlocked and locked tiers. */
public final class ProgressionGuiView {

  private static final int INVENTORY_SIZE = 54;
  private static final int SUMMARY_SLOT = 49;
  private static final String GUI_TITLE = "§8Gear Progression Status";

  private final GearProgressApplication application;

  /**
   * Constructs the progression GUI view.
   *
   * @param application the application instance
   */
  public ProgressionGuiView(GearProgressApplication application) {
    this.application = Objects.requireNonNull(application, "application must not be null");
  }

  /**
   * Creates and populates the progression view inventory.
   *
   * @return the populated inventory
   */
  public Inventory createInventory() {
    ProgressionGuiHolder holder = new ProgressionGuiHolder();
    Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, GUI_TITLE);
    holder.setInventory(inventory);
    populate(inventory);
    return inventory;
  }

  /**
   * Opens the progression GUI for the specified player.
   *
   * @param player the player to open the GUI for
   */
  public void open(Player player) {
    Objects.requireNonNull(player, "player must not be null");
    Inventory inventory = createInventory();
    player.openInventory(inventory);
  }

  private void populate(Inventory inventory) {
    int slot = 0;
    int unlockedCount = 0;
    int totalCount = 0;

    for (EquipmentDefinition def : application.equipmentRegistry().all().values()) {
      if (slot >= 45) {
        break; // Keep bottom row reserved for navigation / summary
      }

      TierKey key = def.key();
      boolean unlocked = application.progressionService().isUnlocked(key);
      if (unlocked) {
        unlockedCount++;
      }
      totalCount++;

      ItemStack item = createTierItem(def, unlocked);
      inventory.setItem(slot++, item);
    }

    // Fill bottom row with gray decorative panes
    ItemStack filler = createFillerItem();
    for (int i = 45; i < INVENTORY_SIZE; i++) {
      if (i != SUMMARY_SLOT) {
        inventory.setItem(i, filler);
      }
    }

    // Summary item at slot 49
    inventory.setItem(SUMMARY_SLOT, createSummaryItem(unlockedCount, totalCount));
  }

  private ItemStack createTierItem(EquipmentDefinition def, boolean unlocked) {
    TierKey key = def.key();
    Material mat = resolveMaterial(key);
    ItemStack item = new ItemStack(mat);
    ItemMeta meta = item.getItemMeta();

    if (meta != null) {
      String color = unlocked ? "§a§l" : "§c§l";
      meta.setDisplayName(
          color
              + key.category().id().toUpperCase(Locale.ROOT)
              + " - "
              + key.tier().toUpperCase(Locale.ROOT));

      List<String> lore = new ArrayList<>();
      lore.add("§7Status: " + (unlocked ? "§aUNLOCKED" : "§cLOCKED"));

      boolean enchantsAllowed = application.progressionService().isEnchantmentAllowed(key);
      lore.add("§7Enchantments: " + (enchantsAllowed ? "§aAllowed" : "§cBlocked"));

      if (!unlocked) {
        lore.add("§7Unlock Method: §f" + def.unlockCondition().method().name());
        Optional<TimeRemaining> remaining = application.timerService().timeRemaining(key);
        remaining.ifPresent(r -> lore.add("§7Time Remaining: §e" + r.formatHuman()));
      }

      List<String> blocked = new ArrayList<>();
      for (RestrictionType type : RestrictionType.values()) {
        if (def.restrictionPolicy().isRestricted(type)) {
          blocked.add(type.configKey());
        }
      }
      if (!blocked.isEmpty()) {
        lore.add("§7Restrictions: §f" + String.join(", ", blocked));
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
    }
    return item;
  }

  private static ItemStack createFillerItem() {
    ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    ItemMeta meta = pane.getItemMeta();
    if (meta != null) {
      meta.setDisplayName("§r");
      pane.setItemMeta(meta);
    }
    return pane;
  }

  private static ItemStack createSummaryItem(int unlocked, int total) {
    ItemStack star = new ItemStack(Material.NETHER_STAR);
    ItemMeta meta = star.getItemMeta();
    if (meta != null) {
      meta.setDisplayName("§6§lProgression Summary");
      List<String> lore = new ArrayList<>();
      lore.add("§7Unlocked Tiers: §a" + unlocked + " §7/ §f" + total);
      lore.add("§8GearProgress Enterprise Engine");
      meta.setLore(lore);
      star.setItemMeta(meta);
    }
    return star;
  }

  private static Material resolveMaterial(TierKey key) {
    String tier = key.tier().toLowerCase(Locale.ROOT);
    return switch (key.category()) {
      case ARMOR ->
          switch (tier) {
            case "leather" -> Material.LEATHER_CHESTPLATE;
            case "chainmail" -> Material.CHAINMAIL_CHESTPLATE;
            case "iron" -> Material.IRON_CHESTPLATE;
            case "golden" -> Material.GOLDEN_CHESTPLATE;
            case "diamond" -> Material.DIAMOND_CHESTPLATE;
            case "netherite" -> Material.NETHERITE_CHESTPLATE;
            default -> Material.TURTLE_HELMET;
          };
      case TOOLS ->
          switch (tier) {
            case "wooden" -> Material.WOODEN_PICKAXE;
            case "stone" -> Material.STONE_PICKAXE;
            case "iron" -> Material.IRON_PICKAXE;
            case "golden" -> Material.GOLDEN_PICKAXE;
            case "diamond" -> Material.DIAMOND_PICKAXE;
            case "netherite" -> Material.NETHERITE_PICKAXE;
            default -> Material.IRON_SHOVEL;
          };
      case WEAPONS ->
          switch (tier) {
            case "wooden" -> Material.WOODEN_SWORD;
            case "stone" -> Material.STONE_SWORD;
            case "iron" -> Material.IRON_SWORD;
            case "golden" -> Material.GOLDEN_SWORD;
            case "diamond" -> Material.DIAMOND_SWORD;
            case "netherite" -> Material.NETHERITE_SWORD;
            case "bow" -> Material.BOW;
            case "crossbow" -> Material.CROSSBOW;
            case "trident" -> Material.TRIDENT;
            case "mace" -> Material.MACE;
            default -> Material.IRON_AXE;
          };
      case SHIELD -> Material.SHIELD;
      case ELYTRA -> Material.ELYTRA;
      default -> Material.BOOK;
    };
  }
}
