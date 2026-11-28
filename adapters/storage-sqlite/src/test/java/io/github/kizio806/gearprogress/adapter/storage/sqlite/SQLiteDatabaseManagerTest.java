package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteDatabaseManagerTest {

  @Test
  void testInitializeDatabase(@TempDir Path tempDir) throws IOException, SQLException {
    Path dbFile = tempDir.resolve("sub").resolve("test.db");
    SQLiteDatabaseManager manager = new SQLiteDatabaseManager(dbFile);

    manager.initialize();
    assertTrue(Files.exists(dbFile));

    try (Connection conn = manager.createConnection()) {
      assertNotNull(conn);
    }
  }
}
