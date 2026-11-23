## Description
<!-- Provide a concise summary of the changes and motivation for this pull request. -->

## Type of Change
- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Breaking change (fix or feature changing existing behavior)
- [ ] Documentation update
- [ ] Refactoring / tooling enhancement

## Architectural Compliance
- [ ] **Hexagonal Architecture:** Domain core remains free of Bukkit/Paper runtime references.
- [ ] **Multi-Distribution Parity:** Changes tested or compatible across Paper, Folia, and Spigot distributions.
- [ ] **Strict Typing:** No raw warnings or suppressions introduced.
- [ ] **Supply Chain Security:** No compiled `.jar` or binary artifacts committed.
- [ ] **Actions Pinning:** Any new workflow actions are pinned to full commit SHAs.

## Verification Checklist
- [ ] `./gradlew check` passes locally
- [ ] `./gradlew spotlessCheck` passes
- [ ] All commits follow Conventional Commits standard
