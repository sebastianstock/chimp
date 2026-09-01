#!/usr/bin/env bash
# Benchmark harness: compare CHIMP-native kernel (current branch) against the
# legacy Meta-CSP backend (legacy-metacsp branch).
#
# Usage:
#   ./benchmark.sh [domain] [problem] [runs]
#
# Example:
#   ./benchmark.sh domains/ordered_domain.ddl problems/test_m_drive_robot_1.pdl 3

set -euo pipefail

DOMAIN="${1:-domains/ordered_domain.ddl}"
PROBLEM="${2:-problems/test_m_drive_robot_1.pdl}"
RUNS="${3:-3}"

LEGACY_BRANCH="legacy-metacsp"
LEGACY_DIR="$(pwd)/build/legacy-worktree"

if ! git rev-parse --verify "$LEGACY_BRANCH" >/dev/null 2>&1; then
    echo "Creating legacy branch $LEGACY_BRANCH ..."
    git branch "$LEGACY_BRANCH" HEAD
fi

# Ensure a clean legacy worktree
if [ -d "$LEGACY_DIR" ]; then
    git worktree remove -f "$LEGACY_DIR" || true
    rm -rf "$LEGACY_DIR"
fi
git worktree add "$LEGACY_DIR" "$LEGACY_BRANCH"

# Cleanup worktree on exit
cleanup() {
    git worktree remove -f "$LEGACY_DIR" || true
}
trap cleanup EXIT

run_benchmark() {
    local label="$1"
    local dir="$2"
    echo "--- $label ---"
    local total=0
    for i in $(seq 1 "$RUNS"); do
        local start end elapsed
        start=$(date +%s.%N)
        (cd "$dir" && ./gradlew --quiet run --args="$DOMAIN $PROBLEM" --no-daemon >/dev/null 2>&1) || true
        end=$(date +%s.%N)
        elapsed=$(awk -v s="$start" -v e="$end" 'BEGIN {print e - s}')
        total=$(awk -v t="$total" -v e="$elapsed" 'BEGIN {print t + e}')
        printf "  run %d: %.3fs\n" "$i" "$elapsed"
    done
    awk -v t="$total" -v n="$RUNS" 'BEGIN {printf "  average: %.3fs\n", t / n}'
}

echo "Benchmarking $DOMAIN / $PROBLEM ($RUNS runs each)"
run_benchmark "CHIMP-native (current)" "$(pwd)"
run_benchmark "Meta-CSP legacy ($LEGACY_BRANCH)" "$LEGACY_DIR"
