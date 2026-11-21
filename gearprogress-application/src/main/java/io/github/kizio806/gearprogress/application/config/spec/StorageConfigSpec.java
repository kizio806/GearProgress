package io.github.kizio806.gearprogress.application.config.spec;

import java.util.Objects;

/**
 * Storage connection configuration mapped from storage.yml.
 *
 * @param type storage backend type (e.g. "sqlite")
 * @param file path to database file relative to data directory
 * @param maxPoolSize connection pool size
 * @param connectionTimeoutMs connection acquisition timeout in milliseconds
 */
public record StorageConfigSpec(
    String type, String file, int maxPoolSize, long connectionTimeoutMs) {

  /** Constructs a validated storage spec. */
  public StorageConfigSpec {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(file, "file must not be null");
    if (maxPoolSize <= 0) {
      maxPoolSize = 1;
    }
    if (connectionTimeoutMs <= 0) {
      connectionTimeoutMs = 5000;
    }
  }

  /**
   * Returns default SQLite storage settings.
   *
   * @return default storage spec
   */
  public static StorageConfigSpec defaultSqlite() {
    return new StorageConfigSpec("sqlite", "data/gearprogress.db", 1, 5000);
  }
}
