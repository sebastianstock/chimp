#!/usr/bin/env bash
# Full-CHIMP benchmark: compare the CHIMP-native kernel (current branch) against
# the legacy Meta-CSP backend (legacy-metacsp branch) across every textual
# planning domain/problem pair.
#
# Runs benchmark.FullChimpBenchmark in-JVM (no Gradle startup in the timing),
# 1 warm-up + 3 measured runs per problem, on both branches, then diffs the
# two CSVs and prints a comparison report.
#
# Usage:
#   ./scripts/compare-benchmark.sh [runs] [csv1] [csv2]
#
# Defaults: runs=3, csv1=build/benchmark/native.csv, csv2=build/benchmark/legacy.csv

set -euo pipefail

RUNS="${1:-3}"
NATIVE_CSV="${2:-build/benchmark/native.csv}"
LEGACY_CSV="${3:-build/benchmark/legacy.csv}"
LEGACY_BRANCH="legacy-metacsp"
LEGACY_DIR="$(pwd)/build/legacy-worktree"
HARNESS_SRC="src/main/java/benchmark/FullChimpBenchmark.java"

echo "=== Full-CHIMP benchmark (runs=$RUNS) ==="
echo

# --------------------------------------------------------------------
# 1. Native branch (current working tree)
# --------------------------------------------------------------------
echo "--- Native (current branch) ---"
./gradlew --quiet benchmark --no-daemon \
    -PbenchmarkArgs="$NATIVE_CSV $RUNS" 2>/dev/null || true
echo "  wrote $NATIVE_CSV"
echo

# --------------------------------------------------------------------
# 2. Legacy branch (worktree)
# --------------------------------------------------------------------
if ! git rev-parse --verify "$LEGACY_BRANCH" >/dev/null 2>&1; then
    echo "Legacy branch '$LEGACY_BRANCH' not found. Aborting." >&2
    exit 1
fi

if [ -d "$LEGACY_DIR" ]; then
    git worktree remove -f "$LEGACY_DIR" || true
    rm -rf "$LEGACY_DIR"
fi
echo "--- Legacy ($LEGACY_BRANCH worktree) ---"
git worktree add --quiet "$LEGACY_DIR" "$LEGACY_BRANCH"
cleanup() { git worktree remove -f "$LEGACY_DIR" || true; }
trap cleanup EXIT

# Copy the portable harness into the legacy worktree (not committed there).
mkdir -p "$LEGACY_DIR/src/main/java/benchmark"
cp "$HARNESS_SRC" "$LEGACY_DIR/src/main/java/benchmark/FullChimpBenchmark.java"

# The legacy branch lacks the 'benchmark' Gradle task; append it.
cat >> "$LEGACY_DIR/build.gradle" <<'GEOF'

task benchmark(type: JavaExec) {
    group = 'verification'
    mainClass = 'benchmark.FullChimpBenchmark'
    classpath = sourceSets.main.runtimeClasspath
    workingDir = project.projectDir
    args = ['build/benchmark/legacy.csv', '3']
    if (project.hasProperty('benchmarkArgs')) {
        args = project.property('benchmarkArgs').toString().split(' ') as List
    }
}
GEOF

# Run the harness in the legacy worktree.
(cd "$LEGACY_DIR" && ./gradlew --quiet benchmark --no-daemon \
    -PbenchmarkArgs="$LEGACY_CSV $RUNS" 2>/dev/null) || true
# The legacy worktree writes to its own build/ dir; copy the CSV out.
cp "$LEGACY_DIR/$LEGACY_CSV" "$LEGACY_CSV" 2>/dev/null || true
echo "  wrote $LEGACY_CSV"
echo

# --------------------------------------------------------------------
# 3. Compare
# --------------------------------------------------------------------
echo "=== Comparison ==="
python3 - "$NATIVE_CSV" "$LEGACY_CSV" <<'PYEOF'
import csv, sys

def load(path):
    rows = {}
    with open(path) as f:
        for r in csv.DictReader(f):
            rows[r["label"]] = r
    return rows

native = load(sys.argv[1])
legacy = load(sys.argv[2])

labels = []
for k in legacy:
    if k in native:
        labels.append(k)
labels.sort()

print(f"{'LABEL':<28} {'LEGACY(ms)':>10} {'NATIVE(ms)':>10} {'Δ%':>8} {'PLAN L/N':>8} {'MATCH':>6}")
print("-" * 80)

mismatches = 0
slowdowns = []
speedups = []
native_only_fail = 0
legacy_only_fail = 0

def ms(v):
    try:
        x = int(v)
        return x if x >= 0 else None
    except ValueError:
        return None

for lab in labels:
    n = native[lab]
    l = legacy[lab]
    ns = ms(n["solverMsMedian"])
    ls = ms(l["solverMsMedian"])
    nf = n["found"] == "true"
    lf = l["found"] == "true"

    # correctness: compare action sequences
    na = n["actions"]
    la = l["actions"]
    match = (na == la)

    if nf and not lf:
        native_only_fail += 1  # native solved, legacy didn't (actually an improvement)
    if lf and not nf:
        native_only_fail += 1  # legacy solved, native didn't (regression)

    if not match:
        mismatches += 1

    # timing comparison (only if both solved)
    if ns is not None and ls is not None and ls > 0:
        delta = (ns - ls) * 100.0 / ls
        sign = "+" if delta >= 0 else ""
        delta_s = f"{sign}{delta:.0f}%"
        if delta > 0:
            slowdowns.append((delta, lab, ls, ns))
        elif delta < 0:
            speedups.append((delta, lab, ls, ns))
    else:
        delta_s = "—"

    plan_cmp = f"{l['planLength']}/{n['planLength']}"
    match_s = "✓" if match else "✗"
    ns_s = str(ns) if ns is not None else "FAIL"
    ls_s = str(ls) if ls is not None else "FAIL"
    print(f"{lab:<28} {ls_s:>10} {ns_s:>10} {delta_s:>8} {plan_cmp:>8} {match_s:>6}")

print("-" * 80)
print(f"Mismatches (action sequence differs): {mismatches}/{len(labels)}")
print(f"Solvability differences: {native_only_fail}")
if slowdowns:
    slowdowns.sort(reverse=True)
    print(f"\nSlowdowns (native slower than legacy), top 5:")
    for d, lab, ls, ns in slowdowns[:5]:
        print(f"  {lab:<28} {ls}ms -> {ns}ms  (+{d:.0f}%)")
if speedups:
    speedups.sort()
    print(f"\nSpeedups (native faster than legacy), top 5:")
    for d, lab, ls, ns in speedups[:5]:
        print(f"  {lab:<28} {ls}ms -> {ns}ms  ({d:.0f}%)")
PYEOF
