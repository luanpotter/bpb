#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> Building..."
gradle installDist
echo "==> Done. Binary at build/install/bpb/bin/bpb"
