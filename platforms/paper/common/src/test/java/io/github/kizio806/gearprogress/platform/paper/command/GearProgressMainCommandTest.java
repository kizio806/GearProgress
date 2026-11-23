package io.github.kizio806.gearprogress.platform.paper.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.CommandDispatcher;
import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.service.ProgressionService;
import io.github.kizio806.gearprogress.application.service.TimerService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.platform.bukkit.gui.ProgressionGuiView;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GearProgressMainCommand Unit Tests")
class GearProgressMainCommandTest {

  private GearProgressApplication application;
  private ProgressionService progressionService;
  private TimerService timerService;
  private EquipmentRegistry registry;
  private LocaleCatalog locales;

  private List<Component> messages;
  private CommandSourceStack source;
  private CommandDispatcher<CommandSourceStack> dispatcher;
  private GearProgressMainCommand mainCommand;

  @BeforeEach
  void setUp() {
    application = mock(GearProgressApplication.class);
    progressionService = mock(ProgressionService.class);
    timerService = mock(TimerService.class);
    registry = new EquipmentRegistry();

    when(application.progressionService()).thenReturn(progressionService);
    when(application.timerService()).thenReturn(timerService);
    when(application.equipmentRegistry()).thenReturn(registry);

    Map<String, String> map = new HashMap<>();
    map.put("general.prefix", "<gray>[GP]</gray> ");
    map.put("general.plugin-reloaded", "Reloaded successfully.");
    map.put("command.help.header", "Help Header");
    map.put("command.help.status", "Status Help");
    map.put("command.help.view", "View Help");
    map.put("command.help.info", "Info Help");
    map.put("command.help.unlock", "Unlock Help");
    map.put("command.help.lock", "Lock Help");
    map.put("command.help.timer", "Timer Help");
    map.put("command.help.reset", "Reset Help");
    map.put("command.help.reload", "Reload Help");
    map.put("command.status.header", "Status Header");
    map.put("command.status.entry", "Entry: %tier%");
    map.put("command.status.status-unlocked", "UNLOCKED");
    map.put("command.status.status-locked", "LOCKED");
    map.put("command.info.header", "Info Header");
    map.put("command.info.status", "Info Status");
    map.put("command.info.unlock-method", "Method");
    map.put("command.info.remaining-time", "Remaining");
    map.put("command.info.enchantments", "Enchantments");
    map.put("command.info.restrictions", "Restrictions");
    map.put("command.unlock.success", "Unlocked %tier%");
    map.put("command.unlock.already-unlocked", "Already unlocked %tier%");
    map.put("command.lock.success", "Locked %tier%");
    map.put("command.lock.already-locked", "Already locked %tier%");
    map.put("command.reset.success", "Reset %target%");
    map.put("command.timer.paused", "Timer paused");
    map.put("command.timer.resumed", "Timer resumed");
    map.put("command.timer.adjusted", "Timer adjusted");

    locales = LocaleCatalog.of(map);
    when(application.localeCatalog()).thenReturn(locales);

    // Register some test tiers
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    registry.register(
        new EquipmentDefinition(
            diamondArmor,
            UnlockCondition.timer(Duration.ofDays(7)),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden()));

    TierKey ironTools = TierKey.of(EquipmentCategory.TOOLS, "iron");
    registry.register(
        new EquipmentDefinition(
            ironTools,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed()));

    mainCommand = new GearProgressMainCommand(application, locales);

    messages = new ArrayList<>();
    CommandSender sender = PaperCommandTestSupport.createRecordingSender(messages);
    source = PaperCommandTestSupport.createMockSource(sender);

    dispatcher = PaperCommandTestSupport.createDispatcher();
    dispatcher.register(mainCommand.buildCommand());
  }

  @Test
  @DisplayName("Executing /gearprogress and /gearprogress help sends help messages")
  void testHelp() throws Exception {
    dispatcher.execute("gearprogress", source);
    assertFalse(messages.isEmpty());

    messages.clear();
    dispatcher.execute("gearprogress help", source);
    assertFalse(messages.isEmpty());
  }

  @Test
  @DisplayName("Executing /gearprogress status displays tier status entries")
  void testStatus() throws Exception {
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(progressionService.isUnlocked(diamondArmor)).thenReturn(false);

    dispatcher.execute("gearprogress status", source);
    assertFalse(messages.isEmpty());
  }

  @Test
  @DisplayName("Executing /gearprogress info displays detailed tier information")
  void testInfo() throws Exception {
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(progressionService.isUnlocked(diamondArmor)).thenReturn(false);
    when(timerService.timeRemaining(diamondArmor))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofDays(3), false)));

    dispatcher.execute("gearprogress info armor diamond", source);
    assertFalse(messages.isEmpty());
  }

  @Test
  @DisplayName("Executing /gearprogress unlock calls progression service")
  void testUnlock() throws Exception {
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(progressionService.isUnlocked(diamondArmor)).thenReturn(false);
    when(progressionService.unlock(diamondArmor, UnlockMethod.COMMAND, "MockAdmin"))
        .thenReturn(CompletableFuture.completedFuture(null));

    dispatcher.execute("gearprogress unlock armor diamond", source);
    verify(progressionService).unlock(diamondArmor, UnlockMethod.COMMAND, "MockAdmin");
  }

  @Test
  @DisplayName("Executing /gearprogress lock calls progression service")
  void testLock() throws Exception {
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(progressionService.isUnlocked(diamondArmor)).thenReturn(true);
    when(progressionService.lock(diamondArmor, "MockAdmin"))
        .thenReturn(CompletableFuture.completedFuture(null));

    dispatcher.execute("gearprogress lock armor diamond", source);
    verify(progressionService).lock(diamondArmor, "MockAdmin");
  }

  @Test
  @DisplayName("Executing /gearprogress reset all and single tier")
  void testReset() throws Exception {
    when(progressionService.initialize()).thenReturn(CompletableFuture.completedFuture(null));
    dispatcher.execute("gearprogress reset all", source);
    verify(progressionService).initialize();

    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(progressionService.lock(diamondArmor, "RESET"))
        .thenReturn(CompletableFuture.completedFuture(null));
    dispatcher.execute("gearprogress reset armor diamond", source);
    verify(progressionService).lock(diamondArmor, "RESET");
  }

  @Test
  @DisplayName("Executing /gearprogress timer subcommands")
  void testTimerSubcommands() throws Exception {
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    when(timerService.timeRemaining(diamondArmor))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(2), false)));
    when(timerService.isPaused(diamondArmor)).thenReturn(false);

    dispatcher.execute("gearprogress timer status armor diamond", source);
    assertFalse(messages.isEmpty());

    dispatcher.execute("gearprogress timer pause armor diamond", source);
    verify(timerService).pause(diamondArmor);

    dispatcher.execute("gearprogress timer resume armor diamond", source);
    verify(timerService).resume(diamondArmor);

    dispatcher.execute("gearprogress timer add armor diamond 1h", source);
    verify(timerService).addElapsed(diamondArmor, Duration.ofHours(1));

    dispatcher.execute("gearprogress timer set armor diamond 2d", source);
    verify(timerService).setElapsed(diamondArmor, Duration.ofDays(2));
  }

  @Test
  @DisplayName("Executing /gearprogress reload reloads application")
  void testReload() throws Exception {
    dispatcher.execute("gearprogress reload", source);
    verify(application).reload();
  }

  @Test
  @DisplayName("Executing /gearprogress view opens GUI for player and informs console")
  void testView() throws Exception {
    ProgressionGuiView mockGui = mock(ProgressionGuiView.class);
    GearProgressMainCommand customCmd = new GearProgressMainCommand(application, locales, mockGui);
    CommandDispatcher<CommandSourceStack> testDispatcher =
        PaperCommandTestSupport.createDispatcher();
    testDispatcher.register(customCmd.buildCommand());

    // Console execution
    testDispatcher.execute("gearprogress view", source);
    verify(mockGui, org.mockito.Mockito.never()).open(any());

    // Player execution
    org.bukkit.entity.Player player = mock(org.bukkit.entity.Player.class);
    when(player.hasPermission("gearprogress.use")).thenReturn(true);
    CommandSourceStack playerSource = PaperCommandTestSupport.createMockSource(player);

    testDispatcher.execute("gearprogress view", playerSource);
    verify(mockGui).open(player);
  }
}
