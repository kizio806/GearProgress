package io.github.kizio806.gearprogress.platform.spigot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Logger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("SpigotBootstrap Unit Tests")
class SpigotBootstrapTest {

  @TempDir Path tempDir;

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  @Test
  @DisplayName("SpigotBootstrap enables and disables cleanly")
  void testBootstrapEnableAndDisable() throws Exception {
    JavaPlugin plugin = mock(JavaPlugin.class);
    BukkitAudiences adventure = mock(BukkitAudiences.class);
    Audience audience = mock(Audience.class);
    when(adventure.sender(any(org.bukkit.command.CommandSender.class))).thenReturn(audience);
    when(adventure.player(any(org.bukkit.entity.Player.class))).thenReturn(audience);

    Server server = Bukkit.getServer();
    Logger logger = Logger.getLogger("SpigotBootstrapTest");

    when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
    when(plugin.getServer()).thenReturn(server);
    when(plugin.getLogger()).thenReturn(logger);

    String configContent = "schema-version: 1\ndefault-locale: en-US\ncheck-interval-seconds: 1\n";
    String equipmentContent =
        "schema-version: 1\ncategories:\n  armor:\n    leather:\n      unlock:\n        method: always\n";
    String timersContent = "schema-version: 1\nserver-start-epoch: \"2026-11-20T20:00:00Z\"\n";
    String storageContent = "schema-version: 1\ntype: sqlite\n";
    String localeContent =
        "schema-version: 1\ngeneral:\n  prefix: '[GP] '\n  plugin-reloaded: 'Reloaded'\n";

    when(plugin.getResource("config.yml"))
        .thenReturn(new ByteArrayInputStream(configContent.getBytes(StandardCharsets.UTF_8)));
    when(plugin.getResource("equipment.yml"))
        .thenReturn(new ByteArrayInputStream(equipmentContent.getBytes(StandardCharsets.UTF_8)));
    when(plugin.getResource("timers.yml"))
        .thenReturn(new ByteArrayInputStream(timersContent.getBytes(StandardCharsets.UTF_8)));
    when(plugin.getResource("storage.yml"))
        .thenReturn(new ByteArrayInputStream(storageContent.getBytes(StandardCharsets.UTF_8)));
    when(plugin.getResource("locales/en-US.yml"))
        .thenReturn(new ByteArrayInputStream(localeContent.getBytes(StandardCharsets.UTF_8)));
    when(plugin.getResource("locales/pl-PL.yml"))
        .thenReturn(new ByteArrayInputStream(localeContent.getBytes(StandardCharsets.UTF_8)));

    PluginCommand mockCommand = mock(PluginCommand.class);
    when(plugin.getCommand("gearprogress")).thenReturn(mockCommand);

    SpigotBootstrap bootstrap = new SpigotBootstrap(plugin, adventure);
    bootstrap.onEnable();

    assertNotNull(bootstrap.application());

    bootstrap.onDisable();
    assertNull(bootstrap.application());
  }
}
