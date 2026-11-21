package io.github.kizio806.gearprogress.application.config.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemLocaleLoaderTest {

  @Test
  void testExtractAndLoadBundledLocales(@TempDir Path tempDir) throws IOException {
    FileSystemLocaleLoader loader = new FileSystemLocaleLoader(tempDir);
    loader.ensureBundledLocales();

    Path enFile = tempDir.resolve("locales").resolve("en-US.yml");
    Path plFile = tempDir.resolve("locales").resolve("pl-PL.yml");

    assertTrue(Files.exists(enFile));
    assertTrue(Files.exists(plFile));

    LocaleCatalog catalogEn = loader.loadCatalog("en-US");
    assertEquals("en-US", catalogEn.selectedLocale());
    assertNotNull(catalogEn.message("general.prefix"));

    LocaleCatalog catalogPl = loader.loadCatalog("pl-PL");
    assertEquals("pl-PL", catalogPl.selectedLocale());
    assertTrue(catalogPl.message("blocking.craft").contains("Wytwarzanie"));

    // Fallback to en-US when invalid locale requested
    LocaleCatalog catalogUnknown = loader.loadCatalog("de-DE");
    assertEquals("en-US", catalogUnknown.selectedLocale());
  }

  @Test
  void testOperatorOverlay(@TempDir Path tempDir) throws IOException {
    FileSystemLocaleLoader loader = new FileSystemLocaleLoader(tempDir);
    loader.ensureBundledLocales();

    // Create an operator overlay for en-US
    Path enFile = tempDir.resolve("locales").resolve("en-US.yml");
    Files.writeString(
        enFile,
        """
        general:
          prefix: "<gold>[CustomPrefix]</gold> "
        """);

    LocaleCatalog catalog = loader.loadCatalog("en-US");
    assertEquals("<gold>[CustomPrefix]</gold> ", catalog.message("general.prefix"));
    // Other keys remain accessible from bundled fallback
    assertNotNull(catalog.message("general.plugin-reloaded"));
  }
}
