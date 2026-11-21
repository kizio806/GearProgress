package io.github.kizio806.gearprogress.platform.bukkit.action;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to player interactions and inventory clicks to prevent equipping restricted equipment
 * tiers.
 */
public final class EquipRestrictionListener implements Listener {

  private static final Set<Integer> ARMOR_RAW_SLOTS = Set.of(5, 6, 7, 8);

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public EquipRestrictionListener(RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR
        && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    ItemStack item = event.getItem();
    if (item == null || item.getAmount() <= 0) {
      return;
    }

    Player player = event.getPlayer();
    if (feedback.canBypass(player)) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isEmpty()) {
      return;
    }

    TierKey key = tierKeyOpt.get();
    if (key.category() != EquipmentCategory.ARMOR && key.category() != EquipmentCategory.ELYTRA) {
      return;
    }

    if (queryService.isActionRestricted(key, RestrictionType.EQUIP)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, key, RestrictionType.EQUIP);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    // Direct placement into armor slot
    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
      ItemStack itemToEquip = event.getCursor();
      if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() >= 0) {
        itemToEquip = player.getInventory().getItem(event.getHotbarButton());
      }

      if (itemToEquip != null && itemToEquip.getAmount() > 0) {
        Optional<TierKey> tierKeyOpt = ItemClassifier.classify(itemToEquip);
        if (tierKeyOpt.isPresent()
            && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.EQUIP)) {
          event.setCancelled(true);
          feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.EQUIP);
          return;
        }
      }
    }

    // Shift click from player inventory towards armor slot
    if (event.isShiftClick()) {
      ItemStack current = event.getCurrentItem();
      if (current != null && current.getAmount() > 0) {
        Optional<TierKey> tierKeyOpt = ItemClassifier.classify(current);
        if (tierKeyOpt.isPresent()) {
          TierKey key = tierKeyOpt.get();
          if (key.category() == EquipmentCategory.ARMOR
              || key.category() == EquipmentCategory.ELYTRA) {
            if (queryService.isActionRestricted(key, RestrictionType.EQUIP)) {
              event.setCancelled(true);
              feedback.notifyRestricted(player, key, RestrictionType.EQUIP);
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    boolean targetsArmorSlot = false;
    for (int slot : event.getRawSlots()) {
      if (ARMOR_RAW_SLOTS.contains(slot)) {
        targetsArmorSlot = true;
        break;
      }
    }

    if (!targetsArmorSlot) {
      return;
    }

    ItemStack cursor = event.getOldCursor();
    if (cursor == null || cursor.getAmount() <= 0) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(cursor);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.EQUIP)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.EQUIP);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDispenseArmor(BlockDispenseArmorEvent event) {
    if (!(event.getTargetEntity() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack item = event.getItem();
    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.EQUIP)) {
      event.setCancelled(true);
    }
  }
}
