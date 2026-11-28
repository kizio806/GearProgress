package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EquipRestrictionListener Unit Tests")
class EquipRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private EquipRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new EquipRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("Right click equip is cancelled and notified when restricted")
  void testRightClickEquipRestricted() {
    PlayerInteractEvent event = mock(PlayerInteractEvent.class);
    Player player = mock(Player.class);
    ItemStack armor = mock(ItemStack.class);

    when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
    when(event.getPlayer()).thenReturn(player);
    when(event.getItem()).thenReturn(armor);
    when(armor.getAmount()).thenReturn(1);
    when(armor.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onPlayerInteract(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.EQUIP);
  }

  @Test
  @DisplayName("Inventory click into armor slot is cancelled when restricted")
  void testArmorSlotClickRestricted() {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Player player = mock(Player.class);
    ItemStack cursor = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getSlotType()).thenReturn(InventoryType.SlotType.ARMOR);
    when(event.getCursor()).thenReturn(cursor);
    when(event.getClick()).thenReturn(ClickType.LEFT);
    when(cursor.getAmount()).thenReturn(1);
    when(cursor.getType()).thenReturn(Material.NETHERITE_HELMET);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onInventoryClick(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.EQUIP);
  }

  @Test
  @DisplayName("Inventory shift click armor is cancelled when restricted")
  void testShiftClickArmorRestricted() {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getSlotType()).thenReturn(InventoryType.SlotType.CONTAINER);
    when(event.isShiftClick()).thenReturn(true);
    when(event.getCurrentItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.DIAMOND_BOOTS);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onInventoryClick(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.EQUIP);
  }

  @Test
  @DisplayName("Inventory drag into armor slot is cancelled when restricted")
  void testInventoryDragRestricted() {
    InventoryDragEvent event = mock(InventoryDragEvent.class);
    Player player = mock(Player.class);
    ItemStack cursor = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getRawSlots()).thenReturn(Set.of(6)); // Chestplate slot
    when(event.getOldCursor()).thenReturn(cursor);
    when(cursor.getAmount()).thenReturn(1);
    when(cursor.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onInventoryDrag(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.EQUIP);
  }

  @Test
  @DisplayName("Dispense armor onto player is cancelled when restricted")
  void testDispenseArmorRestricted() {
    BlockDispenseArmorEvent event = mock(BlockDispenseArmorEvent.class);
    Player player = mock(Player.class);
    ItemStack armor = mock(ItemStack.class);

    when(event.getTargetEntity()).thenReturn(player);
    when(event.getItem()).thenReturn(armor);
    when(armor.getAmount()).thenReturn(1);
    when(armor.getType()).thenReturn(Material.NETHERITE_CHESTPLATE);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onDispenseArmor(event);

    verify(event).setCancelled(true);
  }

  @Test
  @DisplayName("Number key hotbar swap into armor slot is cancelled when restricted")
  void testNumberKeyHotbarSwapRestricted() {
    InventoryClickEvent event = mock(InventoryClickEvent.class);
    Player player = mock(Player.class);
    org.bukkit.inventory.PlayerInventory playerInv =
        mock(org.bukkit.inventory.PlayerInventory.class);
    ItemStack hotbarItem = mock(ItemStack.class);

    when(event.getWhoClicked()).thenReturn(player);
    when(event.getSlotType()).thenReturn(InventoryType.SlotType.ARMOR);
    when(event.getClick()).thenReturn(ClickType.NUMBER_KEY);
    when(event.getHotbarButton()).thenReturn(2);
    when(player.getInventory()).thenReturn(playerInv);
    when(playerInv.getItem(2)).thenReturn(hotbarItem);
    when(hotbarItem.getAmount()).thenReturn(1);
    when(hotbarItem.getType()).thenReturn(Material.NETHERITE_BOOTS);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.EQUIP)).thenReturn(true);

    listener.onInventoryClick(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.EQUIP);
  }

  @Test
  @DisplayName("Non-restricted actions and bypass permissions pass without cancellation")
  void testAllowedAndBypassActions() {
    Player player = mock(Player.class);
    when(feedback.canBypass(player)).thenReturn(true);

    PlayerInteractEvent interactEvent = mock(PlayerInteractEvent.class);
    when(interactEvent.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
    when(interactEvent.getPlayer()).thenReturn(player);
    ItemStack armor = mock(ItemStack.class);
    when(armor.getAmount()).thenReturn(1);
    when(armor.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);
    when(interactEvent.getItem()).thenReturn(armor);
    listener.onPlayerInteract(interactEvent);
    verify(interactEvent, org.mockito.Mockito.never()).setCancelled(true);

    PlayerInteractEvent leftClick = mock(PlayerInteractEvent.class);
    when(leftClick.getAction()).thenReturn(Action.LEFT_CLICK_AIR);
    listener.onPlayerInteract(leftClick);
    verify(leftClick, org.mockito.Mockito.never()).setCancelled(true);

    Player nonBypassPlayer = mock(Player.class);
    when(feedback.canBypass(nonBypassPlayer)).thenReturn(false);
    PlayerInteractEvent swordInteract = mock(PlayerInteractEvent.class);
    when(swordInteract.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
    when(swordInteract.getPlayer()).thenReturn(nonBypassPlayer);
    ItemStack sword = mock(ItemStack.class);
    when(sword.getAmount()).thenReturn(1);
    when(sword.getType()).thenReturn(Material.DIAMOND_SWORD);
    when(swordInteract.getItem()).thenReturn(sword);
    listener.onPlayerInteract(swordInteract);
    verify(swordInteract, org.mockito.Mockito.never()).setCancelled(true);

    InventoryDragEvent dragEvent = mock(InventoryDragEvent.class);
    when(dragEvent.getWhoClicked()).thenReturn(nonBypassPlayer);
    when(dragEvent.getRawSlots()).thenReturn(Set.of(12, 13));
    listener.onInventoryDrag(dragEvent);
    verify(dragEvent, org.mockito.Mockito.never()).setCancelled(true);

    BlockDispenseArmorEvent dispenseEvent = mock(BlockDispenseArmorEvent.class);
    org.bukkit.entity.ArmorStand stand = mock(org.bukkit.entity.ArmorStand.class);
    when(dispenseEvent.getTargetEntity()).thenReturn(stand);
    listener.onDispenseArmor(dispenseEvent);
    verify(dispenseEvent, org.mockito.Mockito.never()).setCancelled(true);
  }
}
