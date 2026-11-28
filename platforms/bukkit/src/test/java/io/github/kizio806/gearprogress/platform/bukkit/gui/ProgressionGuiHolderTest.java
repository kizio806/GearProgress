package io.github.kizio806.gearprogress.platform.bukkit.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.Test;

class ProgressionGuiHolderTest {

  @Test
  void testHolderGetAndSetInventory() {
    ProgressionGuiHolder holder = new ProgressionGuiHolder();
    assertNull(holder.getInventory());

    Inventory inventory = mock(Inventory.class);
    holder.setInventory(inventory);
    assertEquals(inventory, holder.getInventory());
  }
}
