package io.github.kizio806.gearprogress.platform.paper.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaperTaskScheduler Unit Tests")
class PaperTaskSchedulerTest {

  private Plugin plugin;
  private Server server;
  private AsyncScheduler asyncScheduler;
  private PaperTaskScheduler scheduler;

  @BeforeEach
  void setUp() {
    plugin = mock(Plugin.class);
    server = mock(Server.class);
    asyncScheduler = mock(AsyncScheduler.class);

    when(plugin.getServer()).thenReturn(server);
    when(server.getAsyncScheduler()).thenReturn(asyncScheduler);

    scheduler = new PaperTaskScheduler(plugin);
  }

  @Test
  @DisplayName("runAsync submits task to async scheduler")
  void testRunAsync() {
    AtomicBoolean executed = new AtomicBoolean(false);
    scheduler.runAsync(() -> executed.set(true));

    verify(asyncScheduler).runNow(eq(plugin), any());
  }

  @Test
  @DisplayName("scheduleRepeatingAsync schedules repeating task")
  void testScheduleRepeatingAsync() {
    ScheduledTask mockTask = mock(ScheduledTask.class);
    when(asyncScheduler.runAtFixedRate(
            eq(plugin), any(), eq(100L), eq(200L), eq(TimeUnit.MILLISECONDS)))
        .thenReturn(mockTask);

    TaskSchedulerPort.CancellableTask task =
        scheduler.scheduleRepeatingAsync(() -> {}, Duration.ofMillis(100), Duration.ofMillis(200));

    assertNotNull(task);
    task.cancel();
    verify(mockTask).cancel();
  }
}
