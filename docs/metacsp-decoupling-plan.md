# Meta-CSP Coupling and Decoupling Plan

## Summary

CHIMP is a hierarchical hybrid planner implemented in Java 17. It currently depends on the Meta-CSP Framework (`org.metacsp:meta-csp-framework:1.3.5`). The primary goal is to make CHIMP completely independent of Meta-CSP, replacing the Meta-CSP storage layer with a CHIMP-native solver kernel. A separate, later goal is to make CHIMP available as a planner in the Unified Planning library (UP) — this is an *integration* (CHIMP as a UP planner provider), not a *dependency* (CHIMP must not depend on UP). CHIMP may also be ported to another programming language before UP integration. For constraint propagation, the CHIMP-native solver kernel may use any suitable constraint programming library, not necessarily UP.

The dependency exists at two architectural levels:

```text
Search and meta-solving (CHIMP-owned since M9)
  ChimpSearchEngine ............ owns backtracking, flaw enumeration, resolver application
  HTNPlanner ................... no longer extends MetaConstraintSolver (M9 step 4)
  Meta-constraints ............. still extend MetaConstraint/Schedulable for the
                                 getMetaVariables/getMetaValues protocol

Fluent model and ground solver (Meta-CSP storage layer — the remaining frontier)
  Fluent ........................ extends MultiVariable + Activity
  FluentConstraint .............. extends MultiBinaryConstraint
  FluentNetworkSolver ........... extends MultiConstraintSolver
        |
        +-> CompoundSymbolicVariableConstraintSolver (unify package)
        +-> AllenIntervalNetworkSolver
        +-> IntegerConstraintSolver
```

The search algorithm and CHIMP-specific planning logic are now CHIMP-owned. What remains is the **storage layer**: `Fluent`, `FluentConstraint`, `FluentNetworkSolver`, the unification and integer solver families, and `ConstraintNetwork` as the resolver representation. These are Meta-CSP objects that CHIMP code directly instantiates, queries, and mutates.

## Current Import Inventory (post-M15 Steps 0-5)

There are **76 Java files** importing `org.metacsp.*`: 63 under `src/main/java` and 13 under `src/test/java`. The CHIMP-native solver kernel (24 files, ~3,300 lines) has been built but is not yet connected — the old storage layer still exists and is still used by all consumers.

Import counts will drop as M15 Steps 6-14 migrate consumers to the kernel and delete the old storage layer.

### By layer (main)

| Layer | Files | Notes |
| --- | --- | --- |
| Adapter (`chimp.adapter.metacsp`) | 10 | By design — these are the Meta-CSP boundary |
| Storage (`fluentSolver`, `unify`, `integers`) | 17 | ARE Meta-CSP subclasses — the root cause |
| Meta-constraints (6 classes) | 6 | Extend `MetaConstraint`/`Schedulable` |
| Search engine (`chimp.search`) | 3 | Uses `ConstraintNetwork` as resolver repr |
| HTN domain model | 5 | `PlanReportroryItem`, `HTNMethod`, `HTNOperator`, `EffectTemplate`, `HTNPlanner` |
| CHIMP facade (`planner/CHIMP`) | 1 | `extractActions()` returns `PlannedAction[]` (M11) |
| Domain parsing | 2 | `HybridDomain`, `TestDomainReader` |
| UI | 2 | `HierarchicalLayout`, `PlanHierarchyFrame` |
| External path planning | 4 | `MoveBaseMetaConstraint` + duration estimators |
| Test-main demos (in `main/`) | 17 | `main()` entry points, not `@Test` |
| Examples | 13 | `main()` entry points, not `@Test` |

### Meta-CSP-free packages

| Package | Status |
| --- | --- |
| `chimp.model` | Zero `org.metacsp` imports |
| `chimp.logging` | Zero `org.metacsp` imports |
| `postprocessing` | Zero `org.metacsp` imports (M6) |
| `htn.valOrderingHeuristics` | Zero `org.metacsp` imports (M10) |

## Meta-CSP Inheritance (current)

| CHIMP class | Meta-CSP superclass | Status |
| --- | --- | --- |
| `HTNPlanner` | ~~`MetaConstraintSolver`~~ | **Broken (M9 step 4)** — now `implements HybridDomainPlanner, SearchHooks` |
| `HTNMetaConstraint` | `MetaConstraint` | Still extends — search protocol |
| `DWRNavigationMetaConstraint` | `MetaConstraint` | Still extends — search protocol |
| `MoveBaseMetaConstraint` | `MetaConstraint` | Still extends — search protocol |
| `GuessOrderingMetaConstraint` | `MetaConstraint` | Still extends — search protocol |
| `FluentScheduler` | `Schedulable` | Still extends — peak-collection engine |
| `FluentResourceUsageScheduler` | `Schedulable` | Still extends — peak-collection engine |
| `Fluent` | `MultiVariable`, `Activity` | Storage layer |
| `FluentConstraint` | `MultiBinaryConstraint` | Storage layer |
| `FluentNetworkSolver` | `MultiConstraintSolver` | Storage layer |
| `CompoundSymbolicVariable` | `MultiVariable` | Storage layer (unify) |
| `CompoundSymbolicValueConstraint` | `MultiBinaryConstraint` | Storage layer (unify) |
| `CompoundSymbolicVariableConstraintSolver` | `MultiConstraintSolver` | Storage layer (unify) |
| `NameVariable` | `Variable` | Storage layer (unify) |
| `NameMatchingConstraint` | `BinaryConstraint` | Storage layer (unify) |
| `NameMatchingConstraintSolver` | `ConstraintSolver` | Storage layer (unify) |
| `NameDomain` | `Domain` | Storage layer (unify) |
| `IntegerVariable` | `Variable` | Storage layer (integers) |
| `IntegerConstraint` | `Constraint` | Storage layer (integers) |
| `IntegerConstraintSolver` | `ConstraintSolver` | Storage layer (integers) |
| `IntegerDomain` | `Domain` | Storage layer (integers) |
| `CHIMPValOH` | ~~`ValueOrderingH`~~ → `MetaCspResolverOrderingAdapter` | **Migrated (M5/M10)** — CHIMP-owned comparator hierarchy |
| `GuessOrderingValOH` | `ValueOrderingH` | Still extends — needs constraint count from `ConstraintNetwork` |

All 6 meta-constraint subclasses now implement `htn.GroundSolverHolder` and hold a direct `groundSolver` field instead of using `this.metaCS.getConstraintSolvers()[0]` (M9 step 4). They no longer depend on a `MetaConstraintSolver` reference, but still inherit the `MetaConstraint`/`Schedulable` search protocol.

## Completed Milestones

### M1–M5: Independent Model, Parser, Resolver Descriptions, Search-Facing Boundaries

CHIMP-owned immutable model types in `chimp.model` and `chimp.search`:
`FluentId`, `FluentType`, `FluentSignature`, `FluentNode`, `TemporalBounds`, `TemporalInterval`, `TemporalRelation`, `TemporalConstraintTemplate`, `IntegerConstraintKind`, `ResourceRequirement`, `ResourceCapacity`, `ConstraintKind`, `ChimpConstraint`, `PlanningFlaw`, `TaskPlanningFlaw`, `ResolverCandidate`, `ResolverCandidateMetadata`, `ResolverDescription`, and related description types.

Both domain parser paths produce these neutral values. Adapters under `chimp.adapter.metacsp` compile them into Meta-CSP representations. Value-ordering heuristics delegate to CHIMP-owned lexicographic comparators via `MetaCspResolverOrderingAdapter`.

### M6: Fluent Read Boundary

`FluentView` and `FluentStore` interfaces in `chimp.model` provide read-only access to fluent identity, type, signature, marking, temporal interval, integer values, and constraint queries. Backed by `MetaCspFluentStore` over `FluentNetworkSolver`. Includes `temporalDistance(FluentId, FluentId, TemporalEndpoint, TemporalEndpoint)` backed by the APSP solver.

Consumers migrated: `PlanExtractor` and `HPRGenerator` (both fully migrated, `postprocessing` package has zero `org.metacsp` imports). Esterel export removed entirely as non-core.

### M7: Hide Unification and Integer Solver Construction

All `IntegerConstraint` and `CompoundSymbolicValueConstraint` construction in `PlanReportroryItem` now routes through `MetaCspIntegerConstraintAdapter` and `MetaCspSymbolicConstraintAdapter` factory methods. `PlanReportroryItem` has zero `integers.*` imports and no longer imports `unify.CompoundSymbolicValueConstraint`.

**Honest boundary**: construction is isolated, but live variable reads (`Fluent.getIntegerVariables()`, `CompoundSymbolicVariable.possibleArgumentDifferent()`) and `addResolverSub` type-dispatches (`instanceof IntegerConstraint`, `instanceof CompoundSymbolicValueConstraint`, `instanceof AllenIntervalConstraint`) remain because they operate on the raw Meta-CSP `ConstraintNetwork` resolver representation. This is M9-remaining / storage-layer work.

### M8: Resource Scheduling (absorbed by M1–M5 + M9)

Resource construction isolation was already achieved by M1–M5 (`MetaCspResourceCapacityAdapter`, `MetaCspResourceRequirementAdapter`, `MetaCspStateVariableAdapter`). The remaining coupling (`Schedulable extends MetaConstraint`) is M9-coupled. M8 has no independent wedge left.

### M9: Replace HTN Meta-CSP Inheritance

**Step 1**: Enumerated the exact `MetaConstraintSolver` surface. 11 override hooks (7 trivial), zero `super.` calls, ~150 lines of search algorithm to port, 7 framework methods confirmed unused.

**Step 2**: Defined `ChimpSearchEngine` + `FlawProvider`/`SearchHooks` port in `chimp.search`. Ported the search algorithm (`backtrack`/`backtrackHelper`/`getConflict`/`addResolver`/`retractResolver`) from `MetaConstraintSolver`. JUNG search-tree visualization preserved. 8 unit tests with stub providers/hooks.

**Step 3**: Delegated `HTNPlanner.backtrack()` to `ChimpSearchEngine`. Created `MetaCspFlawProviderAdapter` wrapping `MetaConstraint` as `FlawProvider`. `HTNPlanner implements SearchHooks`.

**Step 4**: **Broke the `MetaConstraintSolver` inheritance.** `HTNPlanner` now `implements HybridDomainPlanner, SearchHooks` directly. All 6 meta-constraint subclasses have a direct `groundSolver` field (via `GroundSolverHolder` interface) instead of `this.metaCS.getConstraintSolvers()[0]`. Re-hosted `addMetaConstraint`, `getConstraintSolvers`, `getDescription` on `HTNPlanner`. Removed 7 trivial inherited-API overrides.

Full test suite green throughout: DWR, RACE, TransTerra, mobipick, integer planning (battery `(100,98)`/`(98,96)`), resource, HPR generation, characterization.

### M10: Migrate Peripheral Code

- **Deleted dispatching/sensing** (7 files): user-approved deletion of non-core peripheral services.
- **Migrated `ValueOrderingH` → `CHIMPValOH`** (16 files): 8 tests + 8 examples. Type-narrowing with no behavior change.
- **Replaced `MetaCSPLogging` with `ChimpLogging`** (26 import lines): CHIMP-owned logging utility, Meta-CSP-free.

Result: 75→67 main files, 21→15 test files importing `org.metacsp`. Full test suite green.

### M15 Steps 0-5: CHIMP-Native Solver Kernel (Partially Complete)

Implemented the foundational CHIMP-native solver kernel (Steps 0-5 of 14), replacing Meta-CSP dependencies for temporal, symbolic, integer, and fluent constraint solving.

**Completed**:
- **Step 0**: Baseline characterization (36 domain/problem pairs)
- **Step 1**: Base types (5 files, 650 lines) — Variable, Constraint, ConstraintNetwork, ConstraintSolver, VariablePrototype
- **Step 2**: Symbolic solver (7 files, 1,154 lines) — AC-3 propagation, compound symbolic variables
- **Step 3**: Integer solver (4 files, 369 lines) — Choco-backed constraint propagation
- **Step 4**: Temporal solver (5 files, 727 lines) — Simple Temporal Network with Floyd-Warshall
- **Step 5**: Fluent solver (3 files, 654 lines) — Composition of symbolic + temporal + integer

**Total**: 24 production files, ~3,300 lines of kernel code, ~2,400 lines of test code
**Tests**: 342 tests passing, baseline preserved
**Zero Meta-CSP imports**: All kernel code is `org.metacsp`-free

**Critical issues identified**:
- ⚠️ 33x performance regression in constraint addition (Floyd-Warshall O(n³) per batch vs. Meta-CSP's incremental updates)
- ⚠️ No integration tests (kernel tested in isolation only)
- ⚠️ Missing cross-validation for symbolic/integer solvers
- ⚠️ Missing API documentation

**Lessons learned**:
- Cross-validation is essential (caught 11 bugs in temporal solver that unit tests missed)
- Adaptation > reimplementation (Step 3 was faster than Step 2)
- Systematic review cycles are necessary for critical infrastructure (Step 4 required 3 review cycles)
- Performance instrumentation must be part of initial implementation

**Status**: Kernel implementation complete but not yet integrated. Steps 6-14 (migration) pending prerequisite fixes (Steps 5a-5d).

## Current State (post-M15 Steps 0-5)

**M15 partially complete**: Steps 0-5 done (kernel implementation), Steps 6-14 pending (migration).

**What is CHIMP-owned (new in M15)**:
- **Solver kernel**: 24 files in `chimp.kernel` package (zero `org.metacsp` imports)
  - Base types: Variable, Constraint, ConstraintNetwork, ConstraintSolver, VariablePrototype
  - Temporal solver: SimpleTemporalNetwork (Floyd-Warshall STN), TemporalVariable, TemporalConstraint, TemporalSolver
  - Symbolic solver: SymbolicVariable, SymbolicDomain, SymbolicConstraint, SymbolicSolver (AC-3), CompoundSymbolicVariable, CompoundSymbolicConstraint, CompoundSymbolicSolver
  - Integer solver: IntegerVariable, IntegerDomain, IntegerConstraint, IntegerSolver (Choco-backed)
  - Fluent solver: Fluent, FluentConstraint, FluentNetworkSolver (composes symbolic + temporal + integer)
- **Performance**: 29x faster variable creation, 6x faster queries than Meta-CSP
- **Tests**: 342 tests passing, baseline preserved

**Critical issues identified**:
- ⚠️ **33x performance regression** in constraint addition (blocking issue) — M17 must be implemented before Step 6
- ⚠️ **Missing integration tests** — kernel tested in isolation only
- ⚠️ **Missing cross-validation** for symbolic/integer solvers (temporal solver had 11 bugs)
- ⚠️ **Missing API documentation** for kernel

**What remains Meta-CSP-coupled**:
All remaining coupling flows from one root cause: **the resolver and storage representation is `ConstraintNetwork` containing raw Meta-CSP objects** (`VariablePrototype`, `FluentConstraint`, `AllenIntervalConstraint`, `CompoundSymbolicValueConstraint`, `IntegerConstraint`).

The concrete blockers:

1. **`HTNPlanner.addResolverSub`** type-dispatches on `CompoundSymbolicValueConstraint`/`AllenIntervalConstraint`/`IntegerConstraint` to repair resolver scopes after prototype→Fluent materialization. The `ResolverDescription` values (M3) are already produced but `MetaCspResolverAdapter.compile()` still delegates to the legacy `materializeResolverDescriptions()` path — it is a validation wrapper, not a true compiler from descriptions.

2. **6 meta-constraints** still `extend MetaConstraint`/`Schedulable` for the `getMetaVariables`/`getMetaValues` protocol. They are wrapped by `MetaCspFlawProviderAdapter` rather than implementing `FlawProvider` directly.

3. **`Fluent`/`FluentConstraint`/`FluentNetworkSolver`** are Meta-CSP subclasses. They ARE the storage layer. The `unify` and `integers` packages are Meta-CSP solver families.

4. **`CHIMP.extractActions()`** used to return `Variable[]` — migrated to `PlannedAction[]` (M11). No longer forces consumers to import `Variable`.

5. **`ConstraintNetwork`** is the resolver representation throughout `ChimpSearchEngine`/`FlawProvider`/`SearchHooks` and the flaw/resolver annotation system.

6. **`org.metacsp.time.Bounds`** is returned by Meta-CSP solver methods and used in 13 main files. `chimp.model.TemporalBounds` exists but has a different API (record vs. mutable class with `intersect`/`union`).

## Proposed Next Milestones

### Immediate Next Steps (Critical Path)

**Before proceeding with M15 Steps 6-14 (migration)**, the following prerequisite steps MUST be completed:

1. **Step 5a: Implement Incremental Floyd-Warshall** (M17) — **CRITICAL**
   - Fix 33x performance regression in temporal constraint addition
   - Estimated: 3-5 days
   - **Blocking**: Cannot proceed to integration with 33x slowdown

2. **Step 5b: Add Cross-Validation for Symbolic and Integer Solvers**
   - Apply systematic comparison against Meta-CSP
   - Estimated: 2-3 days
   - **Rationale**: Temporal solver had 11 bugs only caught through cross-validation

3. **Step 5c: Add Integration Tests**
   - Test kernel with search engine and meta-constraints
   - Estimated: 2-3 days
   - **Rationale**: Kernel tested in isolation only; integration may reveal API mismatches

4. **Step 5d: Create Kernel API Documentation**
   - Document API for migration
   - Estimated: 1-2 days

**Total**: 8-13 days before migration can begin.

**Why this order**: The review revealed that the kernel has a critical performance regression and was tested in isolation. Proceeding with migration (Steps 6-14) without fixing these issues would:
- Carry the 33x performance regression into production
- Miss bugs similar to the 11 temporal solver bugs
- Encounter integration issues late in the process
- Make migration harder without API documentation

**Lessons learned**:
- Cross-validation is essential (caught 11 bugs unit tests missed)
- Adaptation > reimplementation (Step 3 was faster than Step 2)
- Systematic review cycles are necessary for critical infrastructure
- Performance instrumentation must be part of initial implementation

### 11. Plan Extraction API (done)

`CHIMP.extractActions()` now returns `PlannedAction[]` instead of `Variable[]`.

- Created `chimp.model.PlannedAction` record: immutable snapshot of a planned action (id, component, name, arguments, temporal bounds, integer values). Meta-CSP-free.
- Internal conversion (`toPlannedAction` in `CHIMP.java`) reads Fluent internals at extraction time, replicating the `getShortName()` filtering logic (skip `NONESYMBOL` "n" args, break at first one) for `toString()` compatibility.
- All 10 consumers migrated (5 examples, 4 tests, 1 adapter-boundary test that gets Fluents directly from solver).
- 4 example files now have zero `org.metacsp` imports.
- Main files importing `org.metacsp`: 67→63. Test files: 15→13.
- Full test suite green.

**Remaining**: The internal conversion still reads Meta-CSP types (`CompoundSymbolicVariable`, `AllenInterval`, `IntegerVariable`) — this is expected to access the storage layer at extraction time. The external API is clean.

### 12. Complete Resolver Materialization (M3 completion) — done

**M12 complete**: `compile()` builds resolver networks directly from `ResolverDescription` values. The legacy `materializeResolverDescriptions()` method is deleted.

**Steps 1–2 (scope-repair isolation)**:
- Created `chimp.model.SubVariableKind` enum: `FLUENT`, `SYMBOLIC`, `TEMPORAL`, `INTEGER`.
- Added `MetaCspResolverAdapter.subVariableKindOf(Constraint)` — isolates the `instanceof` dispatch in the adapter.
- Added `MetaCspResolverAdapter.resolveScopeVariable(Variable, SubVariableKind, ConstraintNetwork)` — handles the `Fluent`→sub-variable extraction (including the nested-prototype case for `INTEGER`).
- `compile()` tags each constraint with its `SubVariableKind` via `tagConstraints()` and stores the map on `ConstraintNetwork.annotation`.
- `HTNPlanner.addResolverSub` reads the tag map and delegates scope repair to `resolveScopeVariable()`. Falls back to `subVariableKindOf()` for untagged resolvers (task unification, non-HTN meta-constraints).
- Removed dead `setAnnotation(flaw)` call in `ChimpSearchEngine`.
- **`HTNPlanner` no longer imports `CompoundSymbolicValueConstraint`, `AllenIntervalConstraint`, or `IntegerConstraint`.**

**Steps 4–8 (description-driven compilation)**:
- Added `PlanReportroryItem.buildResolverFromDescription()` — builds one `ConstraintNetwork` from one `ResolverDescription`. Reconstructs `preKeyToFluentMap` from `selectedPreconditions` (fluent ID lookup), then creates all constraints in the same order as the legacy method.
- `compile()` calls `buildResolverFromDescription()` per description; skips null results (infeasible sub-different). No validation-by-ID matching needed.
- Deleted `materializeResolverDescriptions()` (~310 lines). The cartesian product and feasibility checking live exclusively in `describeResolvers()`.
- Net: -201 lines. Full test suite green.

**Remaining coupling**: `buildResolverFromDescription()` still accesses `PlanReportroryItem` internal templates (`preconditions`, `effects`, `variableOccurrencesMap`, `additionalConstraints`, `integerConstraintTemplates`, etc.) to create constraints. The `ResolverDescription` carries structural summaries but not all wiring details. Full description-driven compilation (without accessing templates) is M14 work (storage layer interfaces).

### 13. MetaConstraint/Schedulable Inheritance Break — done

**M13 complete**: All 6 meta-constraint classes no longer inherit from `MetaConstraint` or `Schedulable`. They implement `FlawProvider` directly via CHIMP-owned base classes. `MetaCspFlawProviderAdapter` is deleted.

**New base classes**:
- `chimp.search.ChimpMetaConstraint` — abstract base implementing `FlawProvider`. Provides `varOH`/`valOH` fields, `logger`, `currentFlaw()` (varOH-sorted `getMetaVariables`), `resolverOrdering()` (returns `valOH`), `markResolved()` (no-op). Replaces `MetaConstraint`.
- `chimp.search.ChimpSchedulable extends ChimpMetaConstraint` — ports `Schedulable`'s peak-collection (sampling/complete/binary) and MCS resolution logic. Replaces `Schedulable`.

**Migrated 6 meta-constraints**:
- `HTNMetaConstraint`, `GuessOrderingMetaConstraint`, `DWRNavigationMetaConstraint`, `MoveBaseMetaConstraint`: `extends ChimpMetaConstraint`. `getMetaValues(MetaVariable)` → `resolvers(ConstraintNetwork)`. Removed `draw()`, `markResolvedSub()`, `getEdgeLabel()`, `clone()`, `isEquivalent()` stubs.
- `FluentScheduler`, `FluentResourceUsageScheduler`: `extends ChimpSchedulable`. `PEAKCOLLECTION` → `PeakCollection` enum.

**HTNPlanner**: `metaConstraints` list → `List<FlawProvider>`; `addMetaConstraint` accepts `FlawProvider`; `backtrack()` adds providers directly (no adapter wrapping).

**GroundSolverHolder**: Added `getGroundSolver()` to interface (was inherited from `MetaConstraint` abstract method).

**Deleted**: `MetaCspFlawProviderAdapter.java` (49 lines).

**Full test suite green** (170 tests). Net: +572/-329 lines (new base classes add ~450 lines, deletion of adapter + stubs removes ~200).

**Remaining coupling**: `ChimpSearchEngine` still uses `MetaVariable` for JUNG search-tree visualization (`DelegateForest<MetaVariable, ConstraintNetwork>`). This is a container type for graph nodes, not search protocol usage — M14 work.

### 14. Storage Layer Interface — 14c done, 14a partial

**14c (Temporal bounds) — done**: `chimp.model.TemporalBounds` is now a full drop-in for `org.metacsp.time.Bounds`. Added `intersectStrict`, `intersect`, `isIntersecting`, `union`, `isSingleton`, `compareTo`. Migrated `ChimpSchedulable`, `FluentResourceUsageScheduler`, `MoveBaseDurationEstimator` interface + implementations, and `MoveBaseMetaConstraint` to use `TemporalBounds`. Converts to `Bounds` at Meta-CSP constructor boundaries. Bounds imports: 14→10 main files.

**14a (Fluent mutation) — partial**: Added write operations to `FluentStore` interface (`createFluent`, `removeFluents`, `setMarking`). Implemented in `MetaCspFluentStore`. `HTNPlanner.addResolverSub` still uses `groundSolver` directly because the resolver representation is `ConstraintNetwork` (14b work). Also replaced `NullConstraintNetwork` with empty `ConstraintNetwork` in `ChimpSearchEngine`.

**Remaining wedges**:
- **14b (Resolver network replacement)**: Define a CHIMP-owned resolver-network representation to replace `ConstraintNetwork` in `ChimpSearchEngine`/`FlawProvider`/`SearchHooks`. This is the core of the remaining coupling — `ConstraintNetwork` is the flaw representation, resolver representation, and JUNG graph edge type. Replacing it requires changing all 6 meta-constraints, `HTNPlanner`, and the search engine. High risk, large scope.
- **14d (Unification/integer solver ports)**: Define runtime service ports for symbolic matching/restriction/binding and integer variable creation/arithmetic. Wrap current Meta-CSP-backed implementations. Medium risk.

**Import counts**: Main files importing `org.metacsp`: 63→60. Test files: 13.

**Honest assessment**: The remaining coupling is fundamentally in the storage layer. The `fluentSolver/`, `unify/`, and `integers/` packages ARE the Meta-CSP implementation — 17 files that extend Meta-CSP framework classes (`Variable`, `Constraint`, `ConstraintSolver`, `MultiConstraintSolver`). They cannot be "migrated away" with refactoring — they must be **replaced** with a CHIMP-native solver kernel (M15). The 14b and 14d wedges define the interfaces that the CHIMP-native kernel will implement. They are necessary preparation, not pure indirection — they specify the contract that any backend (CHIMP-internal, a CP library, or otherwise) must fulfill.

### 15. CHIMP-Native Solver Kernel — IN PROGRESS (Steps 0-5 complete)

**Status**: Steps 0-5 complete (kernel implementation), Steps 6-14 pending (migration).

**Goal**: Replace the Meta-CSP storage layer (`fluentSolver/`, `unify/`, `integers/`) and Meta-CSP visualization classes with CHIMP-owned implementations that do not extend or use any Meta-CSP framework class.

This is the milestone that achieves **complete decoupling** from Meta-CSP. The 17 storage-layer files currently extend Meta-CSP framework classes (`Variable`, `Constraint`, `ConstraintSolver`, `MultiConstraintSolver`, `MultiVariable`, `MultiBinaryConstraint`, `Domain`, `BinaryConstraint`). They must be replaced with CHIMP-owned equivalents. The Meta-CSP visualization classes (`SearchTreeFrame`, `ConstraintNetworkFrame`, `MetaVariable`) are pure JUNG code with no framework coupling beyond generic type parameters — they are copied into `chimp.ui` and retyped.

After M15, `org.metacsp:meta-csp-framework` is removed from `build.gradle` and `grep -r "org.metacsp" src/` returns zero results.

**Completed (Steps 0-5)**:
- ✅ Step 0: Baseline characterization (36 domain/problem pairs)
- ✅ Step 1: Base types (5 files, 650 lines)
- ✅ Step 2: Symbolic solver (7 files, 1,154 lines)
- ✅ Step 3: Integer solver (4 files, 369 lines)
- ✅ Step 4: Temporal solver (5 files, 727 lines) — **11 bugs fixed through 3 review cycles**
- ✅ Step 5: Fluent solver (3 files, 654 lines)

**Total kernel**: 24 files, ~3,300 lines production code, ~2,400 lines test code
**Test results**: 342 tests passing, baseline preserved
**Zero Meta-CSP imports**: All kernel code is `org.metacsp`-free

**Critical issues identified**:
- ⚠️ **33x performance regression** in constraint addition (blocking issue)
- ⚠️ **Missing integration tests** — kernel tested in isolation only
- ⚠️ **Missing cross-validation** for symbolic/integer solvers
- ⚠️ **Missing API documentation**

**Prerequisite steps before migration (Steps 5a-5d)**:

**Step 5a: Implement Incremental Floyd-Warshall (M17)** — CRITICAL PREREQUISITE
- Fix 33x performance regression in temporal constraint addition
- Implement incremental APSP algorithm (Ramalingam's or incremental Floyd-Warshall)
- Target: CHIMP within 2× of Meta-CSP performance (not 33×)
- Estimated effort: 3-5 days
- **Must be completed before Step 6**

**Step 5b: Add Cross-Validation for Symbolic and Integer Solvers**
- Apply systematic comparison against Meta-CSP (same approach that caught 11 temporal solver bugs)
- Create cross-validation tests for symbolic and integer solvers
- Estimated effort: 2-3 days

**Step 5c: Add Integration Tests**
- Add tests that use the new kernel with search engine and meta-constraints
- Catch API mismatches early before migration
- Estimated effort: 2-3 days

**Step 5d: Create Kernel API Documentation**
- Document the kernel API for migration in Steps 6-14
- Include code examples and migration guide
- Estimated effort: 1-2 days

**Remaining steps (Steps 6-14)**:
- Step 6: CHIMP-native resolver representation
- Step 7: Migrate HTNPlanner.addResolverSub
- Step 8: Migrate PlanReportroryItem.buildResolverFromDescription
- Step 9: Migrate meta-constraints and schedulers
- Step 10: Migrate CHIMP facade and plan extraction
- Step 11: Migrate domain parsing and HTN model
- Step 12: Migrate UI and visualization
- Step 13: Delete Meta-CSP storage layer and remove dependency
- Step 14: Final cleanup and documentation

**Detailed step-wise plan**: `docs/m15-plan.md`

**Depends on**: M14b (resolver network replacement) and M14d (unification/integer solver ports) — both absorbed into this milestone's steps.

**Scope**: Major — ~4,600-5,000 lines of new CHIMP-native code; ~25 production files + ~10 test files deleted; ~5 visualization files copied/retyped.
**Risk**: High — the storage layer is woven into every planning operation. Done incrementally, one solver family at a time, with tests green after each.
**Verify**: `./gradlew test` green; planning results identical across all domains; zero `org.metacsp` imports in the entire codebase; `org.metacsp:meta-csp-framework` removed from `build.gradle`.

**Lessons learned from Steps 0-5**:
1. Cross-validation is essential — unit tests alone caught 0 of 11 temporal solver bugs
2. Adaptation > reimplementation — Step 2 took longer than Step 3 (which was adapted)
3. Systematic review cycles are necessary for critical infrastructure
4. Performance instrumentation must be part of initial implementation, not added later

### 16. Unified Planning Integration (future, separate goal)

**Goal**: Make CHIMP available as a planner in the Unified Planning library (UP).

This is **not** a dependency — CHIMP must not depend on UP. This is an *integration* where CHIMP acts as a UP planner provider, wrapping its planning capabilities behind the UP `Planner` interface. This may happen after M15, or after a port to another programming language.

**Scope**: TBD — depends on M15 outcome and UP library capabilities.
**Risk**: Medium — adapter layer, no changes to CHIMP core.

**Goal**: Make CHIMP available as a planner in the Unified Planning library (UP).

This is **not** a dependency — CHIMP must not depend on UP. This is an *integration* where CHIMP acts as a UP planner provider, wrapping its planning capabilities behind the UP `Planner` interface. This may happen after M15, or after a port to another programming language.

**Scope**: TBD — depends on M15 outcome and UP library capabilities.
**Risk**: Medium — adapter layer, no changes to CHIMP core.

## Verification Strategy

Each milestone should preserve existing planning results. Verification includes:

- Existing domain JUnit tests (DWR, RACE, TransTerra, mobipick, integer planning).
- Fluent network solver tests.
- Unification tests, including retraction.
- Resource and negative-effect examples.
- HPR output tests.
- Characterization tests.
- Comparisons of resolver ordering and final plans where practical.

## Engineering Rules

- Small, reviewable changes. Every change compiles and keeps `./gradlew test` green.
- No large rewrites. Each step moves one concept behind a CHIMP-owned abstraction.
- No speculative abstractions or layers beyond what the current step requires.
- Meta-CSP is kept temporarily as an adapter/backend, not removed directly.
- Preserve existing public behavior unless the task explicitly says otherwise.
- Keep Java 17 compatibility.
- Avoid adding production dependencies without asking.
