package io.github.kizio806.gearprogress.platform.bukkit.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class ProgressionGuiListenerTest {

  @Test
  void testClickInsideProgressionGuiIsCancelled() {
    ProgressionGuiListener listener = new ProgressionGuiListener();
    Inventory inventory = mock(Inventory.class);
    ProgressionGuiHolder holder = new ProgressionGuiHolder();
    when(inventory.getHolder()).thenReturn(holder);

    InventoryView view = mock(InventoryView.class);
    when(view.getTopInventory()).thenReturn(inventory);

    InventoryClickEvent event =
        new InventoryClickEvent(
            view, InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    listener.onInventoryClick(event);
    assertTrue(event.isCancelled());
  }

  @Test
  void testClickInsideOtherInventoryIsNotCancelled() {
    ProgressionGuiListener listener = new ProgressionGuiListener();
    Inventory inventory = mock(Inventory.class);
    InventoryHolder otherHolder = mock(InventoryHolder.class);
    when(inventory.getHolder()).thenReturn(otherHolder);

    InventoryView view = mock(InventoryView.class);
    when(view.getTopInventory()).thenReturn(inventory);

    InventoryClickEvent event =
        new InventoryClickEvent(
            view, InventoryType.SlotType.CONTAINER, 0, ClickType.LEFT, InventoryAction.PICKUP_ALL);

    listener.onInventoryClick(event);
    assertFalse(event.isCancelled());
  }

  @Test
  void testDragInsideProgressionGuiIsCancelled() {
    ProgressionGuiListener listener = new ProgressionGuiListener();
    Inventory inventory = mock(Inventory.class);
    ProgressionGuiHolder holder = new ProgressionGuiHolder();
    when(inventory.getHolder()).thenReturn(holder);

    InventoryView view = mock(InventoryView.class);
    when(view.getTopInventory()).thenReturn(inventory);

    InventoryDragEvent event =
        new InventoryDragEvent(
            view, null, mock(ItemStack.class), false, Map.of(0, mock(ItemStack.class)));

    listener.onInventoryDrag(event);
    assertTrue(event.isCancelled());
  }

  @Test
  void testDragInsideOtherInventoryIsNotCancelled() {
    ProgressionGuiListener listener = new ProgressionGuiListener();
    Inventory inventory = mock(Inventory.class);
    when(inventory.getHolder()).thenReturn(null);

    InventoryView view = mock(InventoryView.class);
    when(view.getTopInventory()).thenReturn(inventory);

    InventoryDragEvent event =
        new InventoryDragEvent(
            view, null, mock(ItemStack.class), false, Map.of(0, mock(ItemStack.class)));

    listener.onInventoryDrag(event);
    assertFalse(event.isCancelled());
  }
}
