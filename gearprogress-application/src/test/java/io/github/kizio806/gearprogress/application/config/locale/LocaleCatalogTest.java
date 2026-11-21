package io.github.kizio806.gearprogress.application.config.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class LocaleCatalogTest {

  @Test
  void testCatalogLookupAndFallback() {
    Map<String, String> english =
        Map.of(
            "cmd.help", "Help message",
            "cmd.reload", "Reloaded successfully");

    Map<String, String> polish = Map.of("cmd.help", "Wiadomosc pomocy");

    LocaleCatalog catalog = new LocaleCatalog("pl-PL", english, polish);

    assertEquals("pl-PL", catalog.selectedLocale());
    assertEquals("Wiadomosc pomocy", catalog.message("cmd.help"));
    // Fallback to English for missing key
    assertEquals("Reloaded successfully", catalog.message("cmd.reload"));
    // Missing in both
    assertTrue(catalog.message("cmd.nonexistent").contains("missing message"));
  }

  @Test
  void testPlaceholders() {
    LocaleCatalog catalog =
        LocaleCatalog.of(Map.of("alert.unlock", "Tier %tier% in %category% has unlocked!"));

    String formatted =
        catalog.message(
            "alert.unlock",
            Map.of(
                "tier", "Diamond",
                "category", "Armor"));

    assertEquals("Tier Diamond in Armor has unlocked!", formatted);
  }
}
