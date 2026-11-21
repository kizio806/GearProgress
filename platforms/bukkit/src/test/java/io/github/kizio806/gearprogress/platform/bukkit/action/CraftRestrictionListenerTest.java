package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CraftRestrictionListener Unit Tests")
class CraftRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private CraftRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new CraftRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("PrepareCraft cancels result when tier craft is restricted")
  void testPrepareCraftRestricted() {
    PrepareItemCraftEvent event = mock(PrepareItemCraftEvent.class);
    CraftingInventory inv = mock(CraftingInventory.class);
    InventoryView view = mock(InventoryView.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getInventory()).thenReturn(inv);
    when(event.getView()).thenReturn(view);
    when(view.getPlayer()).thenReturn(player);
    when(inv.getResult()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.DIAMOND_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.CRAFT)).thenReturn(true);

    listener.onPrepareCraft(event);

    verify(inv).setResult(null);
  }

  @Test
  @DisplayName("PrepareCraft does not clear result when bypassed")
  void testPrepareCraftBypassed() {
    PrepareItemCraftEvent event = mock(PrepareItemCraftEvent.class);
    CraftingInventory inv = mock(CraftingInventory.class);
    InventoryView view = mock(InventoryView.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getInventory()).thenReturn(inv);
    when(event.getView()).thenReturn(view);
    when(view.getPlayer()).thenReturn(player);
    when(inv.getResult()).thenReturn(item);
    when(feedback.canBypass(player)).thenReturn(true);

    listener.onPrepareCraft(event);

    verify(inv, never()).setResult(null);
  }

  @Test
  @DisplayName("CraftItemEvent is cancelled and notifies player when restricted")
  void testCraftItemEventRestricted() {
    CraftItemEvent event = mock(CraftItemEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getCurrentItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.CRAFT)).thenReturn(true);

    listener.onCraft(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.CRAFT);
  }

  @Test
  @DisplayName("PrepareSmithing clears result when tier craft is restricted")
  void testPrepareSmithingRestricted() {
    PrepareSmithingEvent event = mock(PrepareSmithingEvent.class);
    InventoryView view = mock(InventoryView.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getView()).thenReturn(view);
    when(view.getPlayer()).thenReturn(player);
    when(event.getResult()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.NETHERITE_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.CRAFT)).thenReturn(true);

    listener.onPrepareSmithing(event);

    verify(event).setResult(null);
  }
}
