---
name: bug-fix
description: Systematic reproduction, fix, and regression protection in GearProgress
---

# Bug Fix Skill

Follow this strict protocol for resolving defects:
1. Write an automated unit or integration test reproducing the bug.
2. Verify the test fails as expected.
3. Apply the targeted fix in the appropriate architectural layer.
4. Verify the test passes and `./gradlew check` is clean.
5. Record the fix in `CHANGELOG.md`.
