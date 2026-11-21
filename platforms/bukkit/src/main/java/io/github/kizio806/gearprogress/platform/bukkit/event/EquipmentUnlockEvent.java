package io.github.kizio806.gearprogress.platform.bukkit.event;

import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Bukkit event fired when an equipment tier is unlocked. */
public final class EquipmentUnlockEvent extends Event implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  private final TierKey tierKey;
  private final UnlockMethod unlockMethod;
  private final String source;
  private boolean cancelled;

  public EquipmentUnlockEvent(TierKey tierKey, UnlockMethod unlockMethod, String source) {
    super(true); // Asynchronous event
    this.tierKey = Objects.requireNonNull(tierKey, "tierKey must not be null");
    this.unlockMethod = Objects.requireNonNull(unlockMethod, "unlockMethod must not be null");
    this.source = Objects.requireNonNull(source, "source must not be null");
  }

  public TierKey tierKey() {
    return tierKey;
  }

  public EquipmentCategory category() {
    return tierKey.category();
  }

  public String tier() {
    return tierKey.tier();
  }

  public UnlockMethod unlockMethod() {
    return unlockMethod;
  }

  public String source() {
    return source;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}
