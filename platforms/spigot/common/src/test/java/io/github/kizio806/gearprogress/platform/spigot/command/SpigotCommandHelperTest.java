package io.github.kizio806.gearprogress.platform.spigot.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SpigotCommandHelper Unit Tests")
class SpigotCommandHelperTest {

  private BukkitAudiences adventure;
  private Audience audience;
  private LocaleCatalog locales;
  private SpigotCommandHelper helper;

  @BeforeEach
  void setUp() {
    adventure = mock(BukkitAudiences.class);
    audience = mock(Audience.class);
    when(adventure.sender(any())).thenReturn(audience);
    locales =
        LocaleCatalog.of(
            Map.of(
                "general.no-permission", "<red>No permission!</red>",
                "test.key", "Hello %name%!"));
    helper = new SpigotCommandHelper(adventure, locales);
  }

  @Test
  @DisplayName("send message dispatches to audience")
  void testSend() {
    CommandSender sender = mock(CommandSender.class);
    helper.send(sender, "test.key", Map.of("name", "Player"));
    verify(audience).sendMessage(any(Component.class));

    helper.send(sender, "general.no-permission");
    helper.sendMessage(sender, Component.text("custom message"));
    assertEquals(locales, helper.locales());
  }

  @Test
  @DisplayName("requirePermission checks permissions accurately")
  void testRequirePermission() {
    CommandSender sender = mock(CommandSender.class);
    when(sender.hasPermission("gearprogress.admin")).thenReturn(false);
    when(sender.hasPermission("gearprogress.custom")).thenReturn(false);
    when(sender.isOp()).thenReturn(false);

    assertFalse(helper.requirePermission(sender, "gearprogress.custom"));

    when(sender.hasPermission("gearprogress.custom")).thenReturn(true);
    assertTrue(helper.requirePermission(sender, "gearprogress.custom"));
  }

  @Test
  @DisplayName("matchPrefix filters candidates correctly")
  void testMatchPrefix() {
    List<String> matches = helper.matchPrefix("un", List.of("unlock", "lock", "update", "status"));
    assertEquals(List.of("unlock"), matches);
  }
}
