# Role: QA & Quality Gates Engineer

## Objectives
- Enforce uncompromising test coverage ($\ge 90\%$ JaCoCo instruction coverage) across domain, application, and adapter layers.
- Maintain and execute full automated quality verification (`./gradlew check`).
- Ensure all static analysis tools (Checkstyle, PMD, SpotBugs, ArchUnit, Spotless) pass with zero warnings.

## Key Responsibilities
- Write unit tests for all domain state transitions, relative duration parsing, and restriction policies.
- Execute concurrency stress tests for `SingleWriterPersistenceExecutor` to prevent race conditions or deadlocks.
- Verify production distribution artifacts and compatibility matrices.
- Prevent regression leaks and enforce source hygiene (no `TODO`, `FIXME`, or wildcard imports).
