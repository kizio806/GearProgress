#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/probe-minecraft-versions.sh \
    --versions "<csv>" \
    [--tasks "<gradle tasks>"] \
    [--keep-workdir]

Example:
  ./scripts/probe-minecraft-versions.sh --versions "26.1,26.1.1,26.1.2,26.2,26.3"
  ./scripts/probe-minecraft-versions.sh --versions "26.3" --tasks "verifyCompatibilityMatrix check"
USAGE
}

require_value() {
  local flag="$1"
  local value="${2:-}"
  if [ -z "$value" ]; then
    echo "Missing value for $flag"
    usage
    exit 1
  fi
}

trim() {
  local value="$1"
  echo "$(echo "$value" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
}

validate_mc_version() {
  local version="$1"
  if [[ ! "$version" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
    echo "Invalid Minecraft version '$version'. Expected MAJOR.MINOR or MAJOR.MINOR.PATCH"
    exit 1
  fi
}

release_range_for_version() {
  local version="$1"
  local major="${version%%.*}"
  local remainder="${version#*.}"
  local minor="${remainder%%.*}"
  if [ "$major" = "1" ]; then
    echo "${major}.${minor}.x"
  else
    echo "${major}.x"
  fi
}

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SET_VERSION_SCRIPT="$PROJECT_ROOT/scripts/set-minecraft-version.sh"

if [ ! -x "$SET_VERSION_SCRIPT" ]; then
  echo "Cannot execute $SET_VERSION_SCRIPT"
  exit 1
fi

VERSIONS_CSV=""
GRADLE_TASKS="verifyCompatibilityMatrix check"
KEEP_WORKDIR=0

while [ "$#" -gt 0 ]; do
  case "$1" in
    --versions)
      require_value "$1" "${2:-}"
      VERSIONS_CSV="$2"
      shift 2
      ;;
    --tasks)
      require_value "$1" "${2:-}"
      GRADLE_TASKS="$2"
      shift 2
      ;;
    --keep-workdir)
      KEEP_WORKDIR=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1"
      usage
      exit 1
      ;;
  esac
done

if [ -z "$VERSIONS_CSV" ]; then
  echo "--versions is required"
  usage
  exit 1
fi

IFS=',' read -ra RAW_VERSIONS <<< "$VERSIONS_CSV"
VERSIONS=()
for version in "${RAW_VERSIONS[@]}"; do
  version="$(trim "$version")"
  [ -z "$version" ] && continue
  validate_mc_version "$version"
  VERSIONS+=("$version")
done

if [ "${#VERSIONS[@]}" -eq 0 ]; then
  echo "No valid versions provided in --versions"
  exit 1
fi

BASE_TMPDIR="${TMPDIR:-/tmp}"
WORKDIR="$(mktemp -d "$BASE_TMPDIR/gearprogress-probe.XXXXXX")"

cleanup() {
  if [ "$KEEP_WORKDIR" -eq 1 ]; then
    echo "Workdir retained at $WORKDIR"
  else
    rm -rf "$WORKDIR"
  fi
}
trap cleanup EXIT

echo "=== Probing GearProgress Candidate Versions ==="
echo "Candidate versions: ${VERSIONS[*]}"
echo "Tasks: $GRADLE_TASKS"
echo "Working directory: $WORKDIR"

SUCCESSFUL_VERSIONS=()
FAILED_VERSIONS=()

for version in "${VERSIONS[@]}"; do
  echo "--- Probing candidate: $version ---"
  CANDIDATE_DIR="$WORKDIR/$version"
  mkdir -p "$CANDIDATE_DIR"
  
  # Copy repository state ignoring heavy build artifacts
  git -C "$PROJECT_ROOT" archive HEAD | tar -x -C "$CANDIDATE_DIR"

  (
    cd "$CANDIDATE_DIR"
    RELEASE_RANGE="$(release_range_for_version "$version")"
    PROPS_FILE="$CANDIDATE_DIR/gradle.properties" "$SET_VERSION_SCRIPT" \
      --base-version "1.1.0" \
      --release-range "$RELEASE_RANGE" \
      --mod-version "1.1.0+mc${RELEASE_RANGE}" \
      --supported-versions "$version"
      
    # Execute gradle verification tasks
    ./gradlew $GRADLE_TASKS --no-daemon
  ) && {
    echo "PASS: $version"
    SUCCESSFUL_VERSIONS+=("$version")
  } || {
    echo "FAIL: $version"
    FAILED_VERSIONS+=("$version")
  }
done

echo "=== Probe Summary ==="
echo "Successful: ${#SUCCESSFUL_VERSIONS[@]} (${SUCCESSFUL_VERSIONS[*]:-none})"
echo "Failed:     ${#FAILED_VERSIONS[@]} (${FAILED_VERSIONS[*]:-none})"

if [ "${#FAILED_VERSIONS[@]}" -gt 0 ]; then
  exit 1
fi
