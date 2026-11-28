package io.github.kizio806.gearprogress.platform.bukkit.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import org.bukkit.plugin.Plugin;

/**
 * Utility for extracting default bundled YAML configuration and locale files from the plugin JAR.
 */
public final class BukkitResourceExtractor {

  private BukkitResourceExtractor() {}

  /**
   * Extracts default resources to the plugin data folder if they do not exist.
   *
   * @param plugin the plugin instance
   * @param dataDirectory the target data folder
   */
  public static void extractDefaultResources(Plugin plugin, Path dataDirectory) {
    Objects.requireNonNull(plugin, "plugin must not be null");
    Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");

    copyResourceIfNotExists(plugin, "config.yml", dataDirectory.resolve("config.yml"));
    copyResourceIfNotExists(plugin, "equipment.yml", dataDirectory.resolve("equipment.yml"));
    copyResourceIfNotExists(plugin, "timers.yml", dataDirectory.resolve("timers.yml"));
    copyResourceIfNotExists(plugin, "storage.yml", dataDirectory.resolve("storage.yml"));
    copyResourceIfNotExists(
        plugin, "locales/en-US.yml", dataDirectory.resolve("locales/en-US.yml"));
    copyResourceIfNotExists(
        plugin, "locales/pl-PL.yml", dataDirectory.resolve("locales/pl-PL.yml"));
  }

  private static void copyResourceIfNotExists(Plugin plugin, String resourcePath, Path targetPath) {
    if (Files.exists(targetPath)) {
      return;
    }
    try (InputStream in = plugin.getResource(resourcePath)) {
      if (in != null) {
        Path parent = targetPath.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException ex) {
      plugin
          .getLogger()
          .warning("Could not extract bundled resource " + resourcePath + ": " + ex.getMessage());
    }
  }
}
