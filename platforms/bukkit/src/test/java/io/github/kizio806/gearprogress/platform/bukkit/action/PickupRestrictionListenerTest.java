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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PickupRestrictionListener Unit Tests")
class PickupRestrictionListenerTest {

  @BeforeAll
  static void beforeAll() {
    MockBukkitSetup.ensureInitialized();
  }

  private RestrictionQueryService queryService;
  private FeedbackNotifier feedback;
  private PickupRestrictionListener listener;

  @BeforeEach
  void setUp() {
    queryService = mock(RestrictionQueryService.class);
    feedback = mock(FeedbackNotifier.class);
    listener = new PickupRestrictionListener(queryService, feedback);
  }

  @Test
  @DisplayName("Player pickup item is cancelled when PICKUP is restricted")
  void testPickupRestricted() {
    EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
    Player player = mock(Player.class);
    Item itemEntity = mock(Item.class);
    ItemStack item = mock(ItemStack.class);

    when(event.getEntity()).thenReturn(player);
    when(event.getItem()).thenReturn(itemEntity);
    when(itemEntity.getItemStack()).thenReturn(item);
    when(item.getAmount()).thenReturn(1);
    when(item.getType()).thenReturn(Material.NETHERITE_SWORD);

    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "netherite");
    when(feedback.canBypass(player)).thenReturn(false);
    when(queryService.isActionRestricted(key, RestrictionType.PICKUP)).thenReturn(true);

    listener.onEntityPickup(event);

    verify(event).setCancelled(true);
    verify(feedback).notifyRestricted(player, key, RestrictionType.PICKUP);
  }

  @Test
  @DisplayName("Player pickup item is allowed when bypassed")
  void testPickupBypassed() {
    EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
    Player player = mock(Player.class);

    when(event.getEntity()).thenReturn(player);
    when(feedback.canBypass(player)).thenReturn(true);

    listener.onEntityPickup(event);

    verify(event, never()).setCancelled(true);
  }
}
