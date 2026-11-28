package io.github.kizio806.gearprogress.platform.bukkit.event;

import io.github.kizio806.gearprogress.application.port.EventPublisherPort;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.util.Objects;
import org.bukkit.Server;

/** Publishes GearProgress domain events to the Bukkit/Paper PluginManager event bus. */
public final class BukkitEventPublisher implements EventPublisherPort {

  private final Server server;

  public BukkitEventPublisher(Server server) {
    this.server = Objects.requireNonNull(server, "server must not be null");
  }

  @Override
  public void publishUnlock(TierKey key, UnlockMethod method, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(method, "method must not be null");
    Objects.requireNonNull(source, "source must not be null");
    EquipmentUnlockEvent event = new EquipmentUnlockEvent(key, method, source);
    server.getPluginManager().callEvent(event);
  }

  @Override
  public void publishLock(TierKey key, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(source, "source must not be null");
    EquipmentLockEvent event = new EquipmentLockEvent(key, source);
    server.getPluginManager().callEvent(event);
  }

  /**
   * Dispatches an enchantment allowance state change event. Currently reserved for future custom
   * event subscribers.
   *
   * @param key the tier key
   * @param allow whether enchantments are allowed
   * @param source operator or trigger name
   */
  @Override
  public void publishEnchantmentAllowed(TierKey key, boolean allow, String source) {
    Objects.requireNonNull(key, "key must not be null");
    Objects.requireNonNull(source, "source must not be null");
  }
}
