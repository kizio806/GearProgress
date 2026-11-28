package io.github.kizio806.gearprogress.platform.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.kizio806.gearprogress.application.config.locale.LocaleCatalog;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CommandText Unit Tests")
class CommandTextTest {

  private LocaleCatalog locales;
  private CommandText commandText;

  @BeforeEach
  void setUp() {
    locales =
        LocaleCatalog.of(
            Map.of(
                "general.prefix", "<gray>[Prefix]</gray> ",
                "test.simple", "<green>Hello World</green>",
                "test.param", "<yellow>Hello %name%</yellow>"));
    commandText = new CommandText(locales);
  }

  @Test
  @DisplayName("Resolves raw text and placeholders")
  void testMessageResolution() {
    assertEquals("<green>Hello World</green>", commandText.message("test.simple"));
    assertEquals(
        "<yellow>Hello Steve</yellow>", commandText.message("test.param", Map.of("name", "Steve")));
  }

  @Test
  @DisplayName("Deserializes into adventure components")
  void testComponentCreation() {
    Component comp = commandText.component("test.simple");
    assertNotNull(comp);

    Component paramComp = commandText.component("test.param", Map.of("name", "Alex"));
    assertNotNull(paramComp);
  }

  @Test
  @DisplayName("Sends messages to command sender")
  void testSendMessages() {
    CommandSender sender = mock(CommandSender.class);

    commandText.send(sender, "test.simple");
    verify(sender).sendMessage(commandText.component("test.simple"));

    commandText.send(sender, "test.param", Map.of("name", "Alex"));
    verify(sender).sendMessage(commandText.component("test.param", Map.of("name", "Alex")));

    commandText.sendPrefixed(sender, "test.simple");
    Component expected =
        commandText.component("general.prefix").append(commandText.component("test.simple"));
    verify(sender).sendMessage(expected);

    commandText.sendPrefixed(sender, "test.param", Map.of("name", "Alex"));
    Component expectedParam =
        commandText
            .component("general.prefix")
            .append(commandText.component("test.param", Map.of("name", "Alex")));
    verify(sender).sendMessage(expectedParam);
  }
}
