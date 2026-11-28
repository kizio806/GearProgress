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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UseRestrictionListener Unit Tests")
class UseRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private UseRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new UseRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("Player interact with restricted item is cancelled and notifies")
  void testInteractRestricted() {
    PlayerInteractEvent event = mock(PlayerInteractEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getPlayer()).thenReturn(player);
    when(event.getItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.DIAMOND_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.USE)).thenReturn(true);

    listener.onPlayerInteract(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.USE);
  }

  @Test
  @DisplayName("Player interact is allowed when player has bypass")
  void testInteractBypassed() {
    PlayerInteractEvent event = mock(PlayerInteractEvent.class);
    Player player = mock(Player.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getPlayer()).thenReturn(player);
    when(event.getItem()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(feedback.canBypass(player)).thenReturn(true);

    listener.onPlayerInteract(event);

    verify(event, never()).setCancelled(true);
  }

  @Test
  @DisplayName("EntityDamageByEntity is cancelled when weapon use is restricted")
  void testEntityDamageRestricted() {
    EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
    Player player = mock(Player.class);
    PlayerInventory inv = mock(PlayerInventory.class);
    ItemStack weapon = mock(ItemStack.class);

    when(event.getDamager()).thenReturn(player);
    when(player.getInventory()).thenReturn(inv);
    when(inv.getItemInMainHand()).thenReturn(weapon);
    when(weapon.getAmount()).thenReturn(1);
    when(weapon.getType()).thenReturn(Material.NETHERITE_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.USE)).thenReturn(true);

    listener.onEntityDamage(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.USE);
  }
}
