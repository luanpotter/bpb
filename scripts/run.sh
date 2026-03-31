#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -x build/install/bpb/bin/bpb ]]; then
    echo "==> Building first..."
    gradle installDist -q
fi

exec build/install/bpb/bin/bpb "$@"
