#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

KTFMT_VERSION="0.62"
KTFMT_JAR=".cache/ktfmt-${KTFMT_VERSION}.jar"
KTFMT_URL="https://repo1.maven.org/maven2/com/facebook/ktfmt/${KTFMT_VERSION}/ktfmt-${KTFMT_VERSION}-with-dependencies.jar"

ensure_jar() {
  if [[ ! -f "$KTFMT_JAR" ]]; then
    mkdir -p .cache
    curl -fSL -o "$KTFMT_JAR" "$KTFMT_URL"
  fi
}

mode="format"
files=()
for arg in "$@"; do
  case "$arg" in
    --check) mode="check" ;;
    --fix) mode="format" ;;
    *) files+=("$arg") ;;
  esac
done

if [[ ${#files[@]} -eq 0 ]]; then
  while IFS= read -r -d '' f; do
    files+=("$f")
  done < <(find src -name '*.kt' -print0)
fi

ensure_jar

if [[ "$mode" == "check" ]]; then
  java -jar "$KTFMT_JAR" --dry-run --set-exit-if-changed "${files[@]}"
else
  java -jar "$KTFMT_JAR" "${files[@]}"
fi
