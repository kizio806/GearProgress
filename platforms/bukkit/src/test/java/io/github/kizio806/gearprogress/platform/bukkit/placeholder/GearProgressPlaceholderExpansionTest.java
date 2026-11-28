package io.github.kizio806.gearprogress.platform.bukkit.placeholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.GearProgressApplication;
import io.github.kizio806.gearprogress.application.model.EquipmentDefinition;
import io.github.kizio806.gearprogress.application.model.EquipmentRegistry;
import io.github.kizio806.gearprogress.application.service.ProgressionService;
import io.github.kizio806.gearprogress.application.service.TimerService;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.EnchantmentPolicy;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionPolicy;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockCondition;
import io.github.kizio806.gearprogress.core.time.TimeRemaining;
import java.time.Duration;
import java.util.Optional;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GearProgressPlaceholderExpansion Unit Tests")
class GearProgressPlaceholderExpansionTest {

  private GearProgressApplication application;
  private ProgressionService progressionService;
  private TimerService timerService;
  private EquipmentRegistry registry;
  private GearProgressPlaceholderExpansion expansion;

  @BeforeEach
  void setUp() {
    application = mock(GearProgressApplication.class);
    progressionService = mock(ProgressionService.class);
    timerService = mock(TimerService.class);
    registry = new EquipmentRegistry();

    when(application.progressionService()).thenReturn(progressionService);
    when(application.timerService()).thenReturn(timerService);
    when(application.equipmentRegistry()).thenReturn(registry);

    expansion = new GearProgressPlaceholderExpansion(application);
  }

  @Test
  @DisplayName("Basic metadata getters return expected constants")
  void testMetadata() {
    assertEquals("gearprogress", expansion.getIdentifier());
    assertEquals("kizio806", expansion.getAuthor());
    assertEquals("1.0.0", expansion.getVersion());
    assertTrue(expansion.persist());
    assertTrue(expansion.canRegister());
  }

  @Test
  @DisplayName("diamond_unlocked and netherite_unlocked placeholders")
  void testTierUnlockedPlaceholders() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    TierKey diamondArmor = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    TierKey netheriteArmor = TierKey.of(EquipmentCategory.ARMOR, "netherite");

    when(progressionService.isUnlocked(diamondArmor)).thenReturn(true);
    when(progressionService.isUnlocked(netheriteArmor)).thenReturn(false);

    assertEquals("true", expansion.onRequest(player, "diamond_unlocked"));
    assertEquals("false", expansion.onRequest(player, "netherite_unlocked"));
  }

  @Test
  @DisplayName("current_stage placeholder resolves highest unlocked armor tier")
  void testCurrentStage() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    TierKey ironArmor = TierKey.of(EquipmentCategory.ARMOR, "iron");
    when(progressionService.isUnlocked(ironArmor)).thenReturn(true);

    assertEquals("iron", expansion.onRequest(player, "current_stage"));
  }

  @Test
  @DisplayName("time_remaining placeholder formats countdown or status")
  void testTimeRemainingPlaceholder() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    when(progressionService.isUnlocked(key)).thenReturn(false);
    when(timerService.timeRemaining(key))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofHours(5), false)));

    assertEquals("5h", expansion.onRequest(player, "time_remaining_armor_diamond"));

    when(progressionService.isUnlocked(key)).thenReturn(true);
    assertEquals("unlocked", expansion.onRequest(player, "time_remaining_armor_diamond"));
  }

  @Test
  @DisplayName("is_unlocked and status placeholders return correct states")
  void testIsUnlockedAndStatus() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    TierKey key = TierKey.of(EquipmentCategory.WEAPONS, "diamond");

    when(progressionService.isUnlocked(key)).thenReturn(true);
    assertEquals("true", expansion.onRequest(player, "is_unlocked_weapons_diamond"));
    assertEquals("UNLOCKED", expansion.onRequest(player, "status_weapons_diamond"));

    when(progressionService.isUnlocked(key)).thenReturn(false);
    assertEquals("false", expansion.onRequest(player, "is_unlocked_weapons_diamond"));
    assertEquals("LOCKED", expansion.onRequest(player, "status_weapons_diamond"));
  }

  @Test
  @DisplayName("next_unlock finds closest upcoming locked tier")
  void testNextUnlock() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    TierKey key1 = TierKey.of(EquipmentCategory.ARMOR, "iron");
    TierKey key2 = TierKey.of(EquipmentCategory.ARMOR, "diamond");

    EquipmentDefinition def1 =
        new EquipmentDefinition(
            key1, UnlockCondition.always(), RestrictionPolicy.none(), EnchantmentPolicy.allowed());
    EquipmentDefinition def2 =
        new EquipmentDefinition(
            key2,
            UnlockCondition.timer(Duration.ofDays(7)),
            RestrictionPolicy.all(),
            EnchantmentPolicy.forbidden());

    registry.register(def1);
    registry.register(def2);

    when(progressionService.isUnlocked(key1)).thenReturn(true);
    when(progressionService.isUnlocked(key2)).thenReturn(false);
    when(timerService.timeRemaining(key2))
        .thenReturn(Optional.of(new TimeRemaining(Duration.ofDays(2), false)));

    String nextUnlock = expansion.onRequest(player, "next_unlock");
    assertNotNull(nextUnlock);
    assertTrue(nextUnlock.contains("armor diamond"));
    assertTrue(nextUnlock.contains("2d"));
  }

  @Test
  @DisplayName("Unknown placeholder returns null")
  void testUnknownPlaceholder() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    assertNull(expansion.onRequest(player, "unknown_placeholder_xyz"));
  }

  @Test
  @DisplayName("Placeholder edge cases return fallback strings")
  void testPlaceholderFallbacks() {
    OfflinePlayer player = mock(OfflinePlayer.class);

    when(progressionService.isUnlocked(any(TierKey.class))).thenReturn(false);
    assertEquals("none", expansion.onRequest(player, "current_stage"));

    assertEquals("none", expansion.onRequest(player, "next_unlock"));

    assertEquals("invalid", expansion.onRequest(player, "time_remaining_invalid"));
    assertEquals("invalid", expansion.onRequest(player, "time_remaining_armor_"));
    assertEquals("invalid", expansion.onRequest(player, "time_remaining_unknowncat_tier"));

    TierKey noTimerKey = TierKey.of(EquipmentCategory.ARMOR, "leather");
    when(progressionService.isUnlocked(noTimerKey)).thenReturn(false);
    when(timerService.timeRemaining(noTimerKey)).thenReturn(Optional.empty());
    assertEquals("none", expansion.onRequest(player, "time_remaining_armor_leather"));

    assertEquals("false", expansion.onRequest(player, "is_unlocked_invalid"));
    assertEquals("UNKNOWN", expansion.onRequest(player, "status_invalid"));
  }
}
