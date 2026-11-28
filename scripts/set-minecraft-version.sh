#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  ./scripts/set-minecraft-version.sh \
    --base-version <MAJOR.MINOR.PATCH> \
    --release-range <e.g. 1.21.x> \
    --mod-version <MAJOR.MINOR.PATCH+mc<RANGE>> \
    [--paper-versions <csv>] \
    [--spigot-versions <csv>] \
    [--supported-versions <csv>]

Example:
  ./scripts/set-minecraft-version.sh \
    --base-version 1.0.0 \
    --release-range 1.21.x \
    --mod-version 1.0.0+mc1.21.x \
    --supported-versions "1.21,1.21.1,1.21.2,1.21.3,1.21.4,1.21.5,1.21.6,1.21.7,1.21.8,1.21.9,1.21.10,1.21.11"
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

BASE_VERSION=""
RELEASE_RANGE=""
MOD_VERSION=""
PAPER_VERSIONS=""
SPIGOT_VERSIONS=""
SUPPORTED_VERSIONS=""

while [ "$#" -gt 0 ]; do
  case "$1" in
    --base-version)
      require_value "$1" "${2:-}"
      BASE_VERSION="$(trim "$2")"
      shift 2
      ;;
    --release-range)
      require_value "$1" "${2:-}"
      RELEASE_RANGE="$(trim "$2")"
      shift 2
      ;;
    --mod-version)
      require_value "$1" "${2:-}"
      MOD_VERSION="$(trim "$2")"
      shift 2
      ;;
    --paper-versions)
      require_value "$1" "${2:-}"
      PAPER_VERSIONS="$(trim "$2")"
      shift 2
      ;;
    --spigot-versions)
      require_value "$1" "${2:-}"
      SPIGOT_VERSIONS="$(trim "$2")"
      shift 2
      ;;
    --supported-versions)
      require_value "$1" "${2:-}"
      SUPPORTED_VERSIONS="$(trim "$2")"
      shift 2
      ;;
    --help|-h)
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

if [ -n "$SUPPORTED_VERSIONS" ]; then
  if [ -z "$PAPER_VERSIONS" ]; then
    PAPER_VERSIONS="$SUPPORTED_VERSIONS"
  fi
  if [ -z "$SPIGOT_VERSIONS" ]; then
    SPIGOT_VERSIONS="$SUPPORTED_VERSIONS"
  fi
fi

if [ -z "$BASE_VERSION" ] || [ -z "$RELEASE_RANGE" ] || [ -z "$MOD_VERSION" ]; then
  echo "Error: --base-version, --release-range, and --mod-version are required."
  usage
  exit 1
fi

SEMVER_REGEX='^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?\+mc[0-9A-Za-z.-]+$'
if [[ ! "$MOD_VERSION" =~ $SEMVER_REGEX ]]; then
  echo "Error: Version '$MOD_VERSION' does not match format MAJOR.MINOR.PATCH+mc<RANGE>"
  exit 1
fi

EXPECTED_SUFFIX="+mc${RELEASE_RANGE}"
if [[ "$MOD_VERSION" != *"$EXPECTED_SUFFIX" ]]; then
  echo "Error: mod-version '$MOD_VERSION' must end with '$EXPECTED_SUFFIX'"
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS_FILE="${PROPS_FILE:-"$ROOT_DIR/gradle.properties"}"

if [ ! -f "$PROPS_FILE" ]; then
  echo "Error: Properties file not found at $PROPS_FILE"
  exit 1
fi

set_property() {
  local key="$1"
  local val="$2"
  if grep -q "^${key}=" "$PROPS_FILE"; then
    sed -i "s|^${key}=.*|${key}=${val}|" "$PROPS_FILE"
  else
    echo "${key}=${val}" >> "$PROPS_FILE"
  fi
}

set_property "gearprogress.baseVersion" "$BASE_VERSION"
set_property "gearprogress.releaseRange" "$RELEASE_RANGE"
set_property "gearprogress.version" "$MOD_VERSION"

if [ -n "$PAPER_VERSIONS" ]; then
  set_property "paper.minecraftVersions" "$PAPER_VERSIONS"
fi

if [ -n "$SPIGOT_VERSIONS" ]; then
  set_property "spigot.minecraftVersions" "$SPIGOT_VERSIONS"
fi

echo "Updated $PROPS_FILE successfully:"
echo "  gearprogress.baseVersion = $BASE_VERSION"
echo "  gearprogress.releaseRange = $RELEASE_RANGE"
echo "  gearprogress.version = $MOD_VERSION"
[ -n "$PAPER_VERSIONS" ] && echo "  paper.minecraftVersions = $PAPER_VERSIONS"
[ -n "$SPIGOT_VERSIONS" ] && echo "  spigot.minecraftVersions = $SPIGOT_VERSIONS"
