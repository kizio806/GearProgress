package io.github.kizio806.gearprogress.application.config.locale;

import io.github.kizio806.gearprogress.application.config.loader.YamlConfigLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Extracts and loads locale message catalogs from the filesystem and bundled resources. */
public final class FileSystemLocaleLoader {

  private static final List<String> BUNDLED_LOCALES = List.of("en-US.yml", "pl-PL.yml");

  private final Path localesDirectory;
  private final YamlConfigLoader yamlLoader;

  public FileSystemLocaleLoader(Path dataDirectory) {
    Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
    this.localesDirectory = dataDirectory.resolve("locales");
    this.yamlLoader = new YamlConfigLoader();
  }

  public Path localesDirectory() {
    return localesDirectory;
  }

  /**
   * Copies missing default locale files to the plugin data folder without overwriting existing
   * files.
   *
   * @throws IOException on I/O error
   */
  public void ensureBundledLocales() throws IOException {
    Files.createDirectories(localesDirectory);
    for (String fileName : BUNDLED_LOCALES) {
      Path target = localesDirectory.resolve(fileName);
      if (Files.exists(target)) {
        continue;
      }
      try (InputStream resource = getClass().getResourceAsStream("/locales/" + fileName)) {
        if (resource != null) {
          Files.copy(resource, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  /**
   * Loads the selected locale catalog, overlaying operator translations onto the bundled English
   * fallback.
   *
   * @param requestedLocale desired locale code (e.g. "en-US", "pl-PL")
   * @return immutable locale catalog
   * @throws IOException on failure
   */
  public LocaleCatalog loadCatalog(String requestedLocale) throws IOException {
    ensureBundledLocales();

    String selected =
        BUNDLED_LOCALES.contains(requestedLocale + ".yml")
            ? requestedLocale
            : LocaleCatalog.englishLocale();

    Map<String, String> english = readMergedLocale(LocaleCatalog.englishLocale());
    Map<String, String> selectedMessages =
        selected.equals(LocaleCatalog.englishLocale()) ? english : readMergedLocale(selected);

    return new LocaleCatalog(selected, english, selectedMessages);
  }

  private Map<String, String> readMergedLocale(String locale) throws IOException {
    Map<String, String> result = new LinkedHashMap<>();

    // 1. Read bundled resource if available
    try (InputStream bundled = getClass().getResourceAsStream("/locales/" + locale + ".yml")) {
      if (bundled != null) {
        String content = new String(bundled.readAllBytes(), StandardCharsets.UTF_8);
        flatten(yamlLoader.parseMap(content), "", result);
      }
    }

    // 2. Read operator file on disk as overlay
    Path operatorFile = localesDirectory.resolve(locale + ".yml");
    if (Files.isRegularFile(operatorFile)) {
      String content = Files.readString(operatorFile, StandardCharsets.UTF_8);
      flatten(yamlLoader.parseMap(content), "", result);
    }

    result.remove("schema-version");
    return Map.copyOf(result);
  }

  private void flatten(Map<String, Object> source, String prefix, Map<String, String> destination) {
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      if (entry.getValue() instanceof Map<?, ?> nested) {
        Map<String, Object> casted = new LinkedHashMap<>();
        for (Map.Entry<?, ?> nEntry : nested.entrySet()) {
          casted.put(String.valueOf(nEntry.getKey()), nEntry.getValue());
        }
        flatten(casted, key, destination);
      } else if (entry.getValue() != null) {
        destination.put(key, String.valueOf(entry.getValue()));
      }
    }
  }
}
