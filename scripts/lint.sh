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

run "Compile check"       "./gradlew compileKotlin -q"
run "Tests"               "./gradlew test -q"
run "Detekt (static analysis)" "./gradlew detekt -q"
run "Ktfmt (formatting)" "./gradlew ktfmtCheck -q"

if [[ $failed -ne 0 ]]; then
    echo -e "\nSome checks failed."
    exit 1
else
    echo -e "\nAll checks passed."
fi
