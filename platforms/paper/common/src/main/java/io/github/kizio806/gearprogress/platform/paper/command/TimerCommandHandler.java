package io.github.kizio806.gearprogress.platform.paper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.RelativeDurationParser;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/** Handles /gearprogress timer subcommands. */
public final class TimerCommandHandler {

  private final GearProgressApplication application;
  private final CommandText messages;

  public TimerCommandHandler(GearProgressApplication application, CommandText messages) {
    this.application = Objects.requireNonNull(application, "application must not be null");
    this.messages = Objects.requireNonNull(messages, "messages must not be null");
  }

  public LiteralArgumentBuilder<CommandSourceStack> build(
      SuggestionProvider<CommandSourceStack> categorySuggestions,
      SuggestionProvider<CommandSourceStack> tierSuggestions) {
    return Commands.literal("timer")
        .requires(s -> hasAdminPerm(s.getSender()))
        .then(
            Commands.literal("status")
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .executes(this::executeTimerStatus))))
        .then(
            Commands.literal("pause")
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .executes(this::executeTimerPause))))
        .then(
            Commands.literal("resume")
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .executes(this::executeTimerResume))))
        .then(
            Commands.literal("add")
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .then(
                                    Commands.argument("duration", StringArgumentType.greedyString())
                                        .executes(this::executeTimerAdd)))))
        .then(
            Commands.literal("set")
                .then(
                    Commands.argument("category", StringArgumentType.word())
                        .suggests(categorySuggestions)
                        .then(
                            Commands.argument("tier", StringArgumentType.word())
                                .suggests(tierSuggestions)
                                .then(
                                    Commands.argument("duration", StringArgumentType.greedyString())
                                        .executes(this::executeTimerSet)))));
  }

  private int executeTimerStatus(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    Optional<TierKey> keyOpt = resolveKey(ctx);
    if (keyOpt.isEmpty()) {
      return 0;
    }

    TierKey key = keyOpt.get();
    Optional<TimeRemaining> remainingOpt = application.timerService().timeRemaining(key);
    boolean paused = application.timerService().isPaused(key);

    String timeText = remainingOpt.map(TimeRemaining::formatHuman).orElse("None / Unlocked");
    sender.sendMessage(
        Component.text(
            "Timer for " + key + ": " + timeText + (paused ? " [PAUSED]" : ""),
            NamedTextColor.YELLOW));
    return 1;
  }

  private int executeTimerPause(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    Optional<TierKey> keyOpt = resolveKey(ctx);
    if (keyOpt.isEmpty()) {
      return 0;
    }

    TierKey key = keyOpt.get();
    application.timerService().pause(key);
    messages.sendPrefixed(
        sender,
        "command.timer.paused",
        Map.of("category", key.category().id(), "tier", key.tier()));
    return 1;
  }

  private int executeTimerResume(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    Optional<TierKey> keyOpt = resolveKey(ctx);
    if (keyOpt.isEmpty()) {
      return 0;
    }

    TierKey key = keyOpt.get();
    application.timerService().resume(key);
    messages.sendPrefixed(
        sender,
        "command.timer.resumed",
        Map.of("category", key.category().id(), "tier", key.tier()));
    return 1;
  }

  private int executeTimerAdd(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    Optional<TierKey> keyOpt = resolveKey(ctx);
    if (keyOpt.isEmpty()) {
      return 0;
    }

    String durationText = ctx.getArgument("duration", String.class);
    Duration duration;
    try {
      duration = RelativeDurationParser.parse(durationText);
    } catch (IllegalArgumentException ex) {
      sender.sendMessage(
          Component.text("Invalid duration format: " + durationText, NamedTextColor.RED));
      return 0;
    }

    TierKey key = keyOpt.get();
    application.timerService().addElapsed(key, duration);
    String newRemaining =
        application
            .timerService()
            .timeRemaining(key)
            .map(TimeRemaining::formatHuman)
            .orElse("None");

    messages.sendPrefixed(
        sender,
        "command.timer.adjusted",
        Map.of("category", key.category().id(), "tier", key.tier(), "time", newRemaining));
    return 1;
  }

  private int executeTimerSet(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    Optional<TierKey> keyOpt = resolveKey(ctx);
    if (keyOpt.isEmpty()) {
      return 0;
    }

    String durationText = ctx.getArgument("duration", String.class);
    Duration duration;
    try {
      duration = RelativeDurationParser.parse(durationText);
    } catch (IllegalArgumentException ex) {
      sender.sendMessage(
          Component.text("Invalid duration format: " + durationText, NamedTextColor.RED));
      return 0;
    }

    TierKey key = keyOpt.get();
    application.timerService().setElapsed(key, duration);
    String newRemaining =
        application
            .timerService()
            .timeRemaining(key)
            .map(TimeRemaining::formatHuman)
            .orElse("None");

    messages.sendPrefixed(
        sender,
        "command.timer.adjusted",
        Map.of("category", key.category().id(), "tier", key.tier(), "time", newRemaining));
    return 1;
  }

  private Optional<TierKey> resolveKey(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    String catStr = ctx.getArgument("category", String.class);
    String tier = ctx.getArgument("tier", String.class);

    Optional<EquipmentCategory> catOpt = EquipmentCategory.fromString(catStr);
    if (catOpt.isEmpty()) {
      sender.sendMessage(
          Component.text("Unknown equipment category: " + catStr, NamedTextColor.RED));
      return Optional.empty();
    }

    TierKey key = TierKey.of(catOpt.get(), tier);
    Optional<EquipmentDefinition> defOpt = application.equipmentRegistry().find(key);
    if (defOpt.isEmpty()) {
      sender.sendMessage(Component.text("Equipment tier not found: " + key, NamedTextColor.RED));
      return Optional.empty();
    }
    return Optional.of(key);
  }

  private boolean hasAdminPerm(CommandSender sender) {
    return sender.hasPermission("gearprogress.admin") || sender.isOp();
  }
}
