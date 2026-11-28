package io.github.kizio806.gearprogress.platform.spigot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.kizio806.gearprogress.platform.bukkit.lifecycle.BukkitBootstrapLifecycle;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("GearProgressSpigotPlugin Unit Tests")
class GearProgressSpigotPluginTest {

  @TempDir Path tempDir;

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private GearProgressSpigotPlugin createPluginInstance(
      Function<Plugin, BukkitAudiences> audienceFactory,
      BiFunction<JavaPlugin, BukkitAudiences, BukkitBootstrapLifecycle> bootstrapFactory)
      throws Exception {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field f = unsafeClass.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Object unsafe = f.get(null);
    Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
    GearProgressSpigotPlugin plugin =
        (GearProgressSpigotPlugin) allocateInstance.invoke(unsafe, GearProgressSpigotPlugin.class);

    Field afField = GearProgressSpigotPlugin.class.getDeclaredField("audienceFactory");
    afField.setAccessible(true);
    afField.set(plugin, audienceFactory);

    Field bfField = GearProgressSpigotPlugin.class.getDeclaredField("bootstrapFactory");
    bfField.setAccessible(true);
    bfField.set(plugin, bootstrapFactory);

    setField(JavaPlugin.class, plugin, "server", Bukkit.getServer());
    setField(JavaPlugin.class, plugin, "dataFolder", tempDir.toFile());
    org.bukkit.plugin.PluginLogger mockLogger = mock(org.bukkit.plugin.PluginLogger.class);
    setField(JavaPlugin.class, plugin, "logger", mockLogger);

    return plugin;
  }

  private static void setField(Class<?> clazz, Object target, String fieldName, Object value)
      throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  @DisplayName("onEnable succeeds and initializes bootstrap")
  void testOnEnableSuccess() throws Exception {
    AtomicBoolean enabled = new AtomicBoolean(false);
    BukkitAudiences mockAudiences = mock(BukkitAudiences.class);
    BukkitBootstrapLifecycle mockBootstrap =
        new BukkitBootstrapLifecycle() {
          @Override
          public void onEnable() {
            enabled.set(true);
          }

          @Override
          public void onDisable() {}
        };

    GearProgressSpigotPlugin plugin =
        createPluginInstance(p -> mockAudiences, (p, a) -> mockBootstrap);

    plugin.onEnable();
    assertTrue(enabled.get());
    assertNotNull(plugin.bootstrap());
    assertNotNull(plugin.adventure());

    plugin.onDisable();
    assertNull(plugin.bootstrap());
    assertNull(plugin.adventure());
  }

  @Test
  @DisplayName("onEnable fails gracefully when bootstrap throws exception")
  void testOnEnableFailsGracefully() throws Exception {
    BukkitAudiences mockAudiences = mock(BukkitAudiences.class);
    GearProgressSpigotPlugin plugin =
        createPluginInstance(
            p -> mockAudiences,
            (p, a) -> {
              throw new RuntimeException("Simulated Spigot bootstrap startup failure");
            });

    plugin.onEnable();
    assertNull(plugin.bootstrap());
  }

  @Test
  @DisplayName("onDisable handles exceptions thrown during disable gracefully")
  void testOnDisableHandlesException() throws Exception {
    BukkitAudiences mockAudiences = mock(BukkitAudiences.class);
    org.mockito.Mockito.doThrow(new RuntimeException("Simulated Adventure close failure"))
        .when(mockAudiences)
        .close();
    BukkitBootstrapLifecycle failingBootstrap =
        new BukkitBootstrapLifecycle() {
          @Override
          public void onEnable() {}

          @Override
          public void onDisable() {
            throw new RuntimeException("Simulated Spigot bootstrap shutdown failure");
          }
        };

    GearProgressSpigotPlugin plugin =
        createPluginInstance(p -> mockAudiences, (p, a) -> failingBootstrap);
    plugin.onEnable();
    plugin.onDisable();
    assertNull(plugin.bootstrap());
  }
}
