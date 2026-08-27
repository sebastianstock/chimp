# Full-CHIMP Benchmark: Legacy Meta-CSP vs CHIMP-Native Kernel

## Goal

Compare **performance**, **correctness**, and **functionality** of the full CHIMP
planner across every textual planning domain/problem pair, between:

- **legacy** — the `legacy-metacsp` branch (Meta-CSP backend, commit `c56dd39`)
- **native** — the current `remove_metacsp-m15` branch (CHIMP-native kernel)

This mirrors the M15 Step 4 temporal-solver cross-validation benchmark, but at the
**whole-planner** level: real domain/problem files parsed and solved end-to-end.

The motivating observation: `TestRACEDomain` / `test_m_serve_coffee_problem_1.pdl`
takes ~1.4s now vs <1s before. We need to know whether this is a genuine solver
regression, Gradle/JVM noise, or a per-domain outlier — across the full domain set.

---

## Design Decision: Measure Solver Time, Not Wall-Clock

The current `benchmark.sh` times `./gradlew run` end-to-end, which includes
~3–5s of Gradle daemon + JVM startup. For small problems (most of the suite
solve in <500ms), this startup noise dominates and makes comparisons
meaningless.

**Decision**: Write an in-JVM benchmark harness (a `main` class) that:
1. Builds `CHIMP` via the stable facade API.
2. Times only `chimp.generatePlan()` using `System.nanoTime()` (and cross-checks
   with `chimp.getPlanningTime()`, which the facade already records).
3. Captures the plan (`extractActions()`) for correctness comparison.
4. Writes results to a CSV file.

This is the same approach M15 Step 4 used (a JUnit test measuring in-JVM time),
not the wall-clock `benchmark.sh` approach.

---

## Design Decision: One Portable Harness, Two Branches

The harness must run on **both** branches without source changes. Survey of the
`planner.CHIMP` facade on both branches:

| API | legacy (`c56dd39`) | native (current) | Portable? |
|-----|--------------------|------------------|-----------|
| `new CHIMP.CHIMPBuilder(domain, problem)` | ✅ | ✅ | ✅ |
| `.horizon(long)` | ✅ | ✅ | ✅ |
| `.htnUnification(boolean)` | ✅ | ✅ | ✅ |
| `.mbEstimator(MoveBaseDurationEstimator)` | ✅ | ✅ | ✅ |
| `.build()` → `CHIMP` | ✅ | ✅ | ✅ |
| `chimp.generatePlan()` → `boolean` | ✅ | ✅ | ✅ |
| `chimp.getPlanningTime()` → `long` (ms) | ✅ | ✅ | ✅ |
| `chimp.extractActions()` → `PlannedAction[]` | ✅ | ✅ | ✅ |
| `.valHeuristic(...)` | `ValueOrderingH` / `Comparator<ResolverCandidate>` | `Comparator<ConstraintNetwork>` | ❌ |

`PlannedAction` is an identical `record` (name, arguments, est, lst, eet, let)
on both branches.

**Decision**: The harness calls `valHeuristic()` **not at all** — both branches
default to `UnifyFewestsubsEarliesttasksNewestbindingsValOH`, so the default
behaviour is comparable. This avoids the one incompatible API surface.

The harness is committed to the current branch. For the legacy branch, the
`benchmark.sh` worktree checkout copies the harness file into the legacy
source tree before compiling (it is not committed to `legacy-metacsp`).

**Tradeoff surfaced**: By not setting `valHeuristic`, the benchmark uses the
default heuristic rather than the `UnifyDeepestWeightNewestbindingsValOH` that
`Step0Baseline` and `TestRACEDomain` use. This means the benchmark does not
reproduce the exact `TestRACEDomain` configuration. If exact reproduction
matters, we need a branch-specific valHeuristic shim (see "Open Questions"
below).

---

## Design Decision: Correctness = Action-Sequence Match

Two plans are "the same" if they contain the same actions (name + arguments)
in the same order (sorted by EST). Time bounds may differ slightly due to STN
implementation differences, so we compare **action identity**, not exact time
values. A mismatch is flagged as a potential correctness regression.

---

## The Harness: `src/main/java/benchmark/FullChimpBenchmark.java`

A single `main` class that:

```
for each (label, domainFile, problemFile, needsMB, htnUnification) in SUITE:
    warm up:    build + generatePlan once (discard result)   [optional, see below]
    for run in 1..N:
        chimp = new CHIMP.CHIMPBuilder(domain, problem)
                    .htnUnification(htnUnification)
                    .mbEstimator(needsMB ? new LookUpTableDurationEstimator() : null)
                    .build()
        t0 = System.nanoTime()
        found = chimp.generatePlan()
        t1 = System.nanoTime()
        plan  = found ? chimp.extractActions() : []
        solverMs = chimp.getPlanningTime()
        wallMs   = (t1 - t0) / 1e6
        record(label, found, plan.length, actionNames(plan), solverMs, wallMs)
    write CSV row: median(solverMs), median(wallMs), found, planLength, actions
```

**Warm-up**: The first run of each problem pays JIT/class-loading costs. We run
each problem `N` times (default 3) and report the **median** solver time, after
a single warm-up run that is discarded.

**Output**: `build/benchmark/<branch>.csv` with columns:
```
label,domain,problem,found,planLength,solverMsMedian,wallMsMedian,actions
```
where `actions` is a `;`-separated list of `name(arg1,arg2)` sorted by EST.

### Why a `main` class, not a JUnit test?

A JUnit test (`Step0Baseline` style) would work, but:
- JUnit adds startup overhead and mixes test-framework time into the measurement.
- A `main` class can be invoked identically on both branches via a Gradle
  `JavaExec` task, with no JUnit dependency.
- The comparison script reads CSV files, not JUnit XML.

---

## The Domain/Problem Suite

Sourced from `Step0Baseline` (the authoritative domain/problem mapping) plus
additional pairs the user asked to include ("all the different textual planning
domains and problems"). Total: ~50 pairs across 7 domain families.

| Domain family | Domain file | # problems | Needs MB? | htnUnification |
|---------------|-------------|-----------|-----------|----------------|
| DWR | `domains/dwr/dwr.ddl` | 18 | no | true |
| RACE | `domains/ordered_domain.ddl` | 10 | yes | true |
| TransTerra | `domains/transterra_v1.ddl` | 7 | yes | true |
| Mobipick | `domains/mobipick/simple_navigation/domain.ddl` | 1 | yes | true |
| Integer | `domains/simple_integer_domain.ddl` | 1 | no | true |
| Gecko | `domains/gecko.ddl` | 1 (`problems/gecko/gecko1.pdl`) | ? | true |
| ICO | `domains/ico.ddl` | 2 (`problems/ico/*.pdl`) | yes | true |

The full (label, domain, problem) list is hardcoded in the harness — same source
as `Step0Baseline`'s `@Test` methods, so the suite stays in sync with the test
suite.

**Excluded**: problems that are known-unsolvable or require external services
(e.g. `iros_incremental_merging_initial.pdl` needs a running ROS system). The
harness records `found=false` rather than crashing on failures; a problem that
solves on one branch but not the other is itself a finding.

---

## The Comparison Script: `scripts/compare-benchmark.sh`

1. Run the harness on the current branch → `build/benchmark/native.csv`.
2. `git worktree add` the legacy branch into `build/legacy-worktree`.
3. Copy `FullChimpBenchmark.java` into the worktree's `src/main/java/benchmark/`.
4. Run the harness there → `build/benchmark/legacy.csv`.
5. Diff the two CSVs and print a report:

```
LABEL                  LEGACY(solver)  NATIVE(solver)  Δ%      PLAN  MATCH
DWR-leave              45ms            52ms            +15%    1/1   ✓
RACE-serve_coffee      612ms           1403ms          +129%   8/8   ✓
TransTerra-sample      FAIL            88ms            —       0/3   ✗
...
SUMMARY: 47/50 plans match, 3 mismatches, 2 native-only failures
         median slowdown: 1.08×  |  worst: RACE-serve_coffee (2.29×)
```

Columns:
- **solver time**: `chimp.getPlanningTime()` (median of N runs)
- **Δ%**: native vs legacy solver time
- **PLAN**: `legacyLength / nativeLength`
- **MATCH**: ✓ if action sequences (name+args, sorted by EST) are identical

---

## Implementation Steps

1. **Write `src/main/java/benchmark/FullChimpBenchmark.java`** → verify: compiles
   on current branch, produces `build/benchmark/native.csv`.
2. **Add a `benchmark` Gradle task** (`JavaExec`) in `build.gradle` that runs the
   harness with the working directory set to the repo root (so domain/problem
   relative paths resolve) → verify: `./gradlew benchmark` writes CSV.
3. **Write `scripts/compare-benchmark.sh`** (evolved from `benchmark.sh`) that runs
   the harness on both branches via worktree, then diffs CSVs → verify: produces
   a comparison report for the DWR domain subset.
4. **Run the full suite** on both branches → verify: all ~50 pairs complete (or
   fail gracefully) on both, report is produced.
5. **Analyse the report**: identify per-domain regressions, investigate the worst
   outliers (starting with RACE-serve_coffee).
6. **Profile** the worst native outliers using the JVM profiler (`-agentpath` or
   manual `System.nanoTime()` instrumentation in the hot kernel path), focusing
   on `SimpleTemporalNetwork` (full rebuild on retraction) and the CHIMP facade
   path.

---

## Open Questions (decisions needed before implementing)

1. **valHeuristic**: The harness uses the **default** heuristic
   (`UnifyFewestsubs...`) on both branches for portability. But `TestRACEDomain`
   and `Step0Baseline` use `UnifyDeepestWeightNewestbindingsValOH`. If you want
   the benchmark to reproduce the exact `TestRACEDomain` configuration, we need a
   branch-specific shim (reflection or a tiny per-branch adapter file). **Which
   do you prefer: portable default, or exact `TestRACEDomain` reproduction?**

2. **Warm-up runs**: Should each problem run with a JIT warm-up (1 discarded run +
   N measured), or cold-start only (N runs, report median)? Warm-up gives cleaner
   solver-only numbers; cold-start better reflects real user experience. **Which?**

3. **Run count (N)**: 3 runs (fast, ~2min total) or 5 runs (more stable, ~5min)?
   Small problems have high variance with N=3.
