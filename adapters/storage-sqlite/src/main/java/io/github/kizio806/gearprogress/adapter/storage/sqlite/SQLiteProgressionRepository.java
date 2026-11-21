package io.github.kizio806.gearprogress.adapter.storage.sqlite;

import io.github.kizio806.gearprogress.application.port.ProgressionRepository;
import io.github.kizio806.gearprogress.core.model.category.EquipmentCategory;
import io.github.kizio806.gearprogress.core.model.state.ProgressionSnapshot;
import io.github.kizio806.gearprogress.core.model.state.TierProgressState;
import io.github.kizio806.gearprogress.core.model.state.TierStatus;
import io.github.kizio806.gearprogress.core.model.tier.TierKey;
import io.github.kizio806.gearprogress.core.model.unlock.UnlockMethod;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous SQLite persistence implementation of {@link ProgressionRepository} utilizing
 * serialized single-writer persistence.
 */
public final class SQLiteProgressionRepository implements ProgressionRepository {

  private final SQLiteDatabaseManager databaseManager;
  private final SingleWriterPersistenceExecutor persistenceExecutor;

  public SQLiteProgressionRepository(Path databasePath) {
    this(new SQLiteDatabaseManager(databasePath), new SingleWriterPersistenceExecutor());
  }

  public SQLiteProgressionRepository(
      SQLiteDatabaseManager databaseManager, SingleWriterPersistenceExecutor persistenceExecutor) {
    this.databaseManager =
        Objects.requireNonNull(databaseManager, "databaseManager must not be null");
    this.persistenceExecutor =
        Objects.requireNonNull(persistenceExecutor, "persistenceExecutor must not be null");
  }

  /**
   * Initializes SQLite database schema and tables.
   *
   * @throws IOException on I/O failure
   * @throws SQLException on database error
   */
  public void initialize() throws IOException, SQLException {
    databaseManager.initialize();
  }

  @Override
  public CompletableFuture<ProgressionSnapshot> loadSnapshot() {
    return persistenceExecutor.submit(
        () -> {
          Map<TierKey, TierProgressState> states = new LinkedHashMap<>();
          String sql =
              """
          SELECT category, tier, status, allow_enchantments, unlocked_at, unlock_method, unlocked_by, updated_at
          FROM gear_progression_state
          ORDER BY category, tier;
          """;

          try (Connection connection = databaseManager.createConnection();
              Statement statement = connection.createStatement();
              ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
              String catRaw = rs.getString("category");
              String tier = rs.getString("tier");
              EquipmentCategory category = EquipmentCategory.fromStringOrThrow(catRaw);
              TierKey key = TierKey.of(category, tier);

              TierStatus status = TierStatus.valueOf(rs.getString("status"));
              boolean allowEnchants = rs.getInt("allow_enchantments") == 1;

              String unlockedAtStr = rs.getString("unlocked_at");
              Optional<Instant> unlockedAt =
                  (unlockedAtStr != null && !unlockedAtStr.isBlank())
                      ? Optional.of(Instant.parse(unlockedAtStr))
                      : Optional.empty();

              UnlockMethod method = UnlockMethod.fromStringOrThrow(rs.getString("unlock_method"));
              String unlockedByStr = rs.getString("unlocked_by");
              Optional<String> unlockedBy = Optional.ofNullable(unlockedByStr);

              Instant updatedAt = Instant.parse(rs.getString("updated_at"));

              TierProgressState state =
                  new TierProgressState(
                      key, status, allowEnchants, unlockedAt, method, unlockedBy, updatedAt);

              states.put(key, state);
            }
          }
          return ProgressionSnapshot.of(states);
        });
  }

  @Override
  public CompletableFuture<Void> saveTierState(TierProgressState state) {
    Objects.requireNonNull(state, "state must not be null");
    return persistenceExecutor.run(
        () -> {
          String sql =
              """
          INSERT INTO gear_progression_state
              (category, tier, status, allow_enchantments, unlocked_at, unlock_method, unlocked_by, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?)
          ON CONFLICT(category, tier) DO UPDATE SET
              status = excluded.status,
              allow_enchantments = excluded.allow_enchantments,
              unlocked_at = excluded.unlocked_at,
              unlock_method = excluded.unlock_method,
              unlocked_by = excluded.unlocked_by,
              updated_at = excluded.updated_at;
          """;

          try (Connection connection = databaseManager.createConnection();
              PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, state.key().category().id());
            ps.setString(2, state.key().tier());
            ps.setString(3, state.status().name());
            ps.setInt(4, state.allowEnchantments() ? 1 : 0);
            ps.setString(5, state.unlockedAt().map(Instant::toString).orElse(null));
            ps.setString(6, state.unlockMethod().id());
            ps.setString(7, state.unlockedBy().orElse(null));
            ps.setString(8, state.updatedAt().toString());

            ps.executeUpdate();
          }
        });
  }

  @Override
  public CompletableFuture<Void> saveAll(ProgressionSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot must not be null");
    return persistenceExecutor.run(
        () -> {
          String sql =
              """
          INSERT INTO gear_progression_state
              (category, tier, status, allow_enchantments, unlocked_at, unlock_method, unlocked_by, updated_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?)
          ON CONFLICT(category, tier) DO UPDATE SET
              status = excluded.status,
              allow_enchantments = excluded.allow_enchantments,
              unlocked_at = excluded.unlocked_at,
              unlock_method = excluded.unlock_method,
              unlocked_by = excluded.unlocked_by,
              updated_at = excluded.updated_at;
          """;

          try (Connection connection = databaseManager.createConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
              for (TierProgressState state : snapshot.states().values()) {
                ps.setString(1, state.key().category().id());
                ps.setString(2, state.key().tier());
                ps.setString(3, state.status().name());
                ps.setInt(4, state.allowEnchantments() ? 1 : 0);
                ps.setString(5, state.unlockedAt().map(Instant::toString).orElse(null));
                ps.setString(6, state.unlockMethod().id());
                ps.setString(7, state.unlockedBy().orElse(null));
                ps.setString(8, state.updatedAt().toString());
                ps.addBatch();
              }
              ps.executeBatch();
              connection.commit();
            } catch (SQLException ex) {
              connection.rollback();
              throw ex;
            } finally {
              connection.setAutoCommit(true);
            }
          }
        });
  }

  @Override
  public void close() {
    try {
      persistenceExecutor.shutdown(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
