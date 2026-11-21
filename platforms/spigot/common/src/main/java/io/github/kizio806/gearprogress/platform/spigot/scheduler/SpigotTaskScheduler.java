package io.github.kizio806.gearprogress.platform.spigot.scheduler;

import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import java.time.Duration;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Task scheduler implementation delegating to standard Bukkit asynchronous scheduler methods. */
public final class SpigotTaskScheduler implements TaskSchedulerPort {

  private final Plugin plugin;

  public SpigotTaskScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
  }

  @Override
  public void runAsync(Runnable task) {
    Objects.requireNonNull(task, "task must not be null");
    Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
  }

  @Override
  public CancellableTask scheduleRepeatingAsync(
      Runnable task, Duration initialDelay, Duration period) {
    Objects.requireNonNull(task, "task must not be null");
    Objects.requireNonNull(initialDelay, "initialDelay must not be null");
    Objects.requireNonNull(period, "period must not be null");

    long initialTicks = Math.max(1L, initialDelay.toMillis() / 50L);
    long periodTicks = Math.max(1L, period.toMillis() / 50L);

    BukkitTask scheduled =
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialTicks, periodTicks);

    return scheduled != null ? scheduled::cancel : () -> {};
  }
}
