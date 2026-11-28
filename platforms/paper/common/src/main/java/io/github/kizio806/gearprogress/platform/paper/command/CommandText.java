package io.github.kizio806.gearprogress.platform.paper.command;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/** Converts command locale entries to Adventure components via MiniMessage. */
public final class CommandText {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private final LocaleCatalog locales;

  public CommandText(LocaleCatalog locales) {
    this.locales = Objects.requireNonNull(locales, "locales must not be null");
  }

  public String message(String key) {
    return locales.message(key);
  }

  public String message(String key, Map<String, ?> placeholders) {
    return locales.message(key, placeholders);
  }

  public Component component(String key) {
    return MINI_MESSAGE.deserialize(locales.message(key));
  }

  public Component component(String key, Map<String, ?> placeholders) {
    return MINI_MESSAGE.deserialize(locales.message(key, placeholders));
  }

  public void send(CommandSender sender, String key) {
    sender.sendMessage(component(key));
  }

  public void send(CommandSender sender, String key, Map<String, ?> placeholders) {
    sender.sendMessage(component(key, placeholders));
  }

  public void sendPrefixed(CommandSender sender, String key) {
    Component prefix = component("general.prefix");
    sender.sendMessage(prefix.append(component(key)));
  }

  public void sendPrefixed(CommandSender sender, String key, Map<String, ?> placeholders) {
    Component prefix = component("general.prefix");
    sender.sendMessage(prefix.append(component(key, placeholders)));
  }
}
