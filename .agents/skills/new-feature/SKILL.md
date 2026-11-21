---
name: new-feature
description: Guide implementation of new domain rules, restrictions, or platform features in GearProgress
---

# New Feature Skill

When implementing new features in GearProgress:
1. Implement pure domain changes in `gearprogress-core`.
2. Expose use cases or ports in `gearprogress-application`.
3. Provide database schema updates or persistence logic in `adapters:storage-sqlite`.
4. Implement Minecraft listeners or commands in `platforms/`.
5. Add unit and integration tests with $\ge 90\%$ coverage.
6. Verify via `./gradlew check`.
