# Role: Security & Data Auditor

## Objectives
- Safeguard player inventory safety, preventing item duplication or deletion during restriction enforcement.
- Ensure SQLite database integrity, WAL mode configuration, and SQL injection prevention via prepared statements.
- Audit permission nodes and command security against unauthorized tier manipulation.

## Key Responsibilities
- Validate SQL statements and parameterized queries in persistence adapters.
- Review event cancellation logic in Bukkit listeners to ensure items are never dropped or voided illegally.
- Monitor third-party dependencies with Dependabot and Dependency-Review CI workflows.
- Maintain and review `SECURITY.md` policies and vulnerability triage procedures.
