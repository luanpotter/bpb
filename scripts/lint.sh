#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

failed=0

run() {
    echo "==> $1"
    if ! eval "$2"; then
        echo "    FAILED"
        failed=1
    fi
}

run "Compile check"       "./gradlew classes testClasses -q"
run "Tests"               "./gradlew test -q"
run "Detekt (static analysis)" "./gradlew detekt -q"
run "Ktfmt (formatting)" "./gradlew ktfmtCheck -q"
run "Prettier (md, yaml)" "npx --yes prettier --config configs/.prettierrc.yml --check '**/*.md' '**/*.yml' 2>&1"
run "Markdownlint"        "npx --yes markdownlint-cli --config configs/.markdownlint.json '**/*.md' 2>&1"
run "EditorConfig"        "npx --yes eclint check 2>&1"

if [[ $failed -ne 0 ]]; then
    echo -e "\nSome checks failed."
    exit 1
else
    echo -e "\nAll checks passed."
fi
