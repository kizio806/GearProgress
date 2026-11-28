# Workflow: Bug Fixing

## Steps
1. **Reproduction & Regression Test:**
   - Reproduce the reported issue with a failing unit or integration test before touching implementation code.
2. **Root Cause Analysis:**
   - Trace the issue to domain logic, configuration parsing, persistence serialization, or Bukkit event timing.
3. **Targeted Fix:**
   - Implement the minimal, robust fix respecting hexagonal module boundaries.
   - Never suppress warnings or bypass architectural layers.
4. **Validation:**
   - Ensure the regression test now passes.
   - Run `./gradlew check` to verify zero checkstyle, spotbugs, pmd, or coverage regressions.
5. **Changelog & Documentation:**
   - Record the bug fix in `CHANGELOG.md` under the appropriate milestone.
