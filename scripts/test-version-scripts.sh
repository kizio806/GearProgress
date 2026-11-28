#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SET_SCRIPT="$ROOT_DIR/scripts/set-minecraft-version.sh"

TMP_PROPS="$(mktemp)"
trap 'rm -f "$TMP_PROPS"' EXIT

reset_temp_props() {
  cat <<'PROPS' > "$TMP_PROPS"
gearprogress.group=io.github.kizio806
gearprogress.artifactBaseName=gearprogress
gearprogress.baseVersion=1.0.0
gearprogress.releaseRange=1.21.x
gearprogress.version=1.0.0+mc1.21.x
paper.minecraftVersions=1.21,1.21.1
spigot.minecraftVersions=1.21,1.21.1
PROPS
}

assert_contains() {
  local file="$1"
  local pattern="$2"
  if ! grep -q "^${pattern}$" "$file"; then
    echo "Assertion failed: '$pattern' not found in $file"
    cat "$file"
    exit 1
  fi
}

echo "=== Testing GearProgress Version Scripts ==="

# Test 1: Valid property update
reset_temp_props
PROPS_FILE="$TMP_PROPS" "$SET_SCRIPT" \
  --base-version 1.1.0 \
  --release-range 1.21.x \
  --mod-version 1.1.0+mc1.21.x \
  --supported-versions "1.21,1.21.1,1.21.2" >/dev/null

assert_contains "$TMP_PROPS" "gearprogress.baseVersion=1.1.0"
assert_contains "$TMP_PROPS" "gearprogress.releaseRange=1.21.x"
assert_contains "$TMP_PROPS" "gearprogress.version=1.1.0+mc1.21.x"
assert_contains "$TMP_PROPS" "paper.minecraftVersions=1.21,1.21.1,1.21.2"
assert_contains "$TMP_PROPS" "spigot.minecraftVersions=1.21,1.21.1,1.21.2"
echo "PASS: Valid version update and shared supported versions"

# Test 2: Independent paper and spigot version lists
reset_temp_props
PROPS_FILE="$TMP_PROPS" "$SET_SCRIPT" \
  --base-version 1.2.0 \
  --release-range 1.21.x \
  --mod-version 1.2.0+mc1.21.x \
  --paper-versions "1.21.2,1.21.3,1.21.4" \
  --spigot-versions "1.21.2,1.21.3" >/dev/null

assert_contains "$TMP_PROPS" "gearprogress.baseVersion=1.2.0"
assert_contains "$TMP_PROPS" "paper.minecraftVersions=1.21.2,1.21.3,1.21.4"
assert_contains "$TMP_PROPS" "spigot.minecraftVersions=1.21.2,1.21.3"
echo "PASS: Distinct platform versions supported"

# Test 3: Reject invalid SemVer
reset_temp_props
if PROPS_FILE="$TMP_PROPS" "$SET_SCRIPT" \
  --base-version 1.0.0 \
  --release-range 1.21.x \
  --mod-version "invalid-version-string" \
  --supported-versions "1.21" >/dev/null 2>&1; then
  echo "FAIL: Expected script to reject invalid SemVer"
  exit 1
fi
echo "PASS: Rejection of invalid SemVer format"

# Test 4: Reject mismatching release range suffix
reset_temp_props
if PROPS_FILE="$TMP_PROPS" "$SET_SCRIPT" \
  --base-version 1.0.0 \
  --release-range 1.22.x \
  --mod-version "1.0.0+mc1.21.x" \
  --supported-versions "1.22" >/dev/null 2>&1; then
  echo "FAIL: Expected script to reject mismatched suffix"
  exit 1
fi
echo "PASS: Rejection of mismatched release range suffix"

echo "=== All Version Script Tests Passed Successfully ==="
