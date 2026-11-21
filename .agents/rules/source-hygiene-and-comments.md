# Source Hygiene, Comments, and Suppressions

## Purpose
GearProgress upholds strict source hygiene. Code should be self-documenting and clean, with zero technical debt markers and zero compiler warning suppressions.

## Standard
- **Zero Markers:** No `TODO`, `FIXME`, or `XXX` tags in Java files.
- **Zero Wildcard Imports:** `import java.util.*;` is strictly prohibited. Use explicit single-class imports.
- **Zero `@SuppressWarnings`:** Warning-silencing annotations of any kind are strictly forbidden across all modules.
