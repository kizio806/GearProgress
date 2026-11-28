#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

count=0
failures=()
link_regex="\[[^]]*\]\(([^)]+)\)"

while IFS= read -r doc; do
    count=$((count + 1))
    doc_dir="$(dirname "$doc")"
    line_no=0
    while IFS= read -r line || [[ -n "$line" ]]; do
        line_no=$((line_no + 1))
        temp_line="$line"
        while [[ "$temp_line" =~ $link_regex ]]; do
            matched="${BASH_REMATCH[0]}"
            raw_target="${BASH_REMATCH[1]}"
            temp_line="${temp_line#*"$matched"}"

            # Clean target link
            target="${raw_target%% *}"
            target="${target#<}"
            target="${target%>}"

            # Skip web schemes, mailto, tel, anchor-only links
            if [[ "$target" =~ ^(https?:|mailto:|tel:|#) ]]; then
                continue
            fi

            # Strip anchor and query parameters
            clean_path="${target%%#*}"
            clean_path="${clean_path%%\?*}"

            if [[ -z "$clean_path" ]]; then
                continue
            fi

            # Resolve absolute vs relative paths
            if [[ "$clean_path" == /* ]]; then
                resolved="$ROOT_DIR$clean_path"
            else
                resolved="$doc_dir/$clean_path"
            fi

            if [[ ! -e "$resolved" ]]; then
                failures+=("${doc#./}:$line_no: missing $target")
            fi
        done
    done < "$doc"
done < <(find . -type f -name "*.md" ! -path "*/build/*" ! -path "*/.git/*" ! -path "*/.gradle/*" ! -path "*/server/*" | sort)

if (( ${#failures[@]} > 0 )); then
    echo "Markdown link verification failed:" >&2
    for failure in "${failures[@]}"; do
        echo "- $failure" >&2
    done
    exit 1
fi

echo "Markdown link verification passed ($count files)."
