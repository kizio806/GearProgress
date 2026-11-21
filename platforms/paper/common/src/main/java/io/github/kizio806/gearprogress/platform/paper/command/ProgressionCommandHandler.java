package io.github.kizio806.gearprogress.platform.paper.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/** Handles unlock, lock, and reset commands for GearProgress. */
public final class ProgressionCommandHandler {

  private final GearProgressApplication application;
  private final CommandText messages;

  public ProgressionCommandHandler(GearProgressApplication application, CommandText messages) {
    this.application = Objects.requireNonNull(application, "application must not be null");
    this.messages = Objects.requireNonNull(messages, "messages must not be null");
  }

  public LiteralArgumentBuilder<CommandSourceStack> buildUnlockCommand(
      SuggestionProvider<CommandSourceStack> categorySuggestions,
      SuggestionProvider<CommandSourceStack> tierSuggestions) {
    return Commands.literal("unlock")
        .requires(s -> hasAdminPerm(s.getSender()))
        .then(
            Commands.argument("category", StringArgumentType.word())
                .suggests(categorySuggestions)
                .then(
                    Commands.argument("tier", StringArgumentType.word())
                        .suggests(tierSuggestions)
                        .executes(this::executeUnlock)));
  }

  public LiteralArgumentBuilder<CommandSourceStack> buildLockCommand(
      SuggestionProvider<CommandSourceStack> categorySuggestions,
      SuggestionProvider<CommandSourceStack> tierSuggestions) {
    return Commands.literal("lock")
        .requires(s -> hasAdminPerm(s.getSender()))
        .then(
            Commands.argument("category", StringArgumentType.word())
                .suggests(categorySuggestions)
                .then(
                    Commands.argument("tier", StringArgumentType.word())
                        .suggests(tierSuggestions)
                        .executes(this::executeLock)));
  }

  public LiteralArgumentBuilder<CommandSourceStack> buildResetCommand(
      SuggestionProvider<CommandSourceStack> categorySuggestions,
      SuggestionProvider<CommandSourceStack> tierSuggestions) {
    return Commands.literal("reset")
        .requires(s -> hasAdminPerm(s.getSender()))
        .then(Commands.literal("all").executes(this::executeResetAll))
        .then(
            Commands.argument("category", StringArgumentType.word())
                .suggests(categorySuggestions)
                .then(
                    Commands.argument("tier", StringArgumentType.word())
                        .suggests(tierSuggestions)
                        .executes(this::executeResetSingle)));
  }

  private int executeUnlock(CommandContext<CommandSourceStack> ctx) {
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

    Map<String, String> placeholders = Map.of("category", key.category().id(), "tier", key.tier());
    if (application.progressionService().isUnlocked(key)) {
      messages.sendPrefixed(sender, "command.unlock.already-unlocked", placeholders);
      return 0;
    }

    application.progressionService().unlock(key, UnlockMethod.COMMAND, sender.getName()).join();
    messages.sendPrefixed(sender, "command.unlock.success", placeholders);
    return 1;
  }

  private int executeLock(CommandContext<CommandSourceStack> ctx) {
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

    Map<String, String> placeholders = Map.of("category", key.category().id(), "tier", key.tier());
    if (!application.progressionService().isUnlocked(key)) {
      messages.sendPrefixed(sender, "command.lock.already-locked", placeholders);
      return 0;
    }

    application.progressionService().lock(key, sender.getName()).join();
    messages.sendPrefixed(sender, "command.lock.success", placeholders);
    return 1;
  }

  private int executeResetAll(CommandContext<CommandSourceStack> ctx) {
    CommandSender sender = ctx.getSource().getSender();
    try {
      application.progressionService().initialize().join();
      messages.sendPrefixed(sender, "command.reset.success", Map.of("target", "all categories"));
      return 1;
    } catch (Exception ex) {
      sender.sendMessage(Component.text("Reset failed: " + ex.getMessage(), NamedTextColor.RED));
      return 0;
    }
  }

  private int executeResetSingle(CommandContext<CommandSourceStack> ctx) {
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

    // Reset single tier to its default config state
    boolean initialUnlocked = defOpt.get().unlockCondition() instanceof UnlockCondition.Always;
    if (initialUnlocked) {
      application.progressionService().unlock(key, UnlockMethod.ALWAYS, "RESET").join();
    } else {
      application.progressionService().lock(key, "RESET").join();
    }
    messages.sendPrefixed(sender, "command.reset.success", Map.of("target", key.toString()));
    return 1;
  }

  private boolean hasAdminPerm(CommandSender sender) {
    return sender.hasPermission("gearprogress.admin") || sender.isOp();
  }
}
