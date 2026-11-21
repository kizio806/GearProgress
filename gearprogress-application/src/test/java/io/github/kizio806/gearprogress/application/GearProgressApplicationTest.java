package io.github.kizio806.gearprogress.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.application.port.LifecycleReporterPort;
import io.github.kizio806.gearprogress.application.port.TaskSchedulerPort;
import io.github.kizio806.gearprogress.application.port.TimePort;
import io.github.kizio806.gearprogress.application.repository.InMemoryProgressionRepository;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GearProgressApplicationTest {

  @Test
  void testApplicationLifecycle(@TempDir Path tempDir) throws IOException {
    // Write sample config.yml
    Files.writeString(
        tempDir.resolve("config.yml"),
        """
        locale: "en-US"
        timer-check-interval-ticks: 20
        """);

    // Write sample equipment.yml
    Files.writeString(
        tempDir.resolve("equipment.yml"),
        """
        armor:
          iron:
            unlock-method: always
            allow-enchantments: true
          diamond:
            unlock-method: command
            allow-enchantments: false
        """);

    InMemoryProgressionRepository repo = new InMemoryProgressionRepository();
    TaskSchedulerPort scheduler =
        new TaskSchedulerPort() {
          @Override
          public void runAsync(Runnable task) {
            task.run();
          }

          @Override
          public CancellableTask scheduleRepeatingAsync(
              Runnable task, Duration initialDelay, Duration period) {
            return () -> {};
          }
        };

    LifecycleReporterPort reporter =
        new LifecycleReporterPort() {
          @Override
          public void info(String message) {}

          @Override
          public void warning(String message) {}

          @Override
          public void severe(String message, Throwable throwable) {}
        };

    EventPublisherPort publisher =
        new EventPublisherPort() {
          @Override
          public void publishUnlock(TierKey key, UnlockMethod method, String source) {}

          @Override
          public void publishLock(TierKey key, String source) {}

          @Override
          public void publishEnchantmentAllowed(TierKey key, boolean allow, String source) {}
        };

    TimePort timePort = () -> Instant.parse("2026-11-20T12:00:00Z");

    GearProgressApplication app =
        new GearProgressApplication(tempDir, repo, scheduler, reporter, publisher, timePort);

    app.start();

    TierKey iron = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey diamond = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    assertTrue(app.isUnlocked(iron));
    assertTrue(app.isUnlocked(EquipmentCategory.ARMOR, "iron"));
    assertFalse(app.isUnlocked(diamond));
    assertFalse(app.isUnlocked(EquipmentCategory.ARMOR, "diamond"));

    assertTrue(app.isEnchantmentAllowed(iron));
    assertTrue(app.isEnchantmentAllowed(EquipmentCategory.ARMOR, "iron"));
    assertFalse(app.isEnchantmentAllowed(diamond));
    assertFalse(app.isEnchantmentAllowed(EquipmentCategory.ARMOR, "diamond"));

    assertTrue(app.currentState(iron).isPresent());
    assertTrue(app.currentState(EquipmentCategory.ARMOR, "iron").isPresent());

    // Unlock diamond via API
    boolean unlocked = app.unlock(EquipmentCategory.ARMOR, "diamond", "TEST").join();
    assertTrue(unlocked);
    assertTrue(app.isUnlocked(diamond));

    // Lock diamond via API
    boolean locked = app.lock(EquipmentCategory.ARMOR, "diamond", "TEST").join();
    assertTrue(locked);
    assertFalse(app.isUnlocked(diamond));

    // Unlock again with TierKey overload
    boolean unlockedAgain = app.unlock(diamond, "TEST").join();
    assertTrue(unlockedAgain);
    assertTrue(app.isUnlocked(diamond));

    // Lock with TierKey overload
    boolean lockedAgain = app.lock(diamond, "TEST").join();
    assertTrue(lockedAgain);
    assertFalse(app.isUnlocked(diamond));

    // Remaining time checks
    assertFalse(app.remainingTime(iron).isPresent());
    assertFalse(app.remainingTime(EquipmentCategory.ARMOR, "iron").isPresent());

    // Accessors
    assertNotNull(app.progressionService());
    assertNotNull(app.restrictionQueryService());
    assertNotNull(app.timerService());
    assertNotNull(app.equipmentRegistry());
    assertNotNull(app.pluginConfig());
    assertNotNull(app.localeCatalog());
    assertNotNull(app.eventPublisher());

    // Reload
    app.reload();
    app.shutdown();
  }
}
