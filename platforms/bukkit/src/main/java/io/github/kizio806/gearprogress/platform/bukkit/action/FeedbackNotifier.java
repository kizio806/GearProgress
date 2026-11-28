package io.github.kizio806.gearprogress.platform.bukkit.action;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import io.github.kizio806.gearprogress.application.config.spec.PluginConfigSpec;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Dispatches localized MiniMessage notifications, action bar alerts, and sound effects to players
 * with cooldown throttling to prevent event spam.
 */
public final class FeedbackNotifier {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final long COOLDOWN_MILLIS = 1500L;

  private final PluginConfigSpec config;
  private final LocaleCatalog locales;
  private final Function<Player, Audience> audienceProvider;
  private final Map<UUID, Long> lastNotificationTimestamps = new ConcurrentHashMap<>();

  public FeedbackNotifier(PluginConfigSpec config, LocaleCatalog locales) {
    this(config, locales, player -> (Audience) player);
  }

  public FeedbackNotifier(
      PluginConfigSpec config, LocaleCatalog locales, Function<Player, Audience> audienceProvider) {
    this.config = Objects.requireNonNull(config, "config must not be null");
    this.locales = Objects.requireNonNull(locales, "locales must not be null");
    this.audienceProvider =
        Objects.requireNonNull(audienceProvider, "audienceProvider must not be null");
  }

  /**
   * Checks whether the player has bypass permission.
   *
   * @param player the player
   * @return true if bypassed
   */
  public boolean canBypass(Player player) {
    if (player == null) {
      return false;
    }
    if (!config.enableBypass()) {
      return false;
    }
    return player.hasPermission(config.bypassPermission()) || player.isOp();
  }

  /**
   * Sends restriction feedback (action bar, chat, sound) to the player.
   *
   * @param player the player
   * @param key the tier key of the item
   * @param action the restriction type
   */
  public void notifyRestricted(Player player, TierKey key, RestrictionType action) {
    if (player == null || isThrottled(player.getUniqueId())) {
      return;
    }

    String messageKey = "blocking." + action.configKey();
    Map<String, String> placeholders =
        Map.of(
            "category", key.category().id(),
            "tier", key.tier());

    String rawText = locales.message(messageKey, placeholders);
    Component component = MINI_MESSAGE.deserialize(rawText);

    Audience audience = audienceProvider.apply(player);
    if (config.actionBarOnBlocked()) {
      audience.sendActionBar(component);
    }
    if (config.chatMessageOnBlocked()) {
      String prefix = locales.message("general.prefix");
      audience.sendMessage(MINI_MESSAGE.deserialize(prefix).append(component));
    }
    playSound(player, config.blockedSound());
  }

  /**
   * Sends enchantment restriction feedback to the player.
   *
   * @param player the player
   * @param key the tier key of the item
   */
  public void notifyEnchantmentForbidden(Player player, TierKey key) {
    if (player == null || isThrottled(player.getUniqueId())) {
      return;
    }

    String messageKey = "blocking.enchantment";
    Map<String, String> placeholders =
        Map.of(
            "category", key.category().id(),
            "tier", key.tier());

    String rawText = locales.message(messageKey, placeholders);
    Component component = MINI_MESSAGE.deserialize(rawText);

    Audience audience = audienceProvider.apply(player);
    if (config.actionBarOnBlocked()) {
      audience.sendActionBar(component);
    }
    if (config.chatMessageOnBlocked()) {
      String prefix = locales.message("general.prefix");
      audience.sendMessage(MINI_MESSAGE.deserialize(prefix).append(component));
    }
    playSound(player, config.blockedSound());
  }

  private boolean isThrottled(UUID playerId) {
    long now = System.currentTimeMillis();
    Long previous = lastNotificationTimestamps.put(playerId, now);
    if (previous != null && (now - previous) < COOLDOWN_MILLIS) {
      lastNotificationTimestamps.put(playerId, previous);
      return true;
    }
    return false;
  }

  private void playSound(Player player, String soundKey) {
    if (soundKey == null || soundKey.isBlank()) {
      return;
    }
    try {
      NamespacedKey key = NamespacedKey.fromString(soundKey);
      if (key != null) {
        Sound sound = Registry.SOUNDS.get(key);
        if (sound != null) {
          player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
      }
    } catch (Exception | LinkageError ignored) {
      if (player.getServer() != null && player.getServer().getLogger() != null) {
        player
            .getServer()
            .getLogger()
            .log(java.util.logging.Level.FINEST, "Could not play sound: " + soundKey, ignored);
      }
    }
  }
}
