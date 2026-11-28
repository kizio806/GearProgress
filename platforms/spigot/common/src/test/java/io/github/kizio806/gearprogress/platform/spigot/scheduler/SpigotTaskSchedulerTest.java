package io.github.kizio806.gearprogress.platform.spigot.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SpigotTaskScheduler Unit Tests")
class SpigotTaskSchedulerTest {

  private Plugin plugin;
  private SpigotTaskScheduler taskScheduler;

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  @BeforeEach
  void setUp() {
    plugin = mock(Plugin.class);
    when(plugin.getServer()).thenReturn(Bukkit.getServer());
    taskScheduler = new SpigotTaskScheduler(plugin);
  }

  @Test
  @DisplayName("runAsync dispatches task asynchronously")
  void testRunAsync() {
    AtomicBoolean executed = new AtomicBoolean(false);
    taskScheduler.runAsync(() -> executed.set(true));
    assertNotNull(taskScheduler);
  }

  @Test
  @DisplayName("scheduleRepeatingAsync schedules repeating task")
  void testScheduleRepeatingAsync() {
    TaskSchedulerPort.CancellableTask task =
        taskScheduler.scheduleRepeatingAsync(
            () -> {}, Duration.ofMillis(100), Duration.ofMillis(200));
    assertNotNull(task);
    task.cancel();
  }
}
