package io.github.kizio806.gearprogress.platform.bukkit.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** InventoryHolder marker implementation for the progression view inventory. */
public final class ProgressionGuiHolder implements InventoryHolder {

  private Inventory inventory;

  /**
   * Sets the underlying inventory reference.
   *
   * @param inventory the created inventory
   */
  public void setInventory(Inventory inventory) {
    this.inventory = inventory;
  }

  @Override
  public Inventory getInventory() {
    return inventory;
  }
}
