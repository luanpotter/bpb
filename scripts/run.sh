#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -x build/install/bpb/bin/bpb ]]; then
    echo "==> Building first..."
    ./gradlew installDist -q
fi

JAVA_HOME=$(./gradlew -q javaToolchains 2>&1 | grep -A2 "JDK 21" | grep "Location:" | sed 's/.*Location:\s*//')

exec env JAVA_HOME="$JAVA_HOME" build/install/bpb/bin/bpb "$@"
