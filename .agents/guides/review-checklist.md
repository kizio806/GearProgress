# Code Review & Pull Request Checklist

Comprehensive checklist for reviewing contributions to **GearProgress**.

---

## 🔍 Structural & Architectural Review

- [ ] **Hexagonal Purity:** Are all domain entities and business rules in `gearprogress-core` and `gearprogress-application`?
- [ ] **Zero Minecraft Imports:** Does `core` or `application` import Bukkit or Paper? (Must be 0).
- [ ] **Constructor Injection:** Are dependencies passed via constructors rather than static singletons?
- [ ] **No NMS / Reflection Hacks:** Is standard Bukkit / Paper / Folia API used without internal reflection?

---

## ⚡ Concurrency & Folia Review

- [ ] **Thread-Safe Reads:** Does `RestrictionQueryService` avoid locks on hot tick threads?
- [ ] **Single-Writer DB Writes:** Are all database mutations queued asynchronously to `SingleWriterPersistenceExecutor`?
- [ ] **Folia Compatibility:** Are scheduler tasks bound to regions or scheduled asynchronously without assuming a single main thread?

---

## 🧪 Testing & Coverage Review

- [ ] **Real Behavioral Assertions:** Do tests assert actual behavior, boundary values, and error paths rather than merely executing code for coverage?
- [ ] **Instruction Coverage:** Does coverage meet $\ge 90\%$ aggregated and $\ge 95\%$ on `gearprogress-core`?
- [ ] **MockBukkit Cleanliness:** Are mock servers and plugin instances cleaned up after tests?

---

## 🧹 Source Hygiene Review

- [ ] **Zero Suppressions:** Zero `@SuppressWarnings` annotations.
- [ ] **Zero Markers:** Zero `TODO`, `FIXME`, or `XXX` tags in Java files.
- [ ] **Zero Wildcards:** No `import ...*`. Explicit single-class imports only.
- [ ] **Google Java Format:** Code formatted with `./gradlew spotlessApply`.
- [ ] **Conventional Commits:** Commits follow `<type>(<scope>): <description>`.
