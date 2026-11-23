package io.github.kizio806.gearprogress.platform.bukkit.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.service.ProgressionService;
import io.github.kizio806.gearprogress.application.service.TimerService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import io.github.kizio806.gearprogress.platform.bukkit.testutil.MockBukkitSetup;
import java.time.Duration;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProgressionGuiViewTest {

  @BeforeAll
  static void setupMockBukkit() {
    MockBukkitSetup.ensureInitialized();
  }

  @Test
  void testConstructorNullCheck() {
    assertThrows(NullPointerException.class, () -> new ProgressionGuiView(null));
  }

  @Test
  void testCreateInventoryPopulatesItemsCorrectly() {
    GearProgressApplication application = mock(GearProgressApplication.class);
    EquipmentRegistry registry = mock(EquipmentRegistry.class);
    ProgressionService progressionService = mock(ProgressionService.class);
    TimerService timerService = mock(TimerService.class);

    when(application.equipmentRegistry()).thenReturn(registry);
    when(application.progressionService()).thenReturn(progressionService);
    when(application.timerService()).thenReturn(timerService);

    Map<TierKey, EquipmentDefinition> definitions = new LinkedHashMap<>();

    // Unlocked armor
    TierKey unlockedKey = TierKey.of(EquipmentCategory.ARMOR, "leather");
    EquipmentDefinition unlockedDef =
        new EquipmentDefinition(
            unlockedKey,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed());
    definitions.put(unlockedKey, unlockedDef);
    when(progressionService.isUnlocked(unlockedKey)).thenReturn(true);
    when(progressionService.isEnchantmentAllowed(unlockedKey)).thenReturn(true);

    // Locked weapon with timer and restrictions
    TierKey lockedKey = TierKey.of(EquipmentCategory.WEAPONS, "diamond");
    EquipmentDefinition lockedDef =
        new EquipmentDefinition(
            lockedKey,
            UnlockCondition.timer(Duration.ofDays(7)),
            new RestrictionPolicy(EnumSet.of(RestrictionType.CRAFT, RestrictionType.USE)),
            EnchantmentPolicy.forbidden());
    definitions.put(lockedKey, lockedDef);
    when(progressionService.isUnlocked(lockedKey)).thenReturn(false);
    when(progressionService.isEnchantmentAllowed(lockedKey)).thenReturn(false);
    when(timerService.timeRemaining(lockedKey))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(3), false)));

    // Other categories for material coverage
    TierKey toolKey = TierKey.of(EquipmentCategory.TOOLS, "iron");
    definitions.put(
        toolKey,
        new EquipmentDefinition(
            toolKey,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed()));

    TierKey shieldKey = TierKey.of(EquipmentCategory.SHIELD, "default");
    definitions.put(
        shieldKey,
        new EquipmentDefinition(
            shieldKey,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed()));

    TierKey elytraKey = TierKey.of(EquipmentCategory.ELYTRA, "default");
    definitions.put(
        elytraKey,
        new EquipmentDefinition(
            elytraKey,
            UnlockCondition.always(),
            RestrictionPolicy.none(),
            EnchantmentPolicy.allowed()));

    when(registry.all()).thenReturn(definitions);

    ProgressionGuiView gui = new ProgressionGuiView(application);
    Inventory inventory = gui.createInventory();

    assertNotNull(inventory);
    assertEquals(54, inventory.getSize());
    assertTrue(inventory.getHolder() instanceof ProgressionGuiHolder);

    // Slot 0 should be leather armor (unlocked)
    ItemStack slot0 = inventory.getItem(0);
    assertNotNull(slot0);
    assertEquals(Material.LEATHER_CHESTPLATE, slot0.getType());

    // Slot 1 should be diamond sword (locked)
    ItemStack slot1 = inventory.getItem(1);
    assertNotNull(slot1);
    assertEquals(Material.DIAMOND_SWORD, slot1.getType());

    // Slot 49 should be summary nether star
    ItemStack summary = inventory.getItem(49);
    assertNotNull(summary);
    assertEquals(Material.NETHER_STAR, summary.getType());

    // Decorative panes in bottom row
    ItemStack pane = inventory.getItem(45);
    assertNotNull(pane);
    assertEquals(Material.GRAY_STAINED_GLASS_PANE, pane.getType());
  }

  @Test
  void testOpenPlayerGui() {
    GearProgressApplication application = mock(GearProgressApplication.class);
    EquipmentRegistry registry = mock(EquipmentRegistry.class);
    ProgressionService progressionService = mock(ProgressionService.class);
    TimerService timerService = mock(TimerService.class);

    when(application.equipmentRegistry()).thenReturn(registry);
    when(application.progressionService()).thenReturn(progressionService);
    when(application.timerService()).thenReturn(timerService);
    when(registry.all()).thenReturn(Map.of());

    ProgressionGuiView gui = new ProgressionGuiView(application);
    Player player = mock(Player.class);

    gui.open(player);
    verify(player).openInventory(any(Inventory.class));

    assertThrows(NullPointerException.class, () -> gui.open(null));
  }
}
