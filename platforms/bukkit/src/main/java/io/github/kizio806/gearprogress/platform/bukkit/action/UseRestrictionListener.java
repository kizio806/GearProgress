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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/** Listens to player interactions and attacks to block using restricted equipment tiers. */
public final class UseRestrictionListener implements Listener {

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public UseRestrictionListener(RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item == null || item.getAmount() <= 0) {
      return;
    }

    Player player = event.getPlayer();
    if (feedback.canBypass(player)) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.USE)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.USE);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack weapon = player.getInventory().getItemInMainHand();
    if (weapon == null || weapon.getAmount() <= 0) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(weapon);
    if (tierKeyOpt.isPresent()
        && queryService.isActionRestricted(tierKeyOpt.get(), RestrictionType.USE)) {
      event.setCancelled(true);
      feedback.notifyRestricted(player, tierKeyOpt.get(), RestrictionType.USE);
    }
  }
}
