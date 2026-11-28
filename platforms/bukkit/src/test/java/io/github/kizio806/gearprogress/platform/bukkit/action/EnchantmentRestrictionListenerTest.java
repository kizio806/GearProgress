package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.service.RestrictionQueryService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnchantmentRestrictionListener Unit Tests")
class EnchantmentRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private EnchantmentRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new EnchantmentRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("PrepareItemEnchant is cancelled when enchantments are forbidden")
  void testPrepareItemEnchantForbidden() {
    PrepareItemEnchantEvent event = mock(PrepareItemEnchantEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getEnchanter()).thenReturn(player);
    when(event.getItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.DIAMOND_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isEnchantmentForbidden(key)).thenReturn(true);

    listener.onPrepareItemEnchant(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyEnchantmentForbidden(player, key);
  }

  @Test
  @DisplayName("EnchantItemEvent is cancelled when enchantments are forbidden")
  void testEnchantItemForbidden() {
    EnchantItemEvent event = mock(EnchantItemEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getEnchanter()).thenReturn(player);
    when(event.getItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.IRON_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "iron");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isEnchantmentForbidden(key)).thenReturn(true);

    listener.onEnchantItem(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyEnchantmentForbidden(player, key);
  }

  @Test
  @DisplayName("PrepareAnvil clears result when enchanting forbidden tier")
  void testPrepareAnvilForbidden() {
    PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
    AnvilView view = mock(AnvilView.class);
    Player player = mock(Player.class);
    ItemStack result = mock(ItemStack.class);

    when(event.getView()).thenReturn(view);
    when(view.getPlayer()).thenReturn(player);
    when(event.getResult()).thenReturn(result);
    when(result.getAmount()).thenReturn(1);
    when(result.getType()).thenReturn(Material.DIAMOND_CHESTPLATE);

    Map<org.bukkit.enchantments.Enchantment, Integer> mockEnchants =
        java.util.Collections.singletonMap(null, 1);
    when(result.getEnchantments()).thenReturn(mockEnchants);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isEnchantmentForbidden(key)).thenReturn(true);

    listener.onPrepareAnvil(event);

    verify(event).setResult(null);
    verify(feedback).notifyEnchantmentForbidden(player, key);
  }

  @Test
  @DisplayName("PrepareSmithing clears result when carrying enchantments to forbidden tier")
  void testPrepareSmithingForbidden() {
    PrepareSmithingEvent event = mock(PrepareSmithingEvent.class);
    InventoryView view = mock(InventoryView.class);
    Player player = mock(Player.class);
    ItemStack result = mock(ItemStack.class);

    when(event.getView()).thenReturn(view);
    when(view.getPlayer()).thenReturn(player);
    when(event.getResult()).thenReturn(result);
    when(result.getAmount()).thenReturn(1);
    when(result.getType()).thenReturn(Material.NETHERITE_SWORD);

    Map<org.bukkit.enchantments.Enchantment, Integer> mockEnchants =
        java.util.Collections.singletonMap(null, 1);
    when(result.getEnchantments()).thenReturn(mockEnchants);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isEnchantmentForbidden(key)).thenReturn(true);

    listener.onPrepareSmithing(event);

    verify(event).setResult(null);
    verify(feedback).notifyEnchantmentForbidden(player, key);
  }
}
