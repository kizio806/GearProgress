package io.github.kizio806.gearprogress.platform.bukkit.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Prevents players from modifying items in the progression view GUI. */
public final class ProgressionGuiListener implements Listener {

  /**
   * Cancels click events inside the progression view GUI.
   *
   * @param event the inventory click event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getInventory().getHolder() instanceof ProgressionGuiHolder) {
      event.setCancelled(true);
    }
  }

  /**
   * Cancels drag events inside the progression view GUI.
   *
   * @param event the inventory drag event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getInventory().getHolder() instanceof ProgressionGuiHolder) {
      event.setCancelled(true);
    }
  }
}
