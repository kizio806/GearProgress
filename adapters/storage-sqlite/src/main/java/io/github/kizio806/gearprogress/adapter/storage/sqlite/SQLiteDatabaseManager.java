package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Manages SQLite database connectivity, WAL pragmas, and schema migrations. */
public final class SQLiteDatabaseManager {

  private final Path databasePath;
  private final String jdbcUrl;

  public SQLiteDatabaseManager(Path databasePath) {
    this.databasePath = Objects.requireNonNull(databasePath, "databasePath must not be null");
    this.jdbcUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
  }

  /**
   * Initializes the database directory, applies pragmas, and runs schema creation.
   *
   * @throws IOException on directory creation failure
   * @throws SQLException on database error
   */
  public void initialize() throws IOException, SQLException {
    Path parent = databasePath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    try (Connection connection = createConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode = WAL;");
      statement.execute("PRAGMA synchronous = NORMAL;");
      statement.execute("PRAGMA busy_timeout = 5000;");
      statement.execute("PRAGMA foreign_keys = ON;");

      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS gear_progression_state (
              category TEXT NOT NULL,
              tier TEXT NOT NULL,
              status TEXT NOT NULL,
              allow_enchantments INTEGER NOT NULL,
              unlocked_at TEXT,
              unlock_method TEXT NOT NULL,
              unlocked_by TEXT,
              updated_at TEXT NOT NULL,
              PRIMARY KEY (category, tier)
          );
          """);

      statement.execute(
          """
          CREATE TABLE IF NOT EXISTS gear_progression_audit (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              category TEXT NOT NULL,
              tier TEXT NOT NULL,
              transition_type TEXT NOT NULL,
              operator TEXT,
              reason TEXT,
              timestamp TEXT NOT NULL
          );
          """);
    }
  }

  /**
   * Creates a new connection to the SQLite database.
   *
   * @return connection instance
   * @throws SQLException on connection failure
   */
  public Connection createConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }

  public Path databasePath() {
    return databasePath;
  }
}
