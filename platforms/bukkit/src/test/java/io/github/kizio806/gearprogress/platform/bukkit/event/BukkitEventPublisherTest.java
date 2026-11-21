package io.github.kizio806.gearprogress.platform.bukkit.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BukkitEventPublisher Unit Tests")
class BukkitEventPublisherTest {

  private Server server;
  private PluginManager pluginManager;
  private BukkitEventPublisher publisher;

  @BeforeEach
  void setUp() {
    server = mock(Server.class);
    pluginManager = mock(PluginManager.class);
    when(server.getPluginManager()).thenReturn(pluginManager);
    publisher = new BukkitEventPublisher(server);
  }

  @Test
  @DisplayName("Publishes EquipmentUnlockEvent via PluginManager")
  void testPublishUnlock() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    publisher.publishUnlock(key, UnlockMethod.TIMER, "Timer");

    verify(pluginManager).callEvent(any(EquipmentUnlockEvent.class));
  }

  @Test
  @DisplayName("Publishes EquipmentLockEvent via PluginManager")
  void testPublishLock() {
    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    publisher.publishLock(key, "Admin");

    verify(pluginManager).callEvent(any(EquipmentLockEvent.class));
  }
}
