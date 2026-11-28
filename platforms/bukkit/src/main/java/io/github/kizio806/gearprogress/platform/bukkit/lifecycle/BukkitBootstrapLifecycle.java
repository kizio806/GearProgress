package io.github.kizio806.gearprogress.platform.bukkit.lifecycle;

import java.io.IOException;
import java.sql.SQLException;

/** Lifecycle contract for platform bootstrapping and graceful shutdown. */
public interface BukkitBootstrapLifecycle {

  /**
   * Invoked when the plugin is enabled.
   *
   * @throws IOException on configuration or I/O failure
   * @throws SQLException on database failure
   */
  void onEnable() throws IOException, SQLException;

  /** Invoked when the plugin is disabled. */
  void onDisable();
}
