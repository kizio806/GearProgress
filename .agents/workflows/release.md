# Workflow: Release Preparation

## Steps
1. **Clean Workspace Verification:**
   - Ensure working tree is clean with no uncommitted files or scratch artifacts.
2. **Version Bump:**
   - Update `version` property in `gradle.properties`.
   - Update `CHANGELOG.md` with release date and version header.
3. **Full Build & Verification:**
   - Run `./gradlew clean check build`.
   - Verify distribution artifacts in `distributions/paper/build/libs/` and `distributions/spigot/build/libs/`.
4. **Git Tagging:**
   - Create annotated tag: `git tag -a v<version> -m "Release v<version>"`.
5. **Release Distribution:**
   - Push commit and tag to origin.
   - Publish GitHub Release with distribution JARs and changelog excerpt.
