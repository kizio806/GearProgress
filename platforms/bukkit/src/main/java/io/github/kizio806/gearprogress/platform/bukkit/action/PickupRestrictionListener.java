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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to entity pickup events to prevent players from picking up restricted equipment tiers.
 */
public final class PickupRestrictionListener implements Listener {

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public PickupRestrictionListener(
      RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityPickup(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack item = event.getItem().getItemStack();
    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.PICKUP)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.PICKUP);
    }
  }
}
