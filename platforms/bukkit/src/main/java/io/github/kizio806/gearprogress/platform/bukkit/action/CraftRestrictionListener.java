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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to crafting table, player 2x2, and smithing table events to block crafting restricted
 * tiers.
 */
public final class CraftRestrictionListener implements Listener {

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public CraftRestrictionListener(RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPrepareCraft(PrepareItemCraftEvent event) {
    ItemStack result = event.getInventory().getResult();
    if (result == null || result.getAmount() <= 0) {
      return;
    }

    if (event.getView().getPlayer() instanceof Player player) {
      if (feedback.canBypass(player)) {
        return;
      }
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(result);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.CRAFT)) {
      event.getInventory().setResult(null);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onCraft(CraftItemEvent event) {
    ItemStack result = event.getCurrentItem();
    if (result == null || result.getAmount() <= 0) {
      return;
    }

    if (event.getWhoClicked() instanceof Player player) {
      if (feedback.canBypass(player)) {
        return;
      }

      Optional<TierKey> tierKeyOpt = ItemClassifier.classify(result);
      if (tierKeyOpt.isPresent()
          && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.CRAFT)) {
        event.setCancelled(true);
        feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.CRAFT);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPrepareSmithing(PrepareSmithingEvent event) {
    ItemStack result = event.getResult();
    if (result == null || result.getAmount() <= 0) {
      return;
    }

    if (event.getView().getPlayer() instanceof Player player) {
      if (feedback.canBypass(player)) {
        return;
      }
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(result);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.CRAFT)) {
      event.setResult(null);
    }
  }
}
