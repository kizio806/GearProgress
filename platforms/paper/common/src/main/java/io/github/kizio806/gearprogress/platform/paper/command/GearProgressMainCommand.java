package io.github.kizio806.gearprogress.platform.paper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.platform.bukkit.gui.ProgressionGuiView;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Main Brigadier command tree root for /gearprogress on the Paper platform. */
public final class GearProgressMainCommand {

  private final GearProgressApplication application;
  private final CommandText messages;
  private final ProgressionCommandHandler progressionHandler;
  private final TimerCommandHandler timerHandler;
  private final ProgressionGuiView gui;

  public GearProgressMainCommand(GearProgressApplication application, LocaleCatalog locales) {
    this(application, locales, new ProgressionGuiView(application));
  }

  public GearProgressMainCommand(
      GearProgressApplication application, LocaleCatalog locales, ProgressionGuiView gui) {
    this.application = Objects.requireNonNull(application, "application must not be null");
    this.messages = new CommandText(Objects.requireNonNull(locales, "locales must not be null"));
    this.gui = Objects.requireNonNull(gui, "gui must not be null");
    this.progressionHandler = new ProgressionCommandHandler(application, messages);
    this.timerHandler = new TimerCommandHandler(application, messages);
  }

  public LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
    SuggestionProvider<CommandSourceStack> categorySuggestions =
        (context, builder) -> {
          for (EquipmentCategory category : EquipmentCategory.values()) {
            builder.suggest(category.id());
          }
          return builder.buildFuture();
        };

    SuggestionProvider<CommandSourceStack> tierSuggestions =
        (context, builder) -> {
          try {
            String catStr = context.getArgument("category", String.class);
            Optional<EquipmentCategory> catOpt = EquipmentCategory.fromString(catStr);
            if (catOpt.isPresent()) {
              for (EquipmentDefinition def : application.equipmentRegistry().all().values()) {
                if (def.key().category() == catOpt.get()) {
                  builder.suggest(def.key().tier());
                }
              }
            }
          } catch (IllegalArgumentException ignored) {
            // Argument not present in context
          }
          return builder.buildFuture();
        };

    return Commands.literal("gearprogress")
        .executes(this::executeHelp)
        .then(Commands.literal("help").executes(this::executeHelp))
        .then(
            Commands.literal("status")
                .requires(s -> hasUsePerm(s.getSender()))
                .executes(this::executeStatus))
        .then(
            Commands.literal("view")
                .requires(s -> hasUsePerm(s.getSender()))
                .executes(this::executeView))
        .then(
            Commands.literal("info")
                .requires(s -> hasUsePerm(s.getSender()))
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .executes(this::executeInfo))))
        .then(
            Commands.literal("reload")
                .requires(s -> hasReloadPerm(s.getSender()))
                .executes(this::executeReload))
        .then(progressionHandler.buildUnlockCommand(categorySuggestions, tierSuggestions))
        .then(progressionHandler.buildLockCommand(categorySuggestions, tierSuggestions))
        .then(progressionHandler.buildResetCommand(categorySuggestions, tierSuggestions))
        .then(timerHandler.build(categorySuggestions, tierSuggestions));
  }

  private int executeHelp(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    messages.send(sender, "command.help.header");
    messages.send(sender, "command.help.status");
    messages.send(sender, "command.help.view");
    messages.send(sender, "command.help.info");
    if (hasAdminPerm(sender)) {
      messages.send(sender, "command.help.unlock");
      messages.send(sender, "command.help.lock");
      messages.send(sender, "command.help.timer");
      messages.send(sender, "command.help.reset");
    }
    if (hasReloadPerm(sender)) {
      messages.send(sender, "command.help.reload");
    }
    return 1;
  }

  private int executeStatus(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    messages.send(sender, "command.status.header");

    String statusUnlocked = messages.message("command.status.status-unlocked");
    String statusLocked = messages.message("command.status.status-locked");

    for (EquipmentDefinition def : application.equipmentRegistry().all().values()) {
      TierKey key = def.key();
      boolean unlocked = application.progressionService().isUnlocked(key);
      boolean enchants = application.progressionService().isEnchantmentAllowed(key);

      Map<String, String> placeholders =
          Map.of(
              "category",
              key.category().id(),
              "tier",
              key.tier(),
              "status",
              unlocked ? statusUnlocked : statusLocked,
              "enchants",
              enchants ? "allowed" : "blocked");

      messages.send(sender, "command.status.entry", placeholders);
    }
    return 1;
  }

  private int executeView(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    if (!(sender instanceof Player player)) {
      sender.sendMessage(
          Component.text("Only players can open the progression view GUI.", NamedTextColor.RED));
      return 0;
    }
    gui.open(player);
    return 1;
  }

  private int executeInfo(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    String catStr = ctx.getArgument("category", String.class);
    String tier = ctx.getArgument("tier", String.class);

    Optional<EquipmentCategory> catOpt = EquipmentCategory.fromString(catStr);
    if (catOpt.isEmpty()) {
      sender.sendMessage(
          Component.text("Unknown equipment category: " + catStr, NamedTextColor.RED));
      return 0;
    }

    TierKey key = TierKey.of(catOpt.get(), tier);
    Optional<EquipmentDefinition> defOpt = application.equipmentRegistry().find(key);
    if (defOpt.isEmpty()) {
      sender.sendMessage(Component.text("Equipment tier not found: " + key, NamedTextColor.RED));
      return 0;
    }

    EquipmentDefinition def = defOpt.get();
    boolean unlocked = application.progressionService().isUnlocked(key);
    Optional<TimeRemaining> remainingOpt = application.timerService().timeRemaining(key);

    String statusUnlocked = messages.message("command.status.status-unlocked");
    String statusLocked = messages.message("command.status.status-locked");

    List<String> blockedActions = new ArrayList<>();
    for (RestrictionType type : RestrictionType.values()) {
      if (def.restrictionPolicy().isRestricted(type)) {
        blockedActions.add(type.configKey());
      }
    }

    Map<String, String> placeholders =
        Map.of(
            "category", key.category().id(),
            "tier", key.tier(),
            "status", unlocked ? statusUnlocked : statusLocked,
            "method", def.unlockCondition().method().name().toLowerCase(java.util.Locale.ROOT),
            "time", remainingOpt.map(TimeRemaining::formatHuman).orElse("None / Unlocked"),
            "allowed", String.valueOf(application.progressionService().isEnchantmentAllowed(key)),
            "restrictions", blockedActions.isEmpty() ? "none" : String.join(", ", blockedActions));

    messages.send(sender, "command.info.header", placeholders);
    messages.send(sender, "command.info.status", placeholders);
    messages.send(sender, "command.info.unlock-method", placeholders);
    messages.send(sender, "command.info.remaining-time", placeholders);
    messages.send(sender, "command.info.enchantments", placeholders);
    messages.send(sender, "command.info.restrictions", placeholders);
    return 1;
  }

  private int executeReload(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    try {
      application.reload();
      messages.sendPrefixed(sender, "general.plugin-reloaded");
      return 1;
    } catch (Exception ex) {
      sender.sendMessage(
          Component.text("Failed to reload configuration: " + ex.getMessage(), NamedTextColor.RED));
      return 0;
    }
  }

  private boolean hasUsePerm(CommandSender sender) {
    return sender.hasPermission("gearprogress.use")
        || sender.hasPermission("gearprogress.admin")
        || sender.isOp();
  }

  private boolean hasReloadPerm(CommandSender sender) {
    return sender.hasPermission("gearprogress.reload")
        || sender.hasPermission("gearprogress.admin")
        || sender.isOp();
  }

  private boolean hasAdminPerm(CommandSender sender) {
    return sender.hasPermission("gearprogress.admin") || sender.isOp();
  }
}
