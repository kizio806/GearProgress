package io.github.kizio806.gearprogress.platform.bukkit.action;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to inventory and item movement events to block moving restricted equipment tiers.
 *
 * <p><b>Validation Scope:</b> Validates current slot contents, cursor items, hotbar number-key
 * swaps, offhand swaps, and item drops to prevent moving restricted items.
 */
public final class InventoryMoveRestrictionListener implements Listener {

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public InventoryMoveRestrictionListener(
      RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack current = event.getCurrentItem();
    if (current != null && current.getAmount() > 0) {
      Optional<TierKey> tierKeyOpt = ItemClassifier.classify(current);
      if (tierKeyOpt.isPresent()
          && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
        event.setCancelled(true);
        feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
        return;
      }
    }

    ItemStack cursor = event.getCursor();
    if (cursor != null && cursor.getAmount() > 0) {
      Optional<TierKey> tierKeyOpt = ItemClassifier.classify(cursor);
      if (tierKeyOpt.isPresent()
          && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
        event.setCancelled(true);
        feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
        return;
      }
    }

    if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() >= 0) {
      ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
      if (hotbarItem != null && hotbarItem.getAmount() > 0) {
        Optional<TierKey> tierKeyOpt = ItemClassifier.classify(hotbarItem);
        if (tierKeyOpt.isPresent()
            && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
          event.setCancelled(true);
          feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
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

    ItemStack cursor = event.getOldCursor();
    if (cursor == null || cursor.getAmount() <= 0) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(cursor);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack item = event.getItemDrop().getItemStack();
    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack main = event.getMainHandItem();
    if (main != null && main.getAmount() > 0) {
      Optional<TierKey> tierKeyOpt = ItemClassifier.classify(main);
      if (tierKeyOpt.isPresent()
          && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
        event.setCancelled(true);
        feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
        return;
      }
    }

    ItemStack off = event.getOffHandItem();
    if (off != null && off.getAmount() > 0) {
      Optional<TierKey> tierKeyOpt = ItemClassifier.classify(off);
      if (tierKeyOpt.isPresent()
          && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE)) {
        event.setCancelled(true);
        feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.INVENTORY_MOVE);
      }
    }
  }
}
