package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InventoryMoveRestrictionListener Unit Tests")
class InventoryMoveRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private InventoryMoveRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new InventoryMoveRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("Inventory click is cancelled when moving restricted item")
  void testInventoryClickRestricted() {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Player player = mock(Player.class);
    ItemStack current = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getCurrentItem()).thenReturn(current);
    when(current.getAmount()).thenReturn(1);
    when(current.getType()).thenReturn(Material.NETHERITE_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.INVENTORY_MOVE)).thenReturn(true);

    listener.onInventoryClick(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.INVENTORY_MOVE);
  }

  @Test
  @DisplayName("Inventory drag is cancelled when moving restricted item")
  void testInventoryDragRestricted() {
    InventoryDragEvent event = mock(InventoryDragEvent.class);
    Player player = mock(Player.class);
    ItemStack cursor = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getOldCursor()).thenReturn(cursor);
    when(cursor.getAmount()).thenReturn(1);
    when(cursor.getType()).thenReturn(Material.NETHERITE_PICKAXE);

    TierKey key = TierKey.of(EquipmentCategory.TOOLS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.INVENTORY_MOVE)).thenReturn(true);

    listener.onInventoryDrag(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.INVENTORY_MOVE);
  }

  @Test
  @DisplayName("Player drop item is cancelled when INVENTORY_MOVE is restricted")
  void testDropItemRestricted() {
    PlayerDropItemEvent event = mock(PlayerDropItemEvent.class);
    Player player = mock(Player.class);
    Item itemEntity = mock(Item.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getPlayer()).thenReturn(player);
    when(event.getItemDrop()).thenReturn(itemEntity);
    when(itemEntity.getItemStack()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.NETHERITE_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.INVENTORY_MOVE)).thenReturn(true);

    listener.onPlayerDropItem(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.INVENTORY_MOVE);
  }

  @Test
  @DisplayName("Player swap hands is cancelled when INVENTORY_MOVE is restricted")
  void testSwapHandsRestricted() {
    PlayerSwapHandItemsEvent event = mock(PlayerSwapHandItemsEvent.class);
    Player player = mock(Player.class);
    ItemStack mainHand = mock(ItemStack.class);

    when(event.getPlayer()).thenReturn(player);
    when(event.getMainHandItem()).thenReturn(mainHand);
    when(mainHand.getAmount()).thenReturn(1);
    when(mainHand.getType()).thenReturn(Material.NETHERITE_AXE);

    TierKey key = TierKey.of(EquipmentCategory.TOOLS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.INVENTORY_MOVE)).thenReturn(true);

    listener.onPlayerSwapHandItems(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.INVENTORY_MOVE);
  }
}
