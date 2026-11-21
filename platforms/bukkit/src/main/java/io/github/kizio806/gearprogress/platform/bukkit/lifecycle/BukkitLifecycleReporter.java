package io.github.kizio806.gearprogress.platform.bukkit.lifecycle;

import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

/** Lifecycle reporter implementation delegating to the Bukkit plugin's logger. */
public final class BukkitLifecycleReporter implements LifecycleReporterPort {

  private final Logger logger;

  public BukkitLifecycleReporter(Plugin plugin) {
    this(Objects.requireNonNull(plugin, "plugin must not be null").getLogger());
  }

  public BukkitLifecycleReporter(Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger must not be null");
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void warning(String message) {
    logger.warning(message);
  }

  @Override
  public void severe(String message, Throwable throwable) {
    logger.log(Level.SEVERE, message, throwable);
  }
}
