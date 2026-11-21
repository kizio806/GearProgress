---
name: storage-migration
description: Manage SQLite schema versioning, WAL mode pragma, and data persistence migrations
---

# Storage Migration Skill

When modifying persistence schema:
1. Increment the target schema version constant.
2. Implement idempotent migration steps using `IF NOT EXISTS` or conditional column addition.
3. Test migration with existing database fixtures.
4. Verify all write statements pass through `SingleWriterPersistenceExecutor`.
