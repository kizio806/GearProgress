package io.github.kizio806.gearprogress.platform.spigot.command;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import io.github.kizio806.gearprogress.core.time.RelativeDurationParser;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.platform.bukkit.gui.ProgressionGuiView;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** Main command executor and tab completer for /gearprogress on Spigot/Bukkit. */
public final class SpigotMainCommand implements CommandExecutor, TabCompleter {

  private static final List<String> ROOT_SUBCOMMANDS =
      List.of("reload", "unlock", "lock", "timer", "reset", "info", "status", "view", "help");
  private static final List<String> TIMER_SUBCOMMANDS =
      List.of("add", "set", "status", "pause", "resume");

  private final GearProgressApplication application;
  private final SpigotCommandHelper helper;
  private final ProgressionGuiView gui;

  public SpigotMainCommand(GearProgressApplication application, SpigotCommandHelper helper) {
    this(application, helper, new ProgressionGuiView(application));
  }

  public SpigotMainCommand(
      GearProgressApplication application, SpigotCommandHelper helper, ProgressionGuiView gui) {
    this.application = Objects.requireNonNull(application, "application must not be null");
    this.helper = Objects.requireNonNull(helper, "helper must not be null");
    this.gui = Objects.requireNonNull(gui, "gui must not be null");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
      handleHelp(sender);
      return true;
    }

    String sub = args[0].toLowerCase(Locale.ROOT);
    switch (sub) {
      case "reload" -> handleReload(sender);
      case "unlock" -> handleUnlock(sender, args);
      case "lock" -> handleLock(sender, args);
      case "reset" -> handleReset(sender, args);
      case "info" -> handleInfo(sender, args);
      case "status" -> handleStatus(sender);
      case "view" -> handleView(sender);
      case "timer" -> handleTimer(sender, args);
      default -> {
        helper.send(sender, "general.unknown-command");
        return false;
      }
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return helper.matchPrefix(args[0], ROOT_SUBCOMMANDS);
    }

    if (args.length == 2) {
      String sub = args[0].toLowerCase(Locale.ROOT);
      if ("timer".equals(sub)) {
        return helper.matchPrefix(args[1], TIMER_SUBCOMMANDS);
      }
      if (List.of("unlock", "lock", "reset", "info").contains(sub)) {
        return helper.matchPrefix(
            args[1], Arrays.stream(EquipmentCategory.values()).map(EquipmentCategory::id).toList());
      }
    }

    if (args.length == 3) {
      String sub = args[0].toLowerCase(Locale.ROOT);
      if ("timer".equals(sub)) {
        return helper.matchPrefix(
            args[2], Arrays.stream(EquipmentCategory.values()).map(EquipmentCategory::id).toList());
      }
      if (List.of("unlock", "lock", "reset", "info").contains(sub)) {
        return getTiersForCategory(args[1], args[2]);
      }
    }

    if (args.length == 4 && "timer".equalsIgnoreCase(args[0])) {
      return getTiersForCategory(args[2], args[3]);
    }

    if (args.length == 5 && "timer".equalsIgnoreCase(args[0])) {
      String action = args[1].toLowerCase(Locale.ROOT);
      if ("add".equals(action) || "set".equals(action)) {
        return helper.matchPrefix(args[4], List.of("1d", "12h", "1h", "30m", "10m"));
      }
    }

    return Collections.emptyList();
  }

  private void handleHelp(CommandSender sender) {
    if (!helper.requirePermission(sender, "gearprogress.use")) {
      return;
    }
    helper.sendMessage(
        sender, Component.text("=== GearProgress Commands ===", NamedTextColor.GOLD));
    helper.sendMessage(
        sender,
        Component.text(
            "/gearprogress status - View equipment unlock status", NamedTextColor.YELLOW));
    helper.sendMessage(
        sender,
        Component.text(
            "/gearprogress view - Open interactive progression GUI", NamedTextColor.YELLOW));
    helper.sendMessage(
        sender,
        Component.text(
            "/gearprogress info <category> <tier> - View details of a tier",
            NamedTextColor.YELLOW));
    if (sender.hasPermission("gearprogress.reload")
        || sender.hasPermission("gearprogress.admin")
        || sender.isOp()) {
      helper.sendMessage(
          sender,
          Component.text("/gearprogress reload - Reload configuration files", NamedTextColor.AQUA));
    }
    if (sender.hasPermission("gearprogress.admin") || sender.isOp()) {
      helper.sendMessage(
          sender,
          Component.text(
              "/gearprogress unlock <category> <tier> - Force unlock tier", NamedTextColor.GREEN));
      helper.sendMessage(
          sender,
          Component.text(
              "/gearprogress lock <category> <tier> - Force lock tier", NamedTextColor.RED));
      helper.sendMessage(
          sender,
          Component.text(
              "/gearprogress reset <category> <tier> - Reset tier state",
              NamedTextColor.LIGHT_PURPLE));
      helper.sendMessage(
          sender,
          Component.text(
              "/gearprogress timer <add|set|status|pause|resume> ... - Manage timers",
              NamedTextColor.AQUA));
    }
  }

  private void handleReload(CommandSender sender) {
    if (!helper.requirePermission(sender, "gearprogress.reload")) {
      return;
    }
    try {
      application.reload();
      helper.send(sender, "general.plugin-reloaded");
    } catch (Exception ex) {
      helper.sendMessage(
          sender,
          Component.text("Failed to reload configuration: " + ex.getMessage(), NamedTextColor.RED));
    }
  }

  private void handleUnlock(CommandSender sender, String[] args) {
    if (!helper.requirePermission(sender, "gearprogress.admin")) {
      return;
    }
    if (args.length < 3) {
      helper.sendMessage(
          sender,
          Component.text("Usage: /gearprogress unlock <category> <tier>", NamedTextColor.RED));
      return;
    }
    TierKey key = parseKey(args[1], args[2]);
    if (key == null) {
      helper.send(sender, "command.invalid-tier", Map.of("category", args[1], "tier", args[2]));
      return;
    }
    application.progressionService().unlock(key, UnlockMethod.COMMAND, sender.getName()).join();
    helper.send(
        sender,
        "command.unlock.success",
        Map.of("category", key.category().id(), "tier", key.tier()));
  }

  private void handleLock(CommandSender sender, String[] args) {
    if (!helper.requirePermission(sender, "gearprogress.admin")) {
      return;
    }
    if (args.length < 3) {
      helper.sendMessage(
          sender,
          Component.text("Usage: /gearprogress lock <category> <tier>", NamedTextColor.RED));
      return;
    }
    TierKey key = parseKey(args[1], args[2]);
    if (key == null) {
      helper.send(sender, "command.invalid-tier", Map.of("category", args[1], "tier", args[2]));
      return;
    }
    application.progressionService().lock(key, sender.getName()).join();
    helper.send(
        sender,
        "command.lock.success",
        Map.of("category", key.category().id(), "tier", key.tier()));
  }

  private void handleReset(CommandSender sender, String[] args) {
    if (!helper.requirePermission(sender, "gearprogress.admin")) {
      return;
    }
    if (args.length < 3) {
      helper.sendMessage(
          sender,
          Component.text("Usage: /gearprogress reset <category> <tier>", NamedTextColor.RED));
      return;
    }
    TierKey key = parseKey(args[1], args[2]);
    if (key == null) {
      helper.send(sender, "command.invalid-tier", Map.of("category", args[1], "tier", args[2]));
      return;
    }
    application.progressionService().lock(key, sender.getName()).join();
    helper.send(
        sender,
        "command.reset.success",
        Map.of("category", key.category().id(), "tier", key.tier()));
  }

  private void handleInfo(CommandSender sender, String[] args) {
    if (!helper.requirePermission(sender, "gearprogress.use")) {
      return;
    }
    if (args.length < 3) {
      helper.sendMessage(
          sender,
          Component.text("Usage: /gearprogress info <category> <tier>", NamedTextColor.RED));
      return;
    }
    TierKey key = parseKey(args[1], args[2]);
    if (key == null) {
      helper.send(sender, "command.invalid-tier", Map.of("category", args[1], "tier", args[2]));
      return;
    }
    boolean unlocked = application.progressionService().isUnlocked(key);
    Optional<TimeRemaining> remaining = application.timerService().timeRemaining(key);
    helper.sendMessage(
        sender,
        Component.text(
            "--- Tier Info: " + key.category().id() + " / " + key.tier() + " ---",
            NamedTextColor.GOLD));
    helper.sendMessage(
        sender,
        Component.text(
            "Status: " + (unlocked ? "UNLOCKED" : "LOCKED"),
            unlocked ? NamedTextColor.GREEN : NamedTextColor.RED));
    if (!unlocked && remaining.isPresent()) {
      helper.sendMessage(
          sender,
          Component.text(
              "Time Remaining: " + remaining.get().formatHuman(), NamedTextColor.YELLOW));
    }
  }

  private void handleStatus(CommandSender sender) {
    if (!helper.requirePermission(sender, "gearprogress.use")) {
      return;
    }
    helper.sendMessage(
        sender, Component.text("=== GearProgress Progression Status ===", NamedTextColor.GOLD));
    for (EquipmentCategory category : EquipmentCategory.values()) {
      List<EquipmentDefinition> list =
          new ArrayList<>(application.equipmentRegistry().findByCategory(category).values());
      if (list.isEmpty()) {
        continue;
      }
      Component categoryHeader =
          Component.text(category.id().toUpperCase(Locale.ROOT) + ": ", NamedTextColor.AQUA);
      Component line = categoryHeader;
      for (int i = 0; i < list.size(); i++) {
        EquipmentDefinition def = list.get(i);
        boolean unlocked = application.progressionService().isUnlocked(def.key());
        Component tierComp =
            Component.text(def.key().tier(), unlocked ? NamedTextColor.GREEN : NamedTextColor.GRAY);
        line = line.append(tierComp);
        if (i < list.size() - 1) {
          line = line.append(Component.text(" > ", NamedTextColor.DARK_GRAY));
        }
      }
      helper.sendMessage(sender, line);
    }
  }

  private void handleView(CommandSender sender) {
    if (!(sender instanceof Player player)) {
      helper.sendMessage(
          sender,
          Component.text("Only players can open the progression view GUI.", NamedTextColor.RED));
      return;
    }
    if (!helper.requirePermission(sender, "gearprogress.use")) {
      return;
    }
    gui.open(player);
  }

  private void handleTimer(CommandSender sender, String[] args) {
    if (args.length < 2) {
      helper.sendMessage(
          sender,
          Component.text(
              "Usage: /gearprogress timer <add|set|status|pause|resume> ...", NamedTextColor.RED));
      return;
    }
    String action = args[1].toLowerCase(Locale.ROOT);
    if ("status".equals(action)) {
      if (!helper.requirePermission(sender, "gearprogress.use")) {
        return;
      }
      if (args.length < 4) {
        helper.sendMessage(
            sender,
            Component.text(
                "Usage: /gearprogress timer status <category> <tier>", NamedTextColor.RED));
        return;
      }
      TierKey key = parseKey(args[2], args[3]);
      if (key == null) {
        helper.send(sender, "command.invalid-tier", Map.of("category", args[2], "tier", args[3]));
        return;
      }
      Optional<TimeRemaining> remaining = application.timerService().timeRemaining(key);
      boolean paused = application.timerService().isPaused(key);
      String timeText = remaining.map(TimeRemaining::formatHuman).orElse("None / Unlocked");
      helper.sendMessage(
          sender,
          Component.text(
              "Timer for " + key + ": " + timeText + (paused ? " [PAUSED]" : ""),
              NamedTextColor.YELLOW));
      return;
    }

    if (!helper.requirePermission(sender, "gearprogress.admin")) {
      return;
    }

    if ("pause".equals(action)) {
      if (args.length < 4) {
        helper.sendMessage(
            sender,
            Component.text(
                "Usage: /gearprogress timer pause <category> <tier>", NamedTextColor.RED));
        return;
      }
      TierKey key = parseKey(args[2], args[3]);
      if (key == null) {
        helper.send(sender, "command.invalid-tier", Map.of("category", args[2], "tier", args[3]));
        return;
      }
      application.timerService().pause(key);
      helper.send(
          sender,
          "command.timer.paused",
          Map.of("category", key.category().id(), "tier", key.tier()));
      return;
    }

    if ("resume".equals(action)) {
      if (args.length < 4) {
        helper.sendMessage(
            sender,
            Component.text(
                "Usage: /gearprogress timer resume <category> <tier>", NamedTextColor.RED));
        return;
      }
      TierKey key = parseKey(args[2], args[3]);
      if (key == null) {
        helper.send(sender, "command.invalid-tier", Map.of("category", args[2], "tier", args[3]));
        return;
      }
      application.timerService().resume(key);
      helper.send(
          sender,
          "command.timer.resumed",
          Map.of("category", key.category().id(), "tier", key.tier()));
      return;
    }

    if (args.length < 5) {
      helper.sendMessage(
          sender,
          Component.text(
              "Usage: /gearprogress timer " + action + " <category> <tier> <duration>",
              NamedTextColor.RED));
      return;
    }
    TierKey key = parseKey(args[2], args[3]);
    if (key == null) {
      helper.send(sender, "command.invalid-tier", Map.of("category", args[2], "tier", args[3]));
      return;
    }
    Duration duration;
    try {
      duration = RelativeDurationParser.parse(args[4]);
    } catch (IllegalArgumentException ex) {
      helper.sendMessage(
          sender, Component.text("Invalid duration format: " + args[4], NamedTextColor.RED));
      return;
    }

    if ("add".equals(action)) {
      application.timerService().addElapsed(key, duration);
      helper.send(
          sender,
          "command.timer.adjusted",
          Map.of("category", key.category().id(), "tier", key.tier(), "time", args[4]));
      return;
    }
    if ("set".equals(action)) {
      application.timerService().setElapsed(key, duration);
      helper.send(
          sender,
          "command.timer.adjusted",
          Map.of("category", key.category().id(), "tier", key.tier(), "time", args[4]));
      return;
    }

    helper.sendMessage(
        sender,
        Component.text(
            "Unknown timer action. Use: add, set, status, pause, resume", NamedTextColor.RED));
  }

  private TierKey parseKey(String categoryStr, String tierStr) {
    Optional<EquipmentCategory> optCat = EquipmentCategory.fromString(categoryStr);
    if (optCat.isEmpty()) {
      return null;
    }
    TierKey key = TierKey.of(optCat.get(), tierStr);
    if (application.equipmentRegistry().find(key).isEmpty()) {
      return null;
    }
    return key;
  }

  private List<String> getTiersForCategory(String categoryStr, String prefix) {
    Optional<EquipmentCategory> optCat = EquipmentCategory.fromString(categoryStr);
    if (optCat.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> tiers =
        application.equipmentRegistry().findByCategory(optCat.get()).values().stream()
            .map(def -> def.key().tier())
            .toList();
    return helper.matchPrefix(prefix, tiers);
  }
}
