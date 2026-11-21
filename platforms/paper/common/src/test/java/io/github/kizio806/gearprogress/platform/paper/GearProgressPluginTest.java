package io.github.kizio806.gearprogress.platform.paper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.platform.bukkit.lifecycle.BukkitBootstrapLifecycle;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("GearProgressPlugin Unit Tests")
class GearProgressPluginTest {

  @TempDir Path tempDir;

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private GearProgressPlugin createPluginInstance(
      Function<JavaPlugin, BukkitBootstrapLifecycle> factory) throws Exception {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field f = unsafeClass.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Object unsafe = f.get(null);
    Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
    GearProgressPlugin plugin =
        (GearProgressPlugin) allocateInstance.invoke(unsafe, GearProgressPlugin.class);

    setField(JavaPlugin.class, plugin, "server", Bukkit.getServer());
    setField(JavaPlugin.class, plugin, "dataFolder", tempDir.resolve("data").toFile());
    setField(JavaPlugin.class, plugin, "file", tempDir.resolve("plugin.jar").toFile());
    setField(JavaPlugin.class, plugin, "logger", Logger.getLogger("GearProgress"));
    setField(GearProgressPlugin.class, plugin, "bootstrapFactory", factory);

    return plugin;
  }

  private static void setField(Class<?> clazz, Object target, String name, Object value)
      throws ReflectiveOperationException {
    Field f = clazz.getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }

  @Test
  @DisplayName("onEnable invokes bootstrap lifecycle onEnable")
  void testOnEnableSuccess() throws Exception {
    AtomicBoolean enabled = new AtomicBoolean(false);
    BukkitBootstrapLifecycle mockBootstrap =
        new BukkitBootstrapLifecycle() {
          @Override
          public void onEnable() {
            enabled.set(true);
          }

          @Override
          public void onDisable() {}
        };

    GearProgressPlugin plugin = createPluginInstance(p -> mockBootstrap);
    plugin.onEnable();

    assertTrue(enabled.get());
    assertNotNull(plugin.bootstrap());
  }

  @Test
  @DisplayName("onDisable invokes bootstrap lifecycle onDisable and clears instance")
  void testOnDisableSuccess() throws Exception {
    AtomicBoolean disabled = new AtomicBoolean(false);
    BukkitBootstrapLifecycle mockBootstrap =
        new BukkitBootstrapLifecycle() {
          @Override
          public void onEnable() {}

          @Override
          public void onDisable() {
            disabled.set(true);
          }
        };

    GearProgressPlugin plugin = createPluginInstance(p -> mockBootstrap);
    plugin.onEnable();
    plugin.onDisable();

    assertTrue(disabled.get());
    assertNull(plugin.bootstrap());
  }

  @Test
  @DisplayName("onEnable fails gracefully when bootstrap throws exception")
  void testOnEnableFailsGracefully() throws Exception {
    GearProgressPlugin plugin =
        createPluginInstance(
            p -> {
              throw new RuntimeException("Simulated bootstrap startup failure");
            });

    plugin.onEnable();
    assertNull(plugin.bootstrap());
  }

  @Test
  @DisplayName("onDisable handles exceptions thrown during disable gracefully")
  void testOnDisableHandlesException() throws Exception {
    BukkitBootstrapLifecycle failingBootstrap =
        new BukkitBootstrapLifecycle() {
          @Override
          public void onEnable() {}

          @Override
          public void onDisable() {
            throw new RuntimeException("Simulated bootstrap shutdown failure");
          }
        };

    GearProgressPlugin plugin = createPluginInstance(p -> failingBootstrap);
    plugin.onEnable();
    plugin.onDisable();
    assertNull(plugin.bootstrap());
  }
}
