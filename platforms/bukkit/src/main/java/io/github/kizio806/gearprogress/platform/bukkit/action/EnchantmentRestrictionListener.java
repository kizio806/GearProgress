package io.github.kizio806.gearprogress.platform.bukkit.action;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to enchantment and anvil events to enforce enchantment restrictions per equipment tier.
 */
public final class EnchantmentRestrictionListener implements Listener {

  private final RestrictionQueryService queryService;
  private final FeedbackNotifier feedback;

  public EnchantmentRestrictionListener(
      RestrictionQueryService queryService, FeedbackNotifier feedback) {
    this.queryService = Objects.requireNonNull(queryService, "queryService must not be null");
    this.feedback = Objects.requireNonNull(feedback, "feedback must not be null");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
    Player player = event.getEnchanter();
    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack item = event.getItem();
    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent() && queryService.isEnchantmentForbidden(tierKeyOpt.get())) {
      event.setCancelled(true);
      feedback.notifyEnchantmentForbidden(player, tierKeyOpt.get());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEnchantItem(EnchantItemEvent event) {
    Player player = event.getEnchanter();
    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack item = event.getItem();
    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(item);
    if (tierKeyOpt.isPresent() && queryService.isEnchantmentForbidden(tierKeyOpt.get())) {
      event.setCancelled(true);
      feedback.notifyEnchantmentForbidden(player, tierKeyOpt.get());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    if (!(event.getView().getPlayer() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack result = event.getResult();
    if (result == null || result.getAmount() <= 0) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(result);
    if (tierKeyOpt.isPresent() && queryService.isEnchantmentForbidden(tierKeyOpt.get())) {
      // If result has enchantments, block it
      if (!result.getEnchantments().isEmpty()) {
        event.setResult(null);
        feedback.notifyEnchantmentForbidden(player, tierKeyOpt.get());
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPrepareSmithing(PrepareSmithingEvent event) {
    if (!(event.getView().getPlayer() instanceof Player player)) {
      return;
    }

    if (feedback.canBypass(player)) {
      return;
    }

    ItemStack result = event.getResult();
    if (result == null || result.getAmount() <= 0) {
      return;
    }

    Optional<TierKey> tierKeyOpt = ItemClassifier.classify(result);
    if (tierKeyOpt.isPresent() && queryService.isEnchantmentForbidden(tierKeyOpt.get())) {
      // If upgrading carries over enchantments to a tier where enchantments are forbidden, block it
      if (!result.getEnchantments().isEmpty()) {
        event.setResult(null);
        feedback.notifyEnchantmentForbidden(player, tierKeyOpt.get());
      }
    }
  }
}
