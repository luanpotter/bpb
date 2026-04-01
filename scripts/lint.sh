#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

fix=0
if [[ "${1:-}" == "--fix" ]]; then
    fix=1
fi

failed=0

run() {
    echo "==> $1"
    if ! eval "$2" > /dev/null 2>&1; then
        echo "    FAILED"
        failed=1
    fi
}

eclint_files="'src/**' 'scripts/**' 'configs/**' '*.md' '*.yml' '*.kts' '.editorconfig' '.gitignore'"

run "Compile check"             "./gradlew classes testClasses -q"
run "Tests"                     "./gradlew test -q"
run "Detekt (static analysis)"  "./gradlew detekt -q"

if [[ $fix -eq 1 ]]; then
    run "Ktfmt (fix)"           "./scripts/ktfmt.sh --fix"
    run "Prettier (fix)"        "npx --yes prettier --config configs/.prettierrc.yml --write '**/*.md' '**/*.yml'"
    run "Markdownlint (fix)"    "npx --yes markdownlint-cli --config configs/.markdownlint.json --fix '**/*.md'"
    run "EditorConfig (fix)"    "npx --yes eclint fix $eclint_files"
else
    run "Ktfmt (formatting)"    "./scripts/ktfmt.sh --check"
    run "Prettier (md, yaml)"   "npx --yes prettier --config configs/.prettierrc.yml --check '**/*.md' '**/*.yml'"
    run "Markdownlint"          "npx --yes markdownlint-cli --config configs/.markdownlint.json '**/*.md'"
    run "EditorConfig"          "npx --yes eclint check $eclint_files"
fi

if [[ $failed -ne 0 ]]; then
    echo -e "\nSome checks failed."
    exit 1
else
    echo -e "\nAll checks passed."
fi
