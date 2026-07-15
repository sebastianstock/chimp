# M15 Step 4: CHIMP-Native Temporal Solver — Implementation Plan

## Context

The temporal solver is the highest-risk component of M15. Unlike the symbolic and
integer solvers (which could be adapted from existing CHIMP sources), the temporal
layer (`AllenIntervalNetworkSolver`, `AllenInterval`, `AllenIntervalConstraint`,
`APSPSolver`, `SimpleDistanceConstraint`, `TimePoint`) lives entirely in the
Meta-CSP framework. We must write a **new implementation** — not a copy.

## What the temporal solver does

### Core: Simple Temporal Network (STN)

The temporal solver is an **STN** — a constraint network over time points where each
constraint is a distance bound `t_j - t_i ∈ [min, max]`. Consistency is checked in
polynomial time via all-pairs shortest paths (Floyd-Warshall, O(n³)).

- Each `AllenInterval` (temporal variable) has two **time points**: start and end.
- The solver maintains a **distance matrix** `d[i][j]` = upper bound on `t_j - t_i`.
- Propagation = recompute the distance matrix via Floyd-Warshall.
- Query `getAdmissibleDistanceBounds(tpFrom, tpTo)` returns `[-d[to][from], d[from][to]]`.

Meta-CSP's `APSPSolver` does exactly this: `fromScratchDistanceMatrixComputation()`
rebuilds the entire matrix on every constraint add/remove. It uses `long[][] distance`
with `INF = Long.MAX_VALUE - 1`.

### Allen relation compilation

Each Allen relation type compiles to 1-4 `SimpleDistanceConstraint`s between the
start/end time points of two intervals. For example:

- **Before** [l,u]: `fe → ts` with `[l, u]` (from-end before to-start)
- **Meets**: `fe → ts` with `[0, 0]` (from-end equals to-start)
- **Equals**: `fs → ts` with `[0,0]` AND `te → fe` with `[0,0]`
- **Duration** [l,u]: `fs → fe` with `[l, u]` (from-start to from-end)
- **Overlaps** [l,u][l,u][l,u]: 3 distance constraints
- **DuringOrEqualsOrStartsOrFinishes**: composite — compiled to 4 distance constraints

The compilation logic in `AllenIntervalConstraint.createInternalConstraints()` is
~200 lines of per-type dispatch. This is mechanical but must be complete.

### What CHIMP actually uses

From the codebase, CHIMP uses these Allen relation types:

| Type | Count | Where |
| --- | --- | --- |
| `Duration` | 14 | FluentConstraint (move duration), domain templates |
| `Overlaps` | 2 | FluentConstraint (OPENS) |
| `OverlappedBy` | 2 | FluentConstraint (CLOSES) |
| `DuringOrEqualsOrStartsOrFinishes` | 2 | FluentConstraint (DC default) |
| `MeetsOrOverlapsOrFinishedByOrContains` | 2 | FluentConstraint (PRE default) |
| `Before` | 2 | FluentConstraint (BEFORE), MoveBase |
| `BeforeOrMeets` | 1 | Used in ordering |
| `Meets` | 1 | FluentConstraint (MATCHES) |
| `Equals` | 1 | FluentConstraint (MATCHES) |
| `MetByOrOverlappedByOrIsFinishedByOrDuring` | 2 | FluentConstraint (PRE alt) |

Plus all types in `TemporalRelation` enum (domain parsing can produce any of them).

**Decision 2 says: port all ~35 relation types.** This is the safe choice — the
domain parser can produce any relation from the enum, and porting only a subset
risks silent failure.

### What the API must provide

From examining all CHIMP call sites:

1. **`TemporalVariable`** (replaces `AllenInterval`):
   - `getEST()`, `getLST()`, `getEET()`, `getLET()` — temporal bounds
   - `getStart()`, `getEnd()` — time points (for distance queries)

2. **`TemporalConstraint`** (replaces `AllenIntervalConstraint`):
   - Constructor: `(Type, bounds...)` or `(Type)` with default bounds
   - `setFrom(TemporalVariable)`, `setTo(TemporalVariable)`
   - `getFrom()`, `getTo()`
   - `clone()`, `isEquivalent()`
   - Internal: compiles to distance constraints

3. **`TemporalSolver`** (replaces `AllenIntervalNetworkSolver`):
   - Constructor: `(origin, horizon)`
   - `createVariables(int num)` → `TemporalVariable[]`
   - `addConstraint(TemporalConstraint)` / `removeConstraint(TemporalConstraint)`
   - `propagate()` — Floyd-Warshall
   - `getAdmissibleDistanceBounds(tpFrom, tpTo)` → `TemporalBounds`
   - `getOrigin()`, `getHorizon()`

## Options considered

### Option A: Implement STN from scratch (RECOMMENDED)

A minimal STN is ~300-400 lines:
- `long[][] distance` matrix
- Floyd-Warshall propagation
- Add/remove distance constraints
- Query bounds

**Pros:**
- No new dependency
- STN is polynomial-time (O(n³)) — simple, well-understood algorithm
- Full control over the API and incremental behavior
- The Meta-CSP APSPSolver is itself a from-scratch implementation — we're replacing
  like-for-like, not adding complexity
- No native libraries, no version conflicts

**Cons:**
- Must implement and test the algorithm
- O(n³) per propagation — but Meta-CSP does the same (`fromScratchDistanceMatrixComputation`)
- Need to handle the ~35 Allen relation compilations

**Estimated effort: ~800-1200 lines** (STN + Allen compilation + types + tests)

### Option B: Use Choco (already a dependency)

Model time points as `IntVar`s, distance constraints as `arithm` constraints.

**Pros:**
- No new dependency
- Already familiar

**Cons:**
- Choco is a general CP solver; STN is a special case with a polynomial algorithm.
  Using Choco means solving an NP-hard problem (general CP) for a polynomial one.
- Choco re-solves from scratch each propagation (like our `IntegerSolver`) — no
  performance benefit over from-scratch STN, and potentially worse for large networks.
- `getAdmissibleDistanceBounds()` (querying the current bounds between two time points)
  is not a natural Choco operation — Choco gives solutions, not per-pair bounds.
- The Meta-CSP APSPSolver was specifically designed for STN; Choco is not.

**Verdict: Worse fit than from-scratch.** Using a general CP solver for a problem
that has a dedicated polynomial algorithm is the wrong abstraction.

### Option C: Use OR-Tools (Google CP-SAT)

OR-Tools has Java bindings and is very powerful.

**Pros:**
- Excellent performance for general CP
- Good Java bindings

**Cons:**
- New heavy dependency (native libraries via JNI — ~50MB per platform)
- Same fundamental mismatch as Choco: general CP for a polynomial STN problem
- `getAdmissibleDistanceBounds()` is not a natural operation
- Overkill — STN is O(n³) with a 20-line Floyd-Warshall

**Verdict: Overkill.** The dependency cost (native libraries, build complexity) is
not justified for a problem that has a 20-line polynomial solution.

### Option D: Use a dedicated STN library

There are academic STN libraries (e.g., `eu.ai4wp.core`, `TREX`), but none are
well-maintained or available on Maven Central.

**Verdict: Not viable.** No mature, packaged Java STN library exists.

## Recommendation: Option A (from-scratch STN)

The STN is the **right abstraction** for the temporal solver. It is:
- Simple (Floyd-Warshall is 20 lines)
- Polynomial (O(n³))
- Well-understood (decades of literature)
- Dependency-free
- Exactly what Meta-CSP's APSPSolver does

Using a general CP library (Choco, OR-Tools) would be using the wrong tool —
solving an NP-hard problem for one that has a polynomial dedicated algorithm.

## Implementation plan

### Step 4a: STN core (distance matrix + Floyd-Warshall) → verify: unit tests

Create `chimp.kernel.temporal.SimpleTemporalNetwork`:
- `long[][] distance` matrix indexed by time point
- Time points 0 and 1 are origin and horizon (anchored)
- `addDistanceConstraint(from, to, min, max)` — adds edge, propagates
- `removeDistanceConstraint(from, to, min, max)` — removes edge, propagates
- `propagate()` — Floyd-Warshall: `d[i][j] = min(d[i][j], d[i][k] + d[k][j])`
- `getAdmissibleDistanceBounds(from, to)` → `[-d[to][from], d[from][to]]`
- Consistency check: `d[i][i] < 0` → negative cycle → inconsistent

**Scope: ~200 lines + unit tests (~150 lines).**
**Verify: STN unit tests (consistency, propagation, add/remove, bound queries,
negative cycle detection).**

### Step 4b: TemporalVariable and TemporalConstraint types → verify: unit tests

Create:
- `chimp.kernel.temporal.TemporalVariable` — wraps two time points (start, end);
  provides `getEST()/getLST()/getEET()/getLET()` via the STN distance matrix
- `chimp.kernel.temporal.TemporalConstraint` — Allen relation type + bounds;
  `compileToDistanceConstraints()` produces distance constraints for the STN

**Scope: ~200 lines + unit tests (~100 lines).**
**Verify: TemporalVariable/Constraint unit tests (bound queries, constraint
compilation for basic types: Before, Meets, Equals, Duration).**

### Step 4c: Allen relation compilation (all ~35 types) → verify: unit tests

Implement `TemporalConstraint.compileToDistanceConstraints()` for all relation
types in the `TemporalRelation` enum:
- 13 atomic Allen relations (Before, After, Meets, MetBy, Overlaps, OverlappedBy,
  During, Contains, Starts, StartedBy, Finishes, FinishedBy, Equals)
- Duration, Release, Deadline
- ~20 composite relations (BeforeOrMeets, DuringOrEquals, etc.)

Each type compiles to 1-4 distance constraints between start/end time points.
The compilation table is derived directly from Meta-CSP's
`AllenIntervalConstraint.createInternalConstraints()`.

**Scope: ~300 lines (compilation table) + unit tests (~200 lines).**
**Verify: Unit test for every relation type — check that the compiled distance
constraints match the expected bounds. Cross-check against Meta-CSP behavior
using a side-by-side test that plans a simple domain and compares temporal bounds.**

### Step 4d: TemporalSolver (full solver) → verify: unit tests + baseline

Create `chimp.kernel.temporal.TemporalSolver`:
- Extends `chimp.kernel.ConstraintSolver`
- Manages `TemporalVariable[]` and `TemporalConstraint[]`
- Delegates propagation to `SimpleTemporalNetwork`
- `createVariables(int num)` → `TemporalVariable[]` (each gets 2 time points)
- `getAdmissibleDistanceBounds(tpFrom, tpTo)`
- `getOrigin()`, `getHorizon()`

**Scope: ~150 lines + unit tests (~100 lines).**
**Verify: Full temporal solver tests (create variables, add constraints, propagate,
query bounds, remove constraints, inconsistency detection). Compare against
Meta-CSP `AllenIntervalNetworkSolver` on identical constraint sets.**

### Step 4e: Cross-validation against Meta-CSP → verify: baseline matches

Write a comparison test that:
1. Creates identical temporal constraint networks in both Meta-CSP
   `AllenIntervalNetworkSolver` and CHIMP-native `TemporalSolver`
2. Queries `getAdmissibleDistanceBounds()` for all time point pairs
3. Asserts the bounds match exactly

This ensures the CHIMP-native solver produces identical results before we
swap it in (Step 5+).

**Scope: ~200 lines (test only).**
**Verify: All bound comparisons pass.**

## Risk assessment

| Sub-step | Risk | Mitigation |
| --- | --- | --- |
| 4a (STN core) | Low — Floyd-Warshall is simple | Test against known STN examples |
| 4b (types) | Low — thin wrappers | Unit tests |
| 4c (Allen compilation) | **Medium** — 35 types, easy to get one wrong | Test each type; cross-validate with Meta-CSP |
| 4d (solver) | Low — delegates to STN | Integration tests |
| 4e (cross-validation) | — | This IS the mitigation |

## Why not use a CP library?

The user asked about OR-Tools and other libraries. The key insight is that **STN is
not a general constraint satisfaction problem** — it is a specific structure with a
polynomial-time algorithm. Using a general CP solver would be:

1. **Slower** — CP solvers don't exploit the STN's distance-graph structure
2. **Less natural** — `getAdmissibleDistanceBounds()` (query per-pair bounds) is not
   a natural CP operation; CP gives solutions, not per-pair bounds
3. **More complex** — mapping STN operations to CP operations adds indirection
4. **Heavier** — OR-Tools adds ~50MB of native libraries; Choco re-solves from scratch

The Meta-CSP `APSPSolver` is itself a from-scratch STN implementation. We are
replacing like-for-like with a cleaner, CHIMP-owned version.

**If performance becomes an issue** (e.g., for very large temporal networks), the
from-scratch STN can be upgraded with incremental algorithms (e.g., incremental
Floyd-Warshall or Johnson's) without changing the API. This is a future optimization,
not needed for M15.
