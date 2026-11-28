## Summary

Provide a concise description of the change, the reasoning behind it, and the problem it resolves.

## Type of Change

- [ ] 🐛 Bug fix (non-breaking fix for an issue)
- [ ] ✨ Feature / improvement (non-breaking addition or refinement)
- [ ] ♻️ Refactoring (cleanups or architectural improvements with no behavioral changes)
- [ ] ⚡ Performance optimization
- [ ] 🏗️ Build / CI / tooling update
- [ ] 📝 Documentation update

## Related Issues

Closes #

## What Changed

- Detailed bulleted list of modifications made across modules.

## Validation & Quality Gates

- [ ] `./gradlew check` passes locally without errors or warnings
- [ ] Spotless formatting applied (`./gradlew spotlessApply`)
- [ ] JaCoCo test coverage standards met ($\ge 90\%$)
- [ ] Tested on Paper / Folia distribution artifact
- [ ] Tested on Spigot distribution artifact

## Checklist

- [ ] Code follows Hexagonal Architecture principles (no Minecraft imports in `core` or `application`)
- [ ] No `TODO`, `FIXME`, or wildcard imports added to Java source code
- [ ] No `@SuppressWarnings` annotations introduced
- [ ] Concurrency and SQLite single-writer safety preserved
- [ ] Documentation updated if configuration schemas or public APIs changed
