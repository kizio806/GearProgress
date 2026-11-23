package io.github.kizio806.gearprogress.platform.paper;

import io.github.kizio806.gearprogress.adapter.storage.sqlite.SQLiteDatabaseManager;
import io.github.kizio806.gearprogress.adapter.storage.sqlite.SQLiteProgressionRepository;
import io.github.kizio806.gearprogress.adapter.storage.sqlite.SingleWriterPersistenceExecutor;
import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.platform.bukkit.action.CraftRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.action.EnchantmentRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.action.EquipRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.action.FeedbackNotifier;
import io.github.kizio806.gearprogress.platform.bukkit.action.InventoryMoveRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.action.PickupRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.action.UseRestrictionListener;
import io.github.kizio806.gearprogress.platform.bukkit.event.BukkitEventPublisher;
import io.github.kizio806.gearprogress.platform.bukkit.gui.ProgressionGuiListener;
import io.github.kizio806.gearprogress.platform.bukkit.lifecycle.BukkitBootstrapLifecycle;
import io.github.kizio806.gearprogress.platform.bukkit.lifecycle.BukkitLifecycleReporter;
import io.github.kizio806.gearprogress.platform.bukkit.placeholder.GearProgressPlaceholderExpansion;
import io.github.kizio806.gearprogress.platform.bukkit.util.BukkitResourceExtractor;
import io.github.kizio806.gearprogress.platform.paper.command.GearProgressMainCommand;
import io.github.kizio806.gearprogress.platform.paper.scheduler.PaperTaskScheduler;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/** Composition Root for GearProgress on the Paper server platform. */
public final class PaperBootstrap implements BukkitBootstrapLifecycle {

  private final JavaPlugin plugin;
  private GearProgressApplication application;
  private SQLiteProgressionRepository repository;
  private GearProgressPlaceholderExpansion placeholderExpansion;

  public PaperBootstrap(JavaPlugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
  }

  @Override
  public void onEnable() throws IOException, SQLException {
    long startTime = System.currentTimeMillis();
    Path dataDirectory = plugin.getDataFolder().toPath();
    Files.createDirectories(dataDirectory);

    BukkitResourceExtractor.extractDefaultResources(plugin, dataDirectory);

    Path dbPath = dataDirectory.resolve("gearprogress.db");
    SQLiteDatabaseManager dbManager = new SQLiteDatabaseManager(dbPath);
    this.repository =
        new SQLiteProgressionRepository(dbManager, new SingleWriterPersistenceExecutor());
    this.repository.initialize();

    TaskSchedulerPort taskScheduler = new PaperTaskScheduler(plugin);
    LifecycleReporterPort reporter = new BukkitLifecycleReporter(plugin);
    EventPublisherPort eventPublisher = new BukkitEventPublisher(plugin.getServer());
    TimePort timePort = Instant::now;

    this.application =
        new GearProgressApplication(
            dataDirectory, repository, taskScheduler, reporter, eventPublisher, timePort);
    this.application.start();

    FeedbackNotifier feedback =
        new FeedbackNotifier(application.pluginConfig(), application.localeCatalog());

    registerListeners(feedback);
    registerCommands();
    registerPlaceholderApiHook(reporter);

    long duration = System.currentTimeMillis() - startTime;
    reporter.info("GearProgress enabled successfully in " + duration + "ms.");
  }

  @Override
  public void onDisable() {
    if (placeholderExpansion != null) {
      try {
        placeholderExpansion.unregister();
      } catch (Exception | LinkageError ex) {
        plugin
            .getLogger()
            .log(java.util.logging.Level.FINEST, "Failed to unregister PlaceholderAPI", ex);
      }
      placeholderExpansion = null;
    }

    HandlerList.unregisterAll(plugin);

    if (application != null) {
      application.shutdown();
      application = null;
    }

    if (repository != null) {
      repository.close();
      repository = null;
    }

    plugin.getLogger().info("GearProgress disabled successfully.");
  }

  public GearProgressApplication application() {
    return application;
  }

  private void registerListeners(FeedbackNotifier feedback) {
    var pm = plugin.getServer().getPluginManager();
    pm.registerEvents(
        new CraftRestrictionListener(application.restrictionQueryService(), feedback), plugin);
    pm.registerEvents(
        new UseRestrictionListener(application.restrictionQueryService(), feedback), plugin);
    pm.registerEvents(
        new EquipRestrictionListener(application.restrictionQueryService(), feedback), plugin);
    pm.registerEvents(
        new PickupRestrictionListener(application.restrictionQueryService(), feedback), plugin);
    pm.registerEvents(
        new InventoryMoveRestrictionListener(application.restrictionQueryService(), feedback),
        plugin);
    pm.registerEvents(
        new EnchantmentRestrictionListener(application.restrictionQueryService(), feedback),
        plugin);
    pm.registerEvents(new ProgressionGuiListener(), plugin);
  }

  private void registerCommands() {
    GearProgressMainCommand mainCommand =
        new GearProgressMainCommand(application, application.localeCatalog());

    try {
      plugin
          .getLifecycleManager()
          .registerEventHandler(
              LifecycleEvents.COMMANDS,
              event ->
                  event
                      .registrar()
                      .register(
                          mainCommand.buildCommand().build(),
                          "GearProgress equipment progression management",
                          List.of("gp", "gear")));
    } catch (Exception | LinkageError ex) {
      plugin
          .getLogger()
          .warning("Could not register LifecycleEvents.COMMANDS handler: " + ex.getMessage());
    }
  }

  private void registerPlaceholderApiHook(LifecycleReporterPort reporter) {
    if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      try {
        this.placeholderExpansion = new GearProgressPlaceholderExpansion(application);
        if (this.placeholderExpansion.register()) {
          reporter.info("Registered PlaceholderAPI expansion for %gearprogress_*%");
        }
      } catch (Exception | LinkageError ex) {
        reporter.warning("Failed to hook into PlaceholderAPI: " + ex.getMessage());
      }
    }
  }
}
