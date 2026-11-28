package io.github.kizio806.gearprogress.platform.bukkit.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("BukkitResourceExtractor Unit Tests")
class BukkitResourceExtractorTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("Extracts all default resources when files do not exist")
  void testExtractDefaultResources() {
    Plugin plugin = mock(Plugin.class);
    when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));
    when(plugin.getResource(anyString()))
        .thenAnswer(
            inv ->
                new ByteArrayInputStream("schema-version: 1\n".getBytes(StandardCharsets.UTF_8)));

    BukkitResourceExtractor.extractDefaultResources(plugin, tempDir);

    assertTrue(Files.exists(tempDir.resolve("config.yml")));
    assertTrue(Files.exists(tempDir.resolve("equipment.yml")));
    assertTrue(Files.exists(tempDir.resolve("timers.yml")));
    assertTrue(Files.exists(tempDir.resolve("storage.yml")));
    assertTrue(Files.exists(tempDir.resolve("locales/en-US.yml")));
    assertTrue(Files.exists(tempDir.resolve("locales/pl-PL.yml")));
  }
}
