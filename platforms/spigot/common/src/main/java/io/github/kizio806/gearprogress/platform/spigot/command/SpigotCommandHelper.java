package io.github.kizio806.gearprogress.platform.spigot.command;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Helper for Spigot command dispatching, permission checking, Adventure messaging, and tab
 * completion.
 */
public final class SpigotCommandHelper {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private final BukkitAudiences adventure;
  private final LocaleCatalog locales;

  public SpigotCommandHelper(BukkitAudiences adventure, LocaleCatalog locales) {
    this.adventure = Objects.requireNonNull(adventure, "adventure must not be null");
    this.locales = Objects.requireNonNull(locales, "locales must not be null");
  }

  public LocaleCatalog locales() {
    return locales;
  }

  public void send(CommandSender sender, String key) {
    String message = locales.message(key);
    Component component = MINI_MESSAGE.deserialize(message);
    adventure.sender(sender).sendMessage(component);
  }

  public void send(CommandSender sender, String key, Map<String, String> placeholders) {
    String message = locales.message(key, placeholders);
    Component component = MINI_MESSAGE.deserialize(message);
    adventure.sender(sender).sendMessage(component);
  }

  public void sendMessage(CommandSender sender, Component component) {
    adventure.sender(sender).sendMessage(component);
  }

  public boolean requirePermission(CommandSender sender, String permission) {
    if (!sender.hasPermission(permission)
        && !sender.hasPermission("gearprogress.admin")
        && !sender.isOp()) {
      send(sender, "general.no-permission");
      return false;
    }
    return true;
  }

  public List<String> matchPrefix(String prefix, Collection<String> candidates) {
    String lower = prefix.toLowerCase(Locale.ROOT);
    return candidates.stream()
        .filter(c -> c.toLowerCase(Locale.ROOT).startsWith(lower))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
