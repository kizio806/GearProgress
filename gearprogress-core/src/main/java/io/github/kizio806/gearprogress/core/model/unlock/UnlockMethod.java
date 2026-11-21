package io.github.kizio806.gearprogress.core.model.unlock;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/** Supported mechanism types for unlocking equipment tiers. */
public enum UnlockMethod {
  ALWAYS("always"),
  NEVER("never"),
  TIMER("timer"),
  DATE("date"),
  COMMAND("command");

  private final String id;

  UnlockMethod(String id) {
    this.id = id;
  }

  /**
   * Returns the canonical lowercase identifier for this unlock method.
   *
   * @return the method identifier
   */
  public String id() {
    return id;
  }

  /**
   * Attempts to parse an unlock method string.
   *
   * @param raw the raw input string
   * @return an {@link Optional} containing the matched method, or empty if unknown
   */
  public static Optional<UnlockMethod> fromString(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    for (UnlockMethod method : values()) {
      if (method.id.equals(normalized)
          || method.name().toLowerCase(Locale.ROOT).equals(normalized)) {
        return Optional.of(method);
      }
    }
    return Optional.empty();
  }

  /**
   * Resolves an unlock method from raw string or throws {@link IllegalArgumentException}.
   *
   * @param raw the raw input string
   * @return the matched unlock method
   */
  public static UnlockMethod fromStringOrThrow(String raw) {
    Objects.requireNonNull(raw, "method string must not be null");
    return fromString(raw)
        .orElseThrow(() -> new IllegalArgumentException("Unknown unlock method: " + raw));
  }
}
