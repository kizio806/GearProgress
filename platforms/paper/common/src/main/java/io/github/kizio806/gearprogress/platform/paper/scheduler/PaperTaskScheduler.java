package io.github.kizio806.gearprogress.platform.paper.scheduler;

import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.Plugin;

/**
 * Task scheduler implementation utilizing Paper's AsyncScheduler for Folia and region-thread
 * safety.
 *
 * <p><b>Folia Architectural Invariant:</b> Uses {@code server.getAsyncScheduler()} instead of the
 * classic Bukkit scheduler singleton, guaranteeing that asynchronous tasks never interact with
 * regional tick loops and remain safe across Paper, Purpur, and Folia runtimes.
 */
public final class PaperTaskScheduler implements TaskSchedulerPort {

  private final Plugin plugin;

  public PaperTaskScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
  }

  @Override
  public void runAsync(Runnable task) {
    Objects.requireNonNull(task, "task must not be null");
    plugin.getServer().getAsyncScheduler().runNow(plugin, scheduled -> task.run());
  }

  @Override
  public CancellableTask scheduleRepeatingAsync(
      Runnable task, Duration initialDelay, Duration period) {
    Objects.requireNonNull(task, "task must not be null");
    Objects.requireNonNull(initialDelay, "initialDelay must not be null");
    Objects.requireNonNull(period, "period must not be null");

    ScheduledTask scheduled =
        plugin
            .getServer()
            .getAsyncScheduler()
            .runAtFixedRate(
                plugin,
                st -> task.run(),
                initialDelay.toMillis(),
                period.toMillis(),
                TimeUnit.MILLISECONDS);

    return scheduled::cancel;
  }
}
