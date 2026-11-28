package io.github.kizio806.gearprogress.application.config.locale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable locale catalog with an English fallback for every plugin-owned message. */
public final class LocaleCatalog {

  private static final String ENGLISH = "en-US";

  private final String selectedLocale;
  private final Map<String, String> fallbackMessages;
  private final Map<String, String> selected;

  LocaleCatalog(
      String selectedLocale, Map<String, String> fallbackMessages, Map<String, String> selected) {
    this.selectedLocale = Objects.requireNonNull(selectedLocale, "selectedLocale must not be null");
    this.fallbackMessages = Map.copyOf(new LinkedHashMap<>(fallbackMessages));
    this.selected = Map.copyOf(new LinkedHashMap<>(selected));
  }

  /**
   * Creates a catalog using English messages for both fallback and selected.
   *
   * @param messages map of keys to text
   * @return locale catalog
   */
  public static LocaleCatalog of(Map<String, String> messages) {
    Objects.requireNonNull(messages, "messages must not be null");
    return new LocaleCatalog(ENGLISH, messages, messages);
  }

  /**
   * Returns the selected locale code (e.g., "en-US", "pl-PL").
   *
   * @return locale identifier
   */
  public String selectedLocale() {
    return selectedLocale;
  }

  /**
   * Retrieves the message for a key, falling back to English if missing in the selected locale.
   *
   * @param key translation key
   * @return localized string or placeholder marker if completely missing
   */
  public String message(String key) {
    Objects.requireNonNull(key, "key must not be null");
    return selected.getOrDefault(
        key, fallbackMessages.getOrDefault(key, "<red>[missing message: " + key + "]</red>"));
  }

  /**
   * Retrieves a message and replaces all "%placeholder%" occurrences.
   *
   * @param key translation key
   * @param placeholders map of placeholder names to values
   * @return formatted message
   */
  public String message(String key, Map<String, ?> placeholders) {
    String resolved = message(key);
    if (placeholders == null || placeholders.isEmpty()) {
      return resolved;
    }
    for (Map.Entry<String, ?> entry : placeholders.entrySet()) {
      if (entry.getKey() != null && entry.getValue() != null) {
        resolved = resolved.replace("%" + entry.getKey() + "%", String.valueOf(entry.getValue()));
      }
    }
    return resolved;
  }

  /**
   * Default English locale identifier.
   *
   * @return "en-US"
   */
  public static String englishLocale() {
    return ENGLISH;
  }

  /**
   * Returns all available keys in the selected locale.
   *
   * @return set of keys
   */
  public Set<String> keys() {
    return Collections.unmodifiableSet(selected.keySet());
  }
}
