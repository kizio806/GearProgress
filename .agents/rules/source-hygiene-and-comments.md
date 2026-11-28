# Source Hygiene, Comment Policy, and Suppressions

## Purpose
In the `GearProgress` codebase, source code must uphold uncompromising readability and clean architecture. Automated gates (`validateSourceHygiene` and `check`) fail immediately if marker tags, wildcard imports, or warning suppressions are introduced.

---

## 1. Commenting Philosophy ("Clean Code" Standard)
- **Self-Documenting Code:** Method names, variable names, and class responsibilities must be expressive enough that code narrates itself without needing step-by-step commentary.
- **Explain "WHY", Not "WHAT":** Inline comments (`//`) are reserved strictly for non-obvious context, architectural rationale, Folia concurrency boundaries, SQLite writer threading constraints, or platform workarounds.
- **No Redundant Comments:** Never write comments that merely rephrase or paraphrase the code underneath (e.g., avoid `// register iron armor` immediately before `register(ARMOR, "iron", ...)`).
- **English Exclusively:** All comments, JavaDoc, identifiers, and log messages must be written in English.

---

## 2. API Documentation (JavaDoc)
- **Target Key Touchpoints:** Provide complete, high-value JavaDoc on public interfaces, hexagonal SPI ports (`ProgressionRepository`, `TaskSchedulerPort`), domain records, and application service facades.
- **No Private Method Boilerplate:** Avoid adding formal JavaDoc tags (`@param`, `@return`) to trivial private helper methods; private code should remain concise and self-evident.

---

## 3. Strict Hygiene Invariants (Hard Gates)
- **Zero Markers:** Never commit `TODO`, `FIXME`, or `XXX` tags in `.java` files. Outstanding work or future enhancements must be tracked via GitHub Issues.
- **Zero Wildcard Imports:** Always import explicit, fully qualified class names. Wildcard imports (`import java.util.*;`) are strictly prohibited.
- **Zero `@SuppressWarnings`:** Warning-silencing annotations of any kind are strictly forbidden across all modules. Resolve deprecations and compiler diagnostics at the source.

---

## Examples

### Good:
```java
// Correct: Explains "WHY" — Folia multi-threading and SQLite single-writer constraint
// SQLite write transactions cannot run concurrently across multiple Folia region threads.
// All state mutations are serialized asynchronously through SingleWriterPersistenceExecutor.
writerExecutor.submit(() -> writeState(tierKey, state));

// Correct: Expressive self-documenting code with clean JavaDoc on public SPI port
/**
 * Persistence port for retrieving and updating equipment progression states.
 */
public interface ProgressionRepository {
  Optional<TierProgressState> findState(TierKey key);
}
```

### Bad:
```java
// Bad: Redundant comment restating the obvious
// Check if unlocked
if (state.isUnlocked()) {
  ...
}

// Bad: Marker tag will fail validateSourceHygiene
// TODO: Refactor this later

// Bad: Wildcard import will fail validateSourceHygiene
import java.util.*;

// Bad: Suppressing warnings is prohibited
@SuppressWarnings("unchecked")
```
