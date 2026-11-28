# Rule: SQLite & Transaction Integrity

## Principles
1. **Single Writer Execution:** SQLite does not support concurrent write transactions across multiple threads. All writes must go through `SingleWriterPersistenceExecutor`.
2. **WAL Mode Enabled:** SQLite databases must be initialized with `PRAGMA journal_mode=WAL;` and `PRAGMA synchronous=NORMAL;` for high read concurrency and durability.
3. **Prepared Statements Exclusively:** Dynamic query strings and SQL concatenation are strictly prohibited. Always bind arguments via `PreparedStatement`.
4. **Idempotent Migrations:** Schema creation and upgrade scripts must use `IF NOT EXISTS` and check current schema version pragma.
5. **Safe Resource Management:** Always use `try-with-resources` for `Connection`, `PreparedStatement`, and `ResultSet` to eliminate connection leaks.
