package io.github.kizio806.gearprogress.platform.spigot.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.service.ProgressionService;
import io.github.kizio806.gearprogress.application.service.TimerService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.platform.bukkit.gui.ProgressionGuiView;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SpigotMainCommand Unit Tests")
class SpigotMainCommandTest {

  private GearProgressApplication application;
  private SpigotCommandHelper helper;
  private SpigotMainCommand mainCommand;
  private Command command;
  private CommandSender sender;
  private EquipmentRegistry equipmentRegistry;
  private ProgressionService progressionService;
  private TimerService timerService;

  @BeforeEach
  void setUp() {
    application = mock(GearProgressApplication.class);
    helper = mock(SpigotCommandHelper.class);
    command = mock(Command.class);
    sender = mock(CommandSender.class);
    equipmentRegistry = mock(EquipmentRegistry.class);
    progressionService = mock(ProgressionService.class);
    timerService = mock(TimerService.class);

    when(application.equipmentRegistry()).thenReturn(equipmentRegistry);
    when(application.progressionService()).thenReturn(progressionService);
    when(application.timerService()).thenReturn(timerService);
    when(sender.getName()).thenReturn("Admin");

    mainCommand = new SpigotMainCommand(application, helper);
  }

  @Test
  @DisplayName("handleHelp dispatches help when args empty or help")
  void testHelp() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[0]));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"help"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleReload calls application.reload and sends message")
  void testReload() throws IOException {
    when(helper.requirePermission(sender, "gearprogress.reload")).thenReturn(true);
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"reload"}));
    verify(application).reload();
    verify(helper).send(sender, "general.plugin-reloaded");
  }

  @Test
  @DisplayName("handleUnlock forces unlock of valid tier")
  void testUnlock() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.unlock(eq(key), any(), eq("Admin")))
        .thenReturn(CompletableFuture.completedFuture(null));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"unlock", "armor", "diamond"}));
    verify(progressionService).unlock(eq(key), any(), eq("Admin"));
  }

  @Test
  @DisplayName("handleReset resets tier state")
  void testResetSuccess() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.lock(eq(key), eq("Admin")))
        .thenReturn(CompletableFuture.completedFuture(true));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"reset", "armor", "diamond"}));
    verify(progressionService).lock(eq(key), eq("Admin"));
    verify(helper).send(eq(sender), eq("command.reset.success"), any());
  }

  @Test
  @DisplayName("handleStatus displays status of categories and tiers")
  void testStatusSuccess() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(def.key()).thenReturn(key);
    when(equipmentRegistry.findByCategory(EquipmentCategory.ARMOR)).thenReturn(Map.of(key, def));
    when(progressionService.isUnlocked(key)).thenReturn(true);

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"status"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleInfo displays detailed tier info")
  void testInfoSuccess() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(def.key()).thenReturn(key);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.isUnlocked(key)).thenReturn(false);
    when(timerService.timeRemaining(key))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(2), false)));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"info", "armor", "diamond"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleTimer subcommands execute successfully on valid tiers")
  void testTimerSubcommandsSuccess() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(def.key()).thenReturn(key);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(timerService.timeRemaining(key))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(2), false)));
    when(timerService.isPaused(key)).thenReturn(false);

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "status", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "pause", "armor", "diamond"}));
    verify(timerService).pause(key);

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "resume", "armor", "diamond"}));
    verify(timerService).resume(key);

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "add", "armor", "diamond", "1h"}));
    verify(timerService).addElapsed(key, Duration.ofHours(1));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "set", "armor", "diamond", "2d"}));
    verify(timerService).setElapsed(key, Duration.ofDays(2));
  }

  @Test
  @DisplayName("handleLock forces lock of valid tier")
  void testLock() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.lock(eq(key), eq("Admin")))
        .thenReturn(CompletableFuture.completedFuture(null));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"lock", "armor", "diamond"}));
    verify(progressionService).lock(eq(key), eq("Admin"));
  }

  @Test
  @DisplayName("handleReset resets tier progress state")
  void testReset() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.lock(eq(key), eq("Admin")))
        .thenReturn(CompletableFuture.completedFuture(null));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"reset", "armor", "diamond"}));
    verify(progressionService).lock(eq(key), eq("Admin"));
  }

  @Test
  @DisplayName("handleInfo displays tier progression state")
  void testInfo() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));
    when(progressionService.isUnlocked(key)).thenReturn(false);
    when(timerService.timeRemaining(key))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(5), false)));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"info", "armor", "diamond"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleStatus displays all category lines")
  void testStatus() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(def.key()).thenReturn(key);
    when(equipmentRegistry.findByCategory(EquipmentCategory.ARMOR)).thenReturn(Map.of(key, def));

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"status"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleTimer manages add, set, and status actions")
  void testTimerActions() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "add", "armor", "diamond", "1d"}));
    verify(timerService).addElapsed(eq(key), any(Duration.class));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "set", "armor", "diamond", "2d"}));
    verify(timerService).setElapsed(eq(key), any(Duration.class));

    when(timerService.timeRemaining(key))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(2), false)));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "status", "armor", "diamond"}));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "pause", "armor", "diamond"}));
    verify(timerService).pause(key);

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "resume", "armor", "diamond"}));
    verify(timerService).resume(key);
  }

  @Test
  @DisplayName("handleUnknownCommand dispatches unknown message and returns false")
  void testUnknownCommand() {
    assertFalse(mainCommand.onCommand(sender, command, "gp", new String[] {"nonexistent"}));
    verify(helper).send(sender, "general.unknown-command");
  }

  @Test
  @DisplayName("handleReload handles exception thrown by application")
  void testReloadException() throws IOException {
    when(helper.requirePermission(sender, "gearprogress.reload")).thenReturn(true);
    org.mockito.Mockito.doThrow(new IOException("Disk read error")).when(application).reload();
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"reload"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("handleUnlock and handleLock handle validation errors")
  void testUnlockAndLockValidation() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"unlock"}));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"lock"}));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"reset"}));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"info"}));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"unlock", "unknowncat", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"lock", "unknowncat", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"reset", "unknowncat", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"info", "unknowncat", "diamond"}));
  }

  @Test
  @DisplayName("handleTimer handles short arguments and validation failures")
  void testTimerValidation() {
    when(helper.requirePermission(sender, "gearprogress.admin")).thenReturn(true);
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"timer"}));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"timer", "status", "armor"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "status", "badcat", "tier"}));

    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"timer", "pause", "armor"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "pause", "badcat", "tier"}));
    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"timer", "resume", "armor"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "resume", "badcat", "tier"}));

    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "add", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "add", "badcat", "diamond", "1h"}));

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(equipmentRegistry.find(key)).thenReturn(Optional.of(def));

    assertTrue(
        mainCommand.onCommand(
            sender,
            command,
            "gp",
            new String[] {"timer", "add", "armor", "diamond", "invalid-duration"}));
    assertTrue(
        mainCommand.onCommand(
            sender,
            command,
            "gp",
            new String[] {"timer", "unknownaction", "armor", "diamond", "1h"}));
  }

  @Test
  @DisplayName("handleView opens GUI for player and rejects console")
  void testViewSubcommand() {
    ProgressionGuiView mockGui = mock(ProgressionGuiView.class);
    SpigotMainCommand cmdWithGui = new SpigotMainCommand(application, helper, mockGui);

    assertTrue(cmdWithGui.onCommand(sender, command, "gp", new String[] {"view"}));
    verify(mockGui, org.mockito.Mockito.never()).open(any());

    org.bukkit.entity.Player player = mock(org.bukkit.entity.Player.class);
    when(helper.requirePermission(player, "gearprogress.use")).thenReturn(true);
    assertTrue(cmdWithGui.onCommand(player, command, "gp", new String[] {"view"}));
    verify(mockGui).open(player);

    org.bukkit.entity.Player playerNoPerm = mock(org.bukkit.entity.Player.class);
    when(helper.requirePermission(playerNoPerm, "gearprogress.use")).thenReturn(false);
    assertTrue(cmdWithGui.onCommand(playerNoPerm, command, "gp", new String[] {"view"}));
  }

  @Test
  @DisplayName("handleHelp displays full admin help when permitted")
  void testHelpWithAdminPermissions() {
    when(helper.requirePermission(sender, "gearprogress.use")).thenReturn(true);
    when(sender.hasPermission("gearprogress.reload")).thenReturn(true);
    when(sender.hasPermission("gearprogress.admin")).thenReturn(true);

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"help"}));
    verify(helper, org.mockito.Mockito.atLeastOnce()).sendMessage(any(), any(Component.class));
  }

  @Test
  @DisplayName("permission checks prevent execution when denied")
  void testPermissionDenied() {
    when(helper.requirePermission(any(), any())).thenReturn(false);

    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"help"}));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"reload"}));
    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"unlock", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"lock", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"reset", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(sender, command, "gp", new String[] {"info", "armor", "diamond"}));
    assertTrue(mainCommand.onCommand(sender, command, "gp", new String[] {"status"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "status", "armor", "diamond"}));
    assertTrue(
        mainCommand.onCommand(
            sender, command, "gp", new String[] {"timer", "add", "armor", "diamond", "1d"}));
  }

  @Test
  @DisplayName("onTabComplete provides context-aware completions")
  void testTabComplete() {
    when(helper.matchPrefix(eq("rel"), any())).thenReturn(List.of("reload"));
    List<String> result1 = mainCommand.onTabComplete(sender, command, "gp", new String[] {"rel"});
    assertNotNull(result1);

    when(helper.matchPrefix(eq("add"), any())).thenReturn(List.of("add"));
    List<String> result2 =
        mainCommand.onTabComplete(sender, command, "gp", new String[] {"timer", "add"});
    assertNotNull(result2);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    EquipmentDefinition def = mock(EquipmentDefinition.class);
    when(def.key()).thenReturn(key);
    when(equipmentRegistry.findByCategory(EquipmentCategory.ARMOR)).thenReturn(Map.of(key, def));

    List<String> result3 =
        mainCommand.onTabComplete(sender, command, "gp", new String[] {"unlock", "arm"});
    assertNotNull(result3);

    List<String> result4 =
        mainCommand.onTabComplete(sender, command, "gp", new String[] {"unlock", "armor", "dia"});
    assertNotNull(result4);

    List<String> result5 =
        mainCommand.onTabComplete(sender, command, "gp", new String[] {"timer", "armor"});
    assertNotNull(result5);

    List<String> result6 =
        mainCommand.onTabComplete(
            sender, command, "gp", new String[] {"timer", "add", "armor", "dia"});
    assertNotNull(result6);

    List<String> result7 =
        mainCommand.onTabComplete(
            sender, command, "gp", new String[] {"timer", "add", "armor", "diamond", "1"});
    assertNotNull(result7);

    List<String> result8 =
        mainCommand.onTabComplete(
            sender, command, "gp", new String[] {"timer", "status", "armor", "diamond", "1"});
    assertNotNull(result8);

    List<String> result9 =
        mainCommand.onTabComplete(
            sender,
            command,
            "gp",
            new String[] {"too", "many", "arguments", "here", "to", "complete"});
    assertNotNull(result9);
  }
}
