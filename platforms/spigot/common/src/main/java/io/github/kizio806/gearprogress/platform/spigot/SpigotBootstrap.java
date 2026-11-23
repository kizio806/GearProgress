package io.github.kizio806.gearprogress.platform.spigot;

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
import io.github.kizio806.gearprogress.platform.spigot.command.SpigotCommandHelper;
import io.github.kizio806.gearprogress.platform.spigot.command.SpigotMainCommand;
import io.github.kizio806.gearprogress.platform.spigot.scheduler.SpigotTaskScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/** Composition Root for GearProgress on the Spigot/Bukkit server platform. */
public final class SpigotBootstrap implements BukkitBootstrapLifecycle {

  private final JavaPlugin plugin;
  private final BukkitAudiences adventure;
  private GearProgressApplication application;
  private SQLiteProgressionRepository repository;
  private GearProgressPlaceholderExpansion placeholderExpansion;

  public SpigotBootstrap(JavaPlugin plugin, BukkitAudiences adventure) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    this.adventure = Objects.requireNonNull(adventure, "adventure must not be null");
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

    TaskSchedulerPort taskScheduler = new SpigotTaskScheduler(plugin);
    LifecycleReporterPort reporter = new BukkitLifecycleReporter(plugin);
    EventPublisherPort eventPublisher = new BukkitEventPublisher(plugin.getServer());
    TimePort timePort = Instant::now;

    this.application =
        new GearProgressApplication(
            dataDirectory, repository, taskScheduler, reporter, eventPublisher, timePort);
    this.application.start();

    FeedbackNotifier feedback =
        new FeedbackNotifier(
            application.pluginConfig(), application.localeCatalog(), adventure::player);

    registerListeners(feedback);
    registerCommands();
    registerPlaceholderApiHook(reporter);

    long duration = System.currentTimeMillis() - startTime;
    reporter.info("GearProgress enabled successfully on Spigot/Bukkit in " + duration + "ms.");
  }

  @Override
  public void onDisable() {
    if (placeholderExpansion != null) {
      try {
        placeholderExpansion.unregister();
      } catch (Exception | LinkageError ex) {
        plugin.getLogger().log(Level.FINEST, "Failed to unregister PlaceholderAPI", ex);
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

    plugin.getLogger().info("GearProgress disabled successfully on Spigot/Bukkit.");
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
    PluginCommand command = plugin.getCommand("gearprogress");
    if (command != null) {
      SpigotCommandHelper helper = new SpigotCommandHelper(adventure, application.localeCatalog());
      SpigotMainCommand mainCommand = new SpigotMainCommand(application, helper);
      command.setExecutor(mainCommand);
      command.setTabCompleter(mainCommand);
    } else {
      plugin.getLogger().warning("Command /gearprogress is not registered in plugin.yml");
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
