package io.github.kizio806.gearprogress.platform.paper;

import io.github.kizio806.gearprogress.platform.bukkit.lifecycle.BukkitBootstrapLifecycle;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

/** Main Paper plugin entrypoint for GearProgress. */
public final class GearProgressPlugin extends JavaPlugin {

  private final Function<JavaPlugin, BukkitBootstrapLifecycle> bootstrapFactory;
  private BukkitBootstrapLifecycle bootstrap;

  public GearProgressPlugin() {
    this(PaperBootstrap::new);
  }

  GearProgressPlugin(Function<JavaPlugin, BukkitBootstrapLifecycle> bootstrapFactory) {
    this.bootstrapFactory =
        Objects.requireNonNull(bootstrapFactory, "bootstrapFactory must not be null");
  }

  @Override
  public void onEnable() {
    try {
      this.bootstrap = bootstrapFactory.apply(this);
      this.bootstrap.onEnable();
    } catch (Exception ex) {
      getLogger().log(Level.SEVERE, "Failed to enable GearProgress plugin: " + ex.getMessage(), ex);
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (this.bootstrap != null) {
      try {
        this.bootstrap.onDisable();
      } catch (Exception ex) {
        getLogger()
            .log(Level.SEVERE, "Error while disabling GearProgress plugin: " + ex.getMessage(), ex);
      } finally {
        this.bootstrap = null;
      }
    }
  }

  public BukkitBootstrapLifecycle bootstrap() {
    return bootstrap;
  }
}
