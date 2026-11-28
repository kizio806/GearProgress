package io.github.kizio806.gearprogress.application;

import io.github.kizio806.gearprogress.application.api.GearProgressApi;
import io.github.kizio806.gearprogress.application.config.compiler.EquipmentDefinitionCompiler;
import io.github.kizio806.gearprogress.application.config.loader.YamlConfigLoader;
import io.github.kizio806.gearprogress.application.config.locale.FileSystemLocaleLoader;
import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import io.github.kizio806.gearprogress.application.config.spec.EquipmentConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.PluginConfigSpec;
import io.github.kizio806.gearprogress.application.config.spec.TimersConfigSpec;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import io.github.kizio806.gearprogress.application.port.ProgressionRepository;
import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.application.service.ProgressionService;
import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.application.service.TimerService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.core.validation.ValidationDiagnostic;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Central platform-independent application orchestrator for GearProgress.
 *
 * <p><b>Architectural Role:</b> Acts as the Hexagonal Application Core orchestrator. It manages the
 * compilation of YAML definitions into immutable runtime models, maintains service lifecycles, and
 * implements {@link GearProgressApi} while remaining completely free of Minecraft/Bukkit runtime
 * dependencies.
 *
 * <p><b>Threading Guarantees:</b>
 *
 * <ul>
 *   <li>Configuration reloading ({@link #reload()}) and shutdown ({@link #shutdown()}) are
 *       synchronized.
 *   <li>Live query operations are non-blocking and lock-free across all threads.
 *   <li>Background evaluation of unlock timers runs asynchronously via {@link TaskSchedulerPort}.
 * </ul>
 */
public final class GearProgressApplication implements GearProgressApi {

  private final Path dataDirectory;
  private final ProgressionRepository repository;
  private final TaskSchedulerPort taskScheduler;
  private final LifecycleReporterPort reporter;
  private final EventPublisherPort eventPublisher;
  private final TimePort timePort;

  private final EquipmentRegistry equipmentRegistry = new EquipmentRegistry();
  private final YamlConfigLoader yamlLoader = new YamlConfigLoader();
  private final EquipmentDefinitionCompiler compiler = new EquipmentDefinitionCompiler();

  private final ProgressionService progressionService;
  private final RestrictionQueryService restrictionQueryService;
  private final TimerService timerService;

  private volatile PluginConfigSpec pluginConfig = PluginConfigSpec.defaults();
  private volatile LocaleCatalog localeCatalog;
  private TaskSchedulerPort.CancellableTask timerTask;

  public GearProgressApplication(
      Path dataDirectory,
      ProgressionRepository repository,
      TaskSchedulerPort taskScheduler,
      LifecycleReporterPort reporter,
      EventPublisherPort eventPublisher,
      TimePort timePort) {
    this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
    this.repository = Objects.requireNonNull(repository, "repository must not be null");
    this.taskScheduler = Objects.requireNonNull(taskScheduler, "taskScheduler must not be null");
    this.reporter = Objects.requireNonNull(reporter, "reporter must not be null");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
    this.timePort = Objects.requireNonNull(timePort, "timePort must not be null");

    this.progressionService =
        new ProgressionService(equipmentRegistry, repository, eventPublisher, timePort);
    this.restrictionQueryService =
        new RestrictionQueryService(progressionService, equipmentRegistry);
    this.timerService = new TimerService(progressionService, equipmentRegistry, timePort, reporter);
  }

  /**
   * Starts the application: loads configs, initializes persistence, and starts scheduler.
   *
   * @throws IOException on config read error
   */
  public void start() throws IOException {
    loadConfigurations();
    progressionService.initialize().join();
    startTimerTask();
    reporter.info("GearProgress application core initialized successfully.");
  }

  /**
   * Reloads all configuration files and locale catalogs.
   *
   * @throws IOException on config error
   */
  public void reload() throws IOException {
    loadConfigurations();
    progressionService.initialize().join();
    startTimerTask();
    reporter.info("GearProgress application configuration reloaded.");
  }

  private void loadConfigurations() throws IOException {
    Path configPath = dataDirectory.resolve("config.yml");
    Map<String, Object> configMap = yamlLoader.loadFile(configPath);
    if (!configMap.isEmpty()) {
      this.pluginConfig = yamlLoader.parsePluginSpec(configMap);
    }

    FileSystemLocaleLoader localeLoader = new FileSystemLocaleLoader(dataDirectory);
    this.localeCatalog = localeLoader.loadCatalog(pluginConfig.locale());

    Path equipmentPath = dataDirectory.resolve("equipment.yml");
    Map<String, Object> equipmentMap = yamlLoader.loadFile(equipmentPath);
    if (!equipmentMap.isEmpty()) {
      EquipmentConfigSpec equipmentSpec = yamlLoader.parseEquipmentSpec(equipmentMap);
      var result = compiler.compile(equipmentSpec);
      for (ValidationDiagnostic diag : result.validationResult().diagnostics()) {
        if (diag.severity().isError()) {
          reporter.severe("[" + diag.path() + "] " + diag.message(), null);
        } else {
          reporter.warning("[" + diag.path() + "] " + diag.message());
        }
      }
      equipmentRegistry.replaceAll(result.definitions());
      reporter.info(
          "Compiled " + result.definitions().size() + " equipment progression tier definitions.");
    }

    Path timersPath = dataDirectory.resolve("timers.yml");
    Map<String, Object> timersMap = yamlLoader.loadFile(timersPath);
    if (!timersMap.isEmpty()) {
      TimersConfigSpec timersSpec = yamlLoader.parseTimersSpec(timersMap, timePort.now());
      timerService.loadFromSpec(timersSpec);
    }
  }

  private synchronized void startTimerTask() {
    if (timerTask != null) {
      timerTask.cancel();
      timerTask = null;
    }
    long intervalTicks = pluginConfig.timerCheckIntervalTicks();
    long millis = Math.max(50L, intervalTicks * 50L);
    Duration interval = Duration.ofMillis(millis);

    timerTask =
        taskScheduler.scheduleRepeatingAsync(
            () -> {
              var unused = timerService.evaluateProgression();
            },
            Duration.ofSeconds(1),
            interval);
  }

  /** Shuts down the application and background tasks. */
  public synchronized void shutdown() {
    if (timerTask != null) {
      timerTask.cancel();
      timerTask = null;
    }
    repository.close();
  }

  @Override
  public boolean isUnlocked(EquipmentCategory category, String tier) {
    return isUnlocked(TierKey.of(category, tier));
  }

  @Override
  public boolean isUnlocked(TierKey key) {
    return progressionService.isUnlocked(key);
  }

  @Override
  public boolean isEnchantmentAllowed(EquipmentCategory category, String tier) {
    return isEnchantmentAllowed(TierKey.of(category, tier));
  }

  @Override
  public boolean isEnchantmentAllowed(TierKey key) {
    return progressionService.isEnchantmentAllowed(key);
  }

  @Override
  public CompletableFuture<Boolean> unlock(EquipmentCategory category, String tier, String reason) {
    return unlock(TierKey.of(category, tier), reason);
  }

  @Override
  public CompletableFuture<Boolean> unlock(TierKey key, String reason) {
    return progressionService.unlock(key, UnlockMethod.COMMAND, reason);
  }

  @Override
  public CompletableFuture<Boolean> lock(EquipmentCategory category, String tier, String reason) {
    return lock(TierKey.of(category, tier), reason);
  }

  @Override
  public CompletableFuture<Boolean> lock(TierKey key, String reason) {
    return progressionService.lock(key, reason);
  }

  @Override
  public Optional<TimeRemaining> remainingTime(EquipmentCategory category, String tier) {
    return remainingTime(TierKey.of(category, tier));
  }

  @Override
  public Optional<TimeRemaining> remainingTime(TierKey key) {
    return timerService.timeRemaining(key);
  }

  @Override
  public Optional<TierProgressState> currentState(EquipmentCategory category, String tier) {
    return currentState(TierKey.of(category, tier));
  }

  @Override
  public Optional<TierProgressState> currentState(TierKey key) {
    return progressionService.state(key);
  }

  public ProgressionService progressionService() {
    return progressionService;
  }

  public RestrictionQueryService restrictionQueryService() {
    return restrictionQueryService;
  }

  public TimerService timerService() {
    return timerService;
  }

  public EquipmentRegistry equipmentRegistry() {
    return equipmentRegistry;
  }

  public PluginConfigSpec pluginConfig() {
    return pluginConfig;
  }

  public LocaleCatalog localeCatalog() {
    return localeCatalog;
  }

  public EventPublisherPort eventPublisher() {
    return eventPublisher;
  }
}
