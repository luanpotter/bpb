#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> Building..."
./gradlew installDist
echo "==> Done. Binary at build/install/bpb/bin/bpb"
