# M15: CHIMP-Native Solver Kernel — Step-wise Plan

## Goal

Replace the 17 Meta-CSP storage-layer files (`fluentSolver/`, `unify/`, `integers/`),
all `org.metacsp.framework`/`org.metacsp.multi`/`org.metacsp.time` usage, **and** the
Meta-CSP visualization classes with CHIMP-owned implementations. After M15, CHIMP has
**zero** `import org.metacsp.*` across the entire codebase, and
`org.metacsp:meta-csp-framework` is removed from `build.gradle`.

**Visualization is replaced too.** The Meta-CSP visualization surface is only two
self-contained JUNG viewers (`SearchTreeFrame` ~247 lines, `ConstraintNetworkFrame`
~256 lines) plus `MetaVariable` as a JUNG vertex container (~100 lines). They have
**zero** coupling to Meta-CSP framework logic — they are pure JUNG code whose only
Meta-CSP dependency is their generic type parameters (`<MetaVariable,
ConstraintNetwork>`) and `MetaVariable.toString()`. Replacing them is mechanical:
copy the BSD-licensed viewers into `chimp.ui`, retype the generics to CHIMP-owned
types, and update call sites. CHIMP's own `PlanHierarchyFrame` and
`HierarchicalLayout` are already CHIMP-owned — they just need their JUNG types
retyped when the storage layer is replaced.

## Current state (post-M14)

The search algorithm, meta-constraints, model types, plan extraction, parsing,
postprocessing, and logging are already CHIMP-owned. What remains Meta-CSP-coupled
flows from **one root cause**: the resolver and storage representation is
`ConstraintNetwork` containing raw Meta-CSP objects (`VariablePrototype`,
`FluentConstraint`, `AllenIntervalConstraint`, `CompoundSymbolicValueConstraint`,
`IntegerConstraint`).

### The storage layer (17 files, ~2,793 lines)

| Package | File | Extends | Lines | Role |
| --- | --- | --- | --- | --- |
| `fluentSolver` | `Fluent.java` | `MultiVariable` + `Activity` | 155 | Fluent = composite variable (symbolic + temporal + integer sub-vars) |
| `fluentSolver` | `FluentConstraint.java` | `MultiBinaryConstraint` | 314 | Fluent-level binary constraint (MATCHES, PRE, DC, OPENS, CLOSES, etc.) — creates internal Allen + symbolic constraints |
| `fluentSolver` | `FluentNetworkSolver.java` | `MultiConstraintSolver` | 188 | Composes 3 sub-solvers (symbolic, temporal, integer); provides fluent queries |
| `unify` | `CompoundSymbolicVariable.java` | `MultiVariable` | 315 | Predicate(name + args) as composite of `NameVariable`s |
| `unify` | `CompoundSymbolicValueConstraint.java` | `MultiBinaryConstraint` | 192 | MATCHES/SUBMATCHES/restrictions → creates internal `NameMatchingConstraint`s |
| `unify` | `CompoundSymbolicVariableConstraintSolver.java` | `MultiConstraintSolver` | 67 | Multi-solver over `NameMatchingConstraintSolver`s (one per argument position) |
| `unify` | `NameMatchingConstraintSolver.java` | `ConstraintSolver` | 234 | AC-3 arc-consistency over boolean-domain `NameVariable`s |
| `unify` | `NameMatchingConstraint.java` | `BinaryConstraint` | 82 | EQUALS/DIFFERENT/UNARYEQUALS/UNARYDIFFERENT |
| `unify` | `NameVariable.java` | `Variable` | 86 | String-domain variable backed by `NameDomain` |
| `unify` | `NameDomain.java` | `Domain` | 225 | Boolean array domain (which symbols are possible) |
| `integers` | `IntegerConstraintSolver.java` | `ConstraintSolver` | 187 | Choco-based propagation for ARITHM/ALLDIFFERENT/ALLEQUAL |
| `integers` | `IntegerConstraint.java` | `Constraint` | 85 | Arithmetic/all-diff/all-equal constraint |
| `integers` | `IntegerVariable.java` | `Variable` | 49 | Integer variable backed by `IntegerDomain` |
| `integers` | `IntegerDomain.java` | `Domain` | 81 | Single int value + instantiated/constant flags |

### The consumers (non-storage, non-test, non-example)

These files access the storage layer directly and must be migrated to CHIMP-owned
interfaces:

| Consumer | What it does with Meta-CSP | Count |
| --- | --- | --- |
| `chimp.adapter.metacsp.*` (10 files) | Adapts CHIMP model → Meta-CSP objects | By design |
| `htn/HTNPlanner` | `addResolverSub`: prototype→Fluent materialization, scope repair | 1 |
| `htn/PlanReportroryItem` | `buildResolverFromDescription`: creates `FluentConstraint`, `AllenIntervalConstraint`, `VariablePrototype`, `IntegerConstraint` | 1 |
| `htn/HTNMetaConstraint` | Reads `groundSolver.getVariables()`, `getCompoundSymbolicVariable()`, `getAllenInterval()`, `getFluentConstraintsOfTypeTo()`, creates `VariablePrototype` | 1 |
| `htn/HTNMethod`, `HTNOperator`, `EffectTemplate`, `OrderingConstraintTemplate` | Reference `FluentConstraint.Type` | 4 |
| `dwr/DWRNavigationMetaConstraint` | Reads `AllenInterval`, creates `AllenIntervalConstraint` | 1 |
| `externalPathPlanning/MoveBaseMetaConstraint` | Creates `AllenIntervalConstraint`, reads `AllenInterval` | 1 |
| `resourceFluent/FluentScheduler` | Reads `getCompoundSymbolicVariable()`, creates `CompoundSymbolicValueConstraint` for conflict check | 1 |
| `resourceFluent/FluentResourceUsageScheduler` | Filters resolvers by `AllenIntervalConstraint` type, reads `Fluent.getMarking()` | 1 |
| `chimp.search.ChimpSearchEngine` | `addResolver`/`retractResolver`: constraint propagation via `Constraint.getScope()[0].getConstraintSolver()`; JUNG `MetaVariable`/`ConstraintNetwork` | 1 |
| `chimp.search.ChimpMetaConstraint` | `valOH` is `Comparator<ConstraintNetwork>` | 1 |
| `chimp.search.ChimpSchedulable` | Peak collection: `AllenInterval.getEST/getEET`, `APSPSolver`, `MCSData`, `PowerSet`, `AllenIntervalConstraint` | 1 |
| `chimp.search.ChimpResolver` | Wraps `ConstraintNetwork` as delegate | 1 |
| `planner/CHIMP` | Creates `FluentNetworkSolver`, calls `getAllenInterval()`, `getCompoundSymbolicVariable()`, `getIntegerVariables()` for extraction | 1 |
| `hybridDomainParsing/*` | Parsing reads `FluentConstraint.Type` | 2 |
| `ui/HierarchicalLayout`, `PlanHierarchyFrame` | Visualization reads fluents | 2 |

### What already works (CHIMP-owned abstractions in place)

- `FluentStore` / `FluentView` — read boundary with `FluentId`, `FluentType`, `FluentSignature`, `FluentMarking`, `TemporalInterval`, `IntegerValue`, `ChimpConstraint`, `ConstraintKind`. Write ops defined (`createFluent`, `removeFluents`, `setMarking`) but not used by `addResolverSub` yet.
- `TemporalBounds` — full drop-in for `Bounds`.
- `ChimpFlaw` / `ChimpResolver` — CHIMP-owned search types (wrap `ConstraintNetwork` internally).
- `SubVariableKind` — scope-repair tagging.
- `MetaCspResolverAdapter` — isolates `instanceof` dispatch and scope resolution.

## Strategy

**Bottom-up, one solver family at a time.** Each step replaces one Meta-CSP dependency
with a CHIMP-owned equivalent, keeping `./gradlew test` green. The key insight: the
`chimp.adapter.metacsp` package already isolates most construction. M15 replaces the
*implementation* behind the interfaces, then removes the Meta-CSP classes.

The plan follows a **parallel implementation** strategy: build CHIMP-native equivalents
alongside the Meta-CSP ones, swap consumers one at a time, then delete the Meta-CSP
versions. This avoids a flag-day rewrite.

### Design principles

1. **No speculative abstractions.** Each CHIMP-native class mirrors the exact API
   surface its consumers need — nothing more.
2. **Reuse the existing `chimp.model` types.** `FluentId`, `FluentSignature`,
   `TemporalBounds`, `TemporalInterval`, `ConstraintKind`, etc. are already defined.
   The kernel uses them directly.
3. **Keep Choco for integer propagation.** CHIMP already depends on Choco
   (`org.chocosolver`). The integer solver is a thin Choco wrapper — it stays as-is,
   just re-hosted in a CHIMP-owned class.
4. **Implement a minimal STN for temporal propagation.** The Allen interval algebra
   is the most complex piece. Rather than porting Meta-CSP's APSP solver, implement a
   minimal distance-graph STN (simple, well-understood, ~200 lines). Allen relation
   types are compiled to distance bounds at construction time.
5. **Keep the search-tree visualization working.** JUNG `DelegateForest` stays. The
   edge/vertex types change from `ConstraintNetwork`/`MetaVariable` to CHIMP-owned
   types. `SearchTreeFrame.draw()` is replaced by a CHIMP-owned visualization wrapper
   (or the `draw()` method is adapted to use the new types).

## Current State (post-Step 5)

**COMPLETED**: Steps 0-5 (kernel implementation)
- ✅ Step 0: Baseline characterization (36 domain/problem pairs)
- ✅ Step 1: Base types (5 files, 650 lines)
- ✅ Step 2: Symbolic solver (7 files, 1,154 lines)
- ✅ Step 3: Integer solver (4 files, 369 lines)
- ✅ Step 4: Temporal solver (5 files, 727 lines) — **11 bugs fixed through 3 review cycles**
- ✅ Step 5: Fluent solver (3 files, 654 lines)

**Total kernel**: 24 files, ~3,300 lines production code, ~2,400 lines test code
**Test results**: 342 tests passing, baseline preserved
**Zero Meta-CSP imports**: All kernel code is `org.metacsp`-free

**CRITICAL ISSSES IDENTIFIED**:
1. **33x performance regression** in constraint addition (blocking issue)
   - Temporal solver: 207ms vs 6.2ms for 50 constraints on 100 variables
   - Root cause: O(n³) Floyd-Warshall per batch vs. Meta-CSP's incremental updates
   - **Must be fixed before integration**

2. **Missing integration tests** — kernel tested in isolation only
   - No tests with search engine, meta-constraints, or planning facade
   - Integration may reveal missing functionality or API mismatches

3. **Missing cross-validation** for symbolic and integer solvers
   - Temporal solver had 11 bugs only caught through cross-validation
   - Same systematic comparison needed for symbolic/integer solvers

4. **Missing API documentation** for kernel
   - No reference document for migration in Steps 6-14

**LESSONS LEARNED**:
1. Cross-validation is essential — unit tests alone caught 0 of 11 temporal solver bugs
2. Adaptation > reimplementation — Step 2 took longer than Step 3 (which was adapted)
3. Systematic review cycles are necessary for critical infrastructure
4. Performance instrumentation must be part of initial implementation, not added later

### Step 0: Inventory and test scaffolding → verify: `./gradlew test` green

Before changing anything, establish a characterization safety net:
- Run the full suite and confirm 170 tests pass.
- Document the exact planning results (number of fluents, constraints, plan length)
  for DWR, RACE, TransTerra, mobipick, integer planning — these are the regression
  targets.
- No code changes. This step exists to make every subsequent step's verification
  concrete.

### Step 1: CHIMP-native variable and constraint base types → verify: compile + test green

Create `chimp.kernel` package with the fundamental types that replace
`org.metacsp.framework.Variable`, `Constraint`, `ConstraintSolver`, `Domain`:

```
chimp.kernel.Variable          — int id, String name, marking, solver back-ref
chimp.kernel.Constraint        — Variable[] scope, Type enum, clone(), isEquivalent()
chimp.kernel.ConstraintNetwork — holds variables + constraints, substitution map
                                   Uses REFERENCE EQUALITY (decision 5)
chimp.kernel.ConstraintSolver  — add/remove constraints, propagate(), create variables
chimp.kernel.VariablePrototype — placeholder for unmaterialized variables (decision 1)
```

These are **not** abstract framework classes with 50 hooks. They are concrete,
minimal types with exactly the methods CHIMP uses. `ConstraintNetwork` replaces the
Meta-CSP `ConstraintNetwork` as the resolver representation. It uses reference
equality (not content-based) to avoid the JUNG duplicate-edge class of bug.

`VariablePrototype` is kept as a CHIMP-native placeholder (decision 1) — resolvers
are compiled before fluents are materialized, so the indirection is needed.

**Scope**: ~6 files, ~350 lines. Pure additive — nothing depends on them yet.
**Verify**: compiles; existing tests still green (no behavior change).

### Step 2: CHIMP-native name/symbolic solver → verify: new unit tests pass + suite green

Replace the `unify` package (7 files, 1,512 lines) with CHIMP-native equivalents:

```
chimp.kernel.symbolic.SymbolicVariable     — replaces NameVariable (boolean[] domain)
chimp.kernel.symbolic.SymbolicDomain       — replaces NameDomain
chimp.kernel.symbolic.SymbolicConstraint   — replaces NameMatchingConstraint (EQUALS, DIFFERENT, ...)
chimp.kernel.symbolic.SymbolicSolver       — replaces NameMatchingConstraintSolver (AC-3)
chimp.kernel.symbolic.CompoundSymbolicVariable       — replaces CompoundSymbolicVariable
chimp.kernel.symbolic.CompoundSymbolicConstraint     — replaces CompoundSymbolicValueConstraint
chimp.kernel.symbolic.CompoundSymbolicSolver         — replaces CompoundSymbolicVariableConstraintSolver
```

Port the AC-3 propagation algorithm verbatim from `NameMatchingConstraintSolver` —
it is self-contained and well-tested. Port `NameDomain`'s boolean-array logic
verbatim. The compound variable's `setName`/`getName`/`getShortName`/`getGroundArgs`/
`possibleMatch`/`possibleArgumentMatch`/`possibleArgumentDifferent` methods are copied
with `NameVariable` → `SymbolicVariable`.

Write unit tests mirroring the existing `unify` tests:
- `JUnitTestNameMatchingConstraintSolver` → `SymbolicSolverTest`
- `JunitTestCompoundNameMatchingConstraintSolver` → `CompoundSymbolicSolverTest`
- `TestNameVariable` → `SymbolicVariableTest`

**Scope**: ~7 files, ~1,500 lines + 3 test files.
**Verify**: new unit tests pass; full suite green (old unify package still in place,
not yet swapped).

### Step 3: CHIMP-native integer solver → verify: new unit tests pass + suite green

Replace the `integers` package (4 files, 521 lines):

```
chimp.kernel.integer.IntegerVariable       — replaces IntegerVariable
chimp.kernel.integer.IntegerDomain         — replaces IntegerDomain
chimp.kernel.integer.IntegerConstraint     — replaces IntegerConstraint (ARITHM, ALLDIFFERENT, ALLEQUAL)
chimp.kernel.integer.IntegerSolver         — replaces IntegerConstraintSolver (Choco-backed)
```

Port the Choco propagation logic verbatim. The `IntegerConstraintSolver` already
uses Choco directly — this is a re-hosting, not a reimplementation.

Write unit tests mirroring `TestIntegerConstraintSolver`.

**Scope**: ~4 files, ~400 lines + 1 test file.
**Verify**: new unit tests pass; full suite green.

### Step 4: CHIMP-native temporal solver (STN) → verify: new unit tests pass + suite green

Replace `AllenIntervalNetworkSolver` + `AllenInterval` + `AllenIntervalConstraint`
with a CHIMP-native STN:

```
chimp.kernel.temporal.TemporalVariable      — replaces AllenInterval (EST, LST, EET, LET)
chimp.kernel.temporal.TemporalConstraint    — replaces AllenIntervalConstraint
chimp.kernel.temporal.TemporalSolver        — replaces AllenIntervalNetworkSolver (distance-graph STN)
```

Port **all** ~15 Allen relation types (decision 2): Before, After, Meets, MetBy,
Overlaps, OverlappedBy, During, Contains, Starts, StartedBy, Finishes, FinishedBy,
Equals, Duration, plus composite types (e.g., `DuringOrEqualsOrStartsOrFinishes`,
`MeetsOrOverlapsOrFinishedByOrContains`). Each maps to distance bounds between
start/end time points at constraint construction time.

The STN implementation:
- Each `TemporalVariable` has two time points (start, end).
- The solver maintains a distance graph and runs Floyd-Warshall (or incremental) to
  propagate bounds.
- `getAdmissibleDistanceBounds(timePoint1, timePoint2)` returns the current bounds
  from the distance graph.

**Scope**: ~3 files, ~500-800 lines + 1-2 test files. This is the highest-risk step.
**Verify**: new STN unit tests pass (consistency, propagation, retraction, all Allen
relation types); full suite green. Compare temporal bounds from DWR/RACE plans
against Step 0 baseline.

**Risk mitigation**: If a full STN is too large for one step, split into:
- Step 4a: `TemporalVariable` + `TemporalConstraint` (data types only, no propagation)
- Step 4b: `TemporalSolver` with Floyd-Warshall propagation
- Step 4c: Allen relation → bounds compilation (all ~15 types)

### Step 5: CHIMP-native fluent and fluent network solver → verify: new unit tests pass + suite green

Replace `fluentSolver` (3 production files, 760 lines):

```
chimp.kernel.fluent.Fluent                 — replaces Fluent (composite: symbolic + temporal + integer)
chimp.kernel.fluent.FluentConstraint       — replaces FluentConstraint (MATCHES, PRE, DC, OPENS, CLOSES, ...)
chimp.kernel.fluent.FluentNetworkSolver    — replaces FluentNetworkSolver (composes symbolic + temporal + integer)
```

`Fluent` is a composite variable holding one `CompoundSymbolicVariable`, one
`TemporalVariable`, and N `IntegerVariable`s. `FluentConstraint.createInternalConstraints`
creates the sub-level constraints (same logic as current). `FluentNetworkSolver`
delegates to the three sub-solvers from Steps 2-4.

Port `getOpenFluents()`, `getFluentConstraintsOfTypeTo/From()`, `getConstraintsTo()`
queries.

**Propagation strategy experiment** (decision 4): Try both options and pick the
simpler one that keeps tests green:
- **Option A (delegated)**: `propagate()` is a no-op; sub-solvers propagate on
  `addConstraints` (current Meta-CSP model).
- **Option B (centralized)**: `propagate()` calls all sub-solvers' `propagate()` in
  sequence.

Write unit tests mirroring `JUnitTestFluentNetworkSolver`.

**Scope**: ~3 files, ~700 lines + 1 test file.
**Verify**: new unit tests pass; full suite green.

### Step 5a: Implement Incremental Floyd-Warshall (M17) → verify: performance test passes

**CRITICAL PREREQUISITE** — Must be completed before proceeding to Step 6.

**Goal**: Fix the 33x performance regression in temporal constraint addition.

**Current problem**:
- Temporal solver: 207ms vs 6.2ms (Meta-CSP) for 50 constraints on 100 variables
- Root cause: O(n³) Floyd-Warshall executed per constraint batch
- Meta-CSP uses incremental updates that only recompute affected entries

**Implementation approach**:
1. Research incremental APSP algorithms (incremental Floyd-Warshall or Ramalingam's algorithm)
2. Modify `SimpleTemporalNetwork` to support incremental updates
3. When adding a single distance edge, update only affected entries in the distance matrix
4. Maintain the same correctness guarantees as full Floyd-Warshall

**Verification**:
- `TemporalPerformanceTest.addConstraints` shows CHIMP within 2× of Meta-CSP (not 33×)
- All existing temporal solver tests still pass
- Baseline preservation confirmed

**Risk**: Medium-High — incremental algorithms are complex and error-prone. Requires careful testing.

**Estimated effort**: 3-5 days

### Step 5b: Add Cross-Validation for Symbolic and Integer Solvers → verify: cross-validation tests pass

**Goal**: Apply the same systematic comparison against Meta-CSP that caught 11 temporal solver bugs.

**Implementation**:
1. Create `SymbolicSolverCrossValidationTest` comparing CHIMP vs Meta-CSP symbolic solver
   - Test all symbolic constraint types (MATCHES, SUBMATCHES, etc.)
   - Test compound symbolic variables with various argument patterns
   - Compare domain reductions after constraint propagation

2. Create `IntegerSolverCrossValidationTest` comparing CHIMP vs Meta-CSP integer solver
   - Test all integer constraint types (ARITHM, ALLDIFFERENT, ALLEQUAL)
   - Test various variable configurations
   - Compare domain reductions after constraint propagation

3. Run cross-validation tests and fix any bugs found

**Verification**:
- Cross-validation tests pass for both symbolic and integer solvers
- All existing unit tests still pass
- Any bugs found are fixed before proceeding

**Risk**: Medium — may reveal bugs similar to temporal solver

**Estimated effort**: 2-3 days

### Step 5c: Add Integration Tests → verify: integration tests pass

**Goal**: Add tests that use the new kernel with the search engine and meta-constraints to catch API mismatches early.

**Implementation**:
1. Create `KernelIntegrationTest` that:
   - Creates a simple planning problem (e.g., DWR with 2 locations)
   - Uses CHIMP-native kernel instead of Meta-CSP storage
   - Runs search engine with the native kernel
   - Verifies that a valid plan is produced

2. Test meta-constraint integration:
   - Use CHIMP-native constraints with meta-constraints
   - Verify flaw detection and resolution

3. Test fluent solver integration:
   - Create fluent networks with native kernel
   - Verify constraint propagation works correctly

**Verification**:
- Integration tests pass
- Plans produced are valid (correct ordering, resource usage, temporal constraints)
- No API mismatches between kernel and consumers

**Risk**: High — integration may reveal missing functionality or API incompatibilities

**Estimated effort**: 2-3 days

### Step 5d: Create Kernel API Documentation → verify: documentation complete

**Goal**: Document the kernel API to help with migration in Steps 6-14.

**Implementation**:
1. Create `docs/kernel-api.md` with:
   - Overview of kernel architecture
   - API reference for each solver family (temporal, symbolic, integer, fluent)
   - Code examples showing common usage patterns
   - Migration guide comparing old Meta-CSP API to new CHIMP-native API
   - Performance characteristics and optimization notes

2. Include:
   - Class diagrams or descriptions
   - Method signatures with explanations
   - Cross-references to relevant tests
   - Known limitations and workarounds

**Verification**:
- Documentation is complete and accurate
- Examples in documentation compile and run
- Migration guide covers all major API changes

**Risk**: Low — documentation task

**Estimated effort**: 1-2 days

## Migration Steps (Steps 6-14) CHIMP-native resolver representation → verify: compile + test green

Replace `ConstraintNetwork` as the resolver representation in the search engine:

- `ChimpResolver` wraps `chimp.kernel.ConstraintNetwork` instead of
  `org.metacsp.framework.ConstraintNetwork`.
- `ChimpSearchEngine.addResolver`/`retractResolver`: constraint propagation now goes
  through `chimp.kernel.ConstraintSolver.addConstraints`/`removeConstraints`.
- `ChimpMetaConstraint.valOH`: change from `Comparator<ConstraintNetwork>` to
  `Comparator<chimp.kernel.ConstraintNetwork>`.
- `ChimpSchedulable`: peak collection uses `chimp.kernel.temporal.TemporalVariable`
  instead of `AllenInterval`; `MCSData`/`PowerSet` replaced by CHIMP-owned equivalents
  (or inlined — `PowerSet` is trivial).
- **JUNG visualization** (decision 3): The search tree uses CHIMP-owned
  `SearchTreeNode` (trivial container with `toString()`) instead of `MetaVariable`.
  `draw()` calls `chimp.ui.SearchTreeFrame` (copied from Meta-CSP, retyped to
  `<SearchTreeNode, SearchTreeEdge>`).

**Scope**: ~8 files changed, ~300 lines new (including copied viz viewer).
**Verify**: full suite green; JUNG visualization still renders correctly (manual
check on one example).

### Step 7: Migrate HTNPlanner.addResolverSub → verify: test green

`addResolverSub` is the last direct consumer of `VariablePrototype` and
`ConstraintNetwork` substitution maps. Migrate it to use the CHIMP-native resolver
representation:

- `VariablePrototype` → `chimp.kernel.VariablePrototype` (decision 1: keep
  placeholder). The CHIMP-native prototype carries `(component, symbol, arguments)`
  in typed fields (not `Object[]`).
- Scope repair: `MetaCspResolverAdapter.resolveScopeVariable` → CHIMP-native
  equivalent that extracts sub-variables from `chimp.kernel.fluent.Fluent`.
- `FluentStore.createFluent` / `setMarking` are now actually used (M14a completion).

**Scope**: ~3 files changed.
**Verify**: full suite green; plan results identical to Step 0 baseline.

### Step 8: Migrate PlanReportroryItem.buildResolverFromDescription → verify: test green

`buildResolverFromDescription` creates `FluentConstraint`, `AllenIntervalConstraint`,
`VariablePrototype`, `IntegerConstraint` directly. Migrate to create CHIMP-native
types instead. The `MetaCspIntegerConstraintAdapter`, `MetaCspSymbolicConstraintAdapter`,
`MetaCspTemporalConstraintAdapter` either become CHIMP-native or are absorbed into
`PlanReportroryItem` directly.

**Scope**: ~5 files changed.
**Verify**: full suite green; resolver networks identical (compare constraint
counts/types per resolver).

### Step 9: Migrate meta-constraints and schedulers → verify: test green

Migrate the 6 meta-constraints + 2 schedulers to use CHIMP-native types:

- `HTNMetaConstraint`: `groundSolver.getVariables()` → `FluentNetworkSolver.getFluents()`,
  `getCompoundSymbolicVariable()` → CHIMP-native compound symbolic, etc.
- `DWRNavigationMetaConstraint`, `MoveBaseMetaConstraint`: `AllenIntervalConstraint` →
  `chimp.kernel.temporal.TemporalConstraint`.
- `FluentScheduler.isConflicting()`: create `CompoundSymbolicConstraint` (CHIMP-native)
  for conflict check.
- `FluentResourceUsageScheduler`: filter resolvers by CHIMP-native constraint types.
- `GuessOrderingMetaConstraint`: `ConstraintNetwork.getConstraints().length` →
  `chimp.kernel.ConstraintNetwork.getConstraints().length`.

**Scope**: ~8 files changed.
**Verify**: full suite green.

### Step 10: Migrate CHIMP facade and plan extraction → verify: test green

`CHIMP.java` creates `FluentNetworkSolver` and extracts actions by reading
`getAllenInterval()`, `getCompoundSymbolicVariable()`, `getIntegerVariables()`.
Migrate to use `FluentStore`/`FluentView` (already defined in M6/M11) backed by the
CHIMP-native solver.

**Scope**: ~2 files changed.
**Verify**: full suite green; `PlannedAction[]` output identical.

### Step 11: Migrate domain parsing and HTN model → verify: test green

`HybridDomain`, `OperatorParser`, `HTNMethod`, `HTNOperator`, `EffectTemplate`,
`OrderingConstraintTemplate` reference `FluentConstraint.Type`. Replace with a
CHIMP-owned enum (or reuse `ConstraintKind` which already maps 1:1).

**Scope**: ~6 files changed.
**Verify**: full suite green.

### Step 12: Migrate UI and visualization → verify: test green + manual visualization check

Replace the remaining Meta-CSP visualization code with CHIMP-owned equivalents:

- **`SearchTreeFrame`** (Meta-CSP, 247 lines): Copy to `chimp.ui.SearchTreeFrame`,
  retype `Forest<MetaVariable, ConstraintNetwork>` → `Forest<SearchTreeNode,
  SearchTreeEdge>`. Already partially done in Step 6; finalize here.
- **`ConstraintNetworkFrame`** (Meta-CSP, 256 lines): Copy to `chimp.ui.NetworkViewer`,
  retype to CHIMP-native `Variable`/`Constraint`. Used by `ConstraintNetwork.draw()`
  in examples/tests — provide a CHIMP-native `draw()` method.
- **`PlanHierarchyFrame`** (already CHIMP-owned, 147 lines): Retype
  `Graph<Fluent, FluentConstraint>` → CHIMP-native `Fluent`/`FluentConstraint`.
  Already done when the storage layer was replaced (Step 5); verify here.
- **`HierarchicalLayout`** (already CHIMP-owned, 283 lines): Same — retyped in Step 5.
- Update ~30 `ConstraintNetwork.draw()` / `planner.draw()` call sites in examples
  and tests to use CHIMP-native `draw()` methods.

**Scope**: ~5 new files (copied viewers), ~30 call sites updated.
**Verify**: full suite green; all visualizations render correctly (manual check:
search tree, plan hierarchy, constraint network viewer).

### Step 13: Delete Meta-CSP storage layer and remove dependency → verify: test green + zero `org.metacsp` imports

Delete:
- `fluentSolver/` package (4 production files)
- `unify/` package (7 production + 3 test files)
- `integers/` package (4 production + 1 test files)
- `chimp.adapter.metacsp` package (10 files) — no longer needed
- All Meta-CSP test files that directly test deleted classes

**Remove** `org.metacsp:meta-csp-framework` from `build.gradle`.

**Scope**: ~25 production files deleted, ~10 test files deleted, 1 build.gradle change.
**Verify**: `./gradlew test` green. Confirm:
- `grep -r "import org.metacsp" src/` → **zero results** (entire codebase is clean)
- `grep -r "org.metacsp" build.gradle` → zero results

### Step 14: Final cleanup and documentation → verify: full suite green + zero `org.metacsp`

- Remove any dead code orphaned by the deletion.
- Update `docs/metacsp-decoupling-plan.md` to mark M15 complete.
- Update `AGENTS.md` if architecture description changed.
- Run `grep -r "org.metacsp" src/` → expect **zero results**.
- Run `grep "metacsp" build.gradle` → expect zero results.
- Run full suite one final time.

**Verify**: `./gradlew test` green; zero `org.metacsp` references anywhere.

## Risk assessment

| Step | Risk | Mitigation |
| --- | --- | --- |
| **Step 5a (incremental FW)** | **High** — Incremental algorithms are complex and error-prone | Extensive testing against full Floyd-Warshall; performance benchmarks at multiple scales |
| **Step 5b (cross-validation)** | **Medium** — May reveal bugs similar to temporal solver | Systematic comparison against Meta-CSP; fix bugs before proceeding |
| **Step 5c (integration tests)** | **High** — Integration may reveal missing functionality | Test with simple problems first; iterate on API mismatches |
| **Step 5d (documentation)** | **Low** — Documentation task | Include code examples that compile and run |
| Step 6 (resolver repr) | Medium — touches search engine core | Keep JUNG visualization working at every sub-step |
| Step 7 (addResolverSub) | Medium — scope repair logic is subtle | Keep `MetaCspResolverAdapter.resolveScopeVariable` logic verbatim, just swap types |

## Lessons learned from Steps 0-5

1. **Cross-validation is essential**: The temporal solver had 11 bugs that were only caught through cross-validation testing against Meta-CSP. Unit tests alone caught 0 of these bugs. Cross-validation must be applied systematically to all solver families, not just temporal.

2. **Adaptation > reimplementation**: Step 2 (symbolic solver) was reimplemented from scratch and took longer than Step 3 (integer solver) which was adapted from existing code. For remaining steps, prefer adaptation: copy existing code and change imports/superclasses rather than reimplementing from scratch.

3. **Systematic review cycles are necessary**: Step 4 required 3 review cycles to get right. Each cycle caught different categories of bugs (edge direction errors, structural errors, state management errors). Critical infrastructure like solvers requires multiple review cycles.

4. **Performance instrumentation must be early**: The 33x performance regression was only discovered after the kernel was complete. Performance tests should be part of the initial implementation, not added later. The incremental Floyd-Warshall optimization should have been planned from the start.

5. **Integration testing is critical**: The kernel was tested in isolation but not with the search engine or meta-constraints. Integration tests should be added before migration to catch API mismatches early.

6. **Pure additive approach works**: Keeping old Meta-CSP code untouched during kernel implementation allowed safe parallel development and easy rollback. This approach should continue through the migration steps.

## Resolved decisions

1. **VariablePrototype**: Keep a CHIMP-native placeholder (e.g., `FluentPrototype`).
   Do not eliminate the indirection — the placeholder pattern is needed because
   resolvers are compiled before fluents are materialized.

2. **Allen relation compilation**: Port **all** ~15 Allen relation types (including
   composite types like `DuringOrEqualsOrStartsOrFinishes`). Porting only the
   currently-used subset risks silent breakage if a domain uses a relation that
   wasn't enumerated.

3. **JUNG visualization**: Replace Meta-CSP visualization entirely. The Meta-CSP viz
   surface (`SearchTreeFrame`, `ConstraintNetworkFrame`, `MetaVariable`) is pure JUNG
   code with no framework coupling beyond generic type parameters. Copy these
   BSD-licensed viewers into `chimp.ui`, retype generics to CHIMP-owned types, update
   call sites. `org.metacsp:meta-csp-framework` is removed from `build.gradle`.
   `PlanHierarchyFrame` and `HierarchicalLayout` are already CHIMP-owned and just need
   their JUNG types retyped.

4. **Constraint propagation strategy**: Try **both** options individually during
   implementation (Step 5), evaluate, and pick the simpler one that keeps tests
   green:
   - **Option A (delegated)**: `FluentNetworkSolver.propagate()` is a no-op;
     sub-solvers propagate individually on `addConstraints` (current Meta-CSP model).
   - **Option B (centralized)**: `FluentNetworkSolver.propagate()` calls all
     sub-solvers' `propagate()` in sequence.

5. **`ConstraintNetwork.equals()`**: The CHIMP-native `ConstraintNetwork` uses
   **reference equality** (default `Object.equals()`). This avoids the duplicate-edge
   bug fixed in M14, where content-based equality caused JUNG to reject distinct
   empty networks as duplicates.

## Estimated effort

| Steps | Lines (new/changed) | Effort |
| --- | --- | --- |
| 0-5 (completed) | ~3,300 new, ~2,400 test | 10 days |
| 5a (incremental FW) | ~500 | 3-5 days |
| 5b (cross-validation) | ~400 test | 2-3 days |
| 5c (integration tests) | ~300 test | 2-3 days |
| 5d (documentation) | ~200 docs | 1-2 days |
| 6 | ~200 | 2 days |
| 7-12 | ~1,000 (migrations) | 3-4 days |
| 13-14 | deletions | 1 day |
| **Total** | ~4,600-5,000 | **24-30 days** (10 days done, 14-20 remaining) |
