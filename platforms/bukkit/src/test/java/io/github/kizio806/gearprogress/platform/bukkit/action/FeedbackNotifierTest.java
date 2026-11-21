package io.github.kizio806.gearprogress.platform.bukkit.action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import io.github.kizio806.gearprogress.application.config.spec.PluginConfigSpec;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.rule.RestrictionType;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FeedbackNotifier Unit Tests")
class FeedbackNotifierTest {

  private PluginConfigSpec config;
  private LocaleCatalog locales;
  private net.kyori.adventure.audience.Audience audience;
  private FeedbackNotifier notifier;

  @BeforeEach
  void setUp() {
    config =
        new PluginConfigSpec(
            "en-US",
            20L,
            true,
            true,
            true,
            "minecraft:block.chest.locked",
            true,
            "gearprogress.bypass");
    locales =
        LocaleCatalog.of(
            Map.of(
                "general.prefix", "<gray>[GP]</gray> ",
                "blocking.craft", "<red>Crafting %tier% is locked!</red>",
                "blocking.enchantment", "<red>Enchanting %tier% is forbidden!</red>"));
    audience = mock(net.kyori.adventure.audience.Audience.class);
    notifier = new FeedbackNotifier(config, locales, p -> audience);
  }

  @Test
  @DisplayName("canBypass returns true when player has permission or is op")
  void testCanBypass() {
    assertFalse(notifier.canBypass(null));

    Player player = mock(Player.class);
    when(player.hasPermission("gearprogress.bypass")).thenReturn(false);
    when(player.isOp()).thenReturn(false);
    assertFalse(notifier.canBypass(player));

    when(player.hasPermission("gearprogress.bypass")).thenReturn(true);
    assertTrue(notifier.canBypass(player));

    when(player.hasPermission("gearprogress.bypass")).thenReturn(false);
    when(player.isOp()).thenReturn(true);
    assertTrue(notifier.canBypass(player));
  }

  @Test
  @DisplayName("canBypass returns false when bypass config is disabled")
  void testCanBypassDisabledInConfig() {
    PluginConfigSpec disabledBypass =
        new PluginConfigSpec(
            "en-US",
            20L,
            true,
            true,
            true,
            "minecraft:block.chest.locked",
            false,
            "gearprogress.bypass");
    FeedbackNotifier disabledNotifier =
        new FeedbackNotifier(disabledBypass, locales, p -> audience);

    Player player = mock(Player.class);
    when(player.hasPermission("gearprogress.bypass")).thenReturn(true);
    assertFalse(disabledNotifier.canBypass(player));
  }

  @Test
  @DisplayName("notifyRestricted sends chat and action bar to player")
  void testNotifyRestricted() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    when(player.getUniqueId()).thenReturn(uuid);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    notifier.notifyRestricted(player, key, RestrictionType.CRAFT);

    verify(audience, times(1)).sendMessage(any(Component.class));
    verify(audience, times(1)).sendActionBar(any(Component.class));
  }

  @Test
  @DisplayName("notifyRestricted throttles rapid subsequent notifications")
  void testNotifyRestrictedThrottling() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    when(player.getUniqueId()).thenReturn(uuid);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    notifier.notifyRestricted(player, key, RestrictionType.CRAFT);
    notifier.notifyRestricted(player, key, RestrictionType.CRAFT);

    // Only called once due to throttling cooldown
    verify(audience, times(1)).sendMessage(any(Component.class));
  }

  @Test
  @DisplayName("notifyEnchantmentForbidden sends feedback to player")
  void testNotifyEnchantmentForbidden() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    when(player.getUniqueId()).thenReturn(uuid);

    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    notifier.notifyEnchantmentForbidden(player, key);

    verify(audience, times(1)).sendMessage(any(Component.class));
    verify(audience, times(1)).sendActionBar(any(Component.class));
  }

  @Test
  @DisplayName("Null player does not throw exception")
  void testNullPlayerSafelyHandled() {
    TierKey key = TierKey.of(EquipmentCategory.ARMOR, "diamond");
    notifier.notifyRestricted(null, key, RestrictionType.CRAFT);
    notifier.notifyEnchantmentForbidden(null, key);
  }
}
