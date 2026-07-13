# Meta-CSP Coupling and Decoupling Plan

## Summary

CHIMP is a hierarchical hybrid planner implemented in Java 17. It currently depends on the Meta-CSP Framework (`org.metacsp:meta-csp-framework:1.3.5`). The long-term goal is to make CHIMP independent of Meta-CSP and integrate it with the Unified Planning library (UP) while preserving planner semantics.

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

## Current Import Inventory (post-M10)

There are **76 Java files** importing `org.metacsp.*`: 63 under `src/main/java` and 13 under `src/test/java`.

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

## Current Boundary

### What is CHIMP-owned

- **Search algorithm**: `ChimpSearchEngine` owns backtracking, flaw enumeration, resolver application/retraction, rollback, and search-tree visualization.
- **HTNPlanner**: no longer inherits Meta-CSP. Owns meta-constraint list, ground solver, `addMetaConstraint`, `backtrack` (delegates to engine).
- **Model types**: `chimp.model` and `chimp.search` packages are Meta-CSP-free (except `ChimpSearchEngine`/`FlawProvider`/`SearchHooks` which use `ConstraintNetwork` as the resolver representation).
- **Plan extraction**: `CHIMP.extractActions()` returns `PlannedAction[]` (CHIMP-owned, Meta-CSP-free).
- **Parsing**: both parser paths produce neutral `chimp.model` values.
- **Resolver descriptions**: `ResolverDescription` values are produced before compilation to Meta-CSP networks.
- **Postprocessing**: `PlanExtractor` and `HPRGenerator` read through `FluentStore`/`FluentView`.
- **Value ordering**: `CHIMPValOH` hierarchy delegates to CHIMP-owned comparators.
- **Logging**: `ChimpLogging` replaces `MetaCSPLogging`.

### What remains Meta-CSP-coupled

All remaining coupling flows from one root cause: **the resolver and storage representation is `ConstraintNetwork` containing raw Meta-CSP objects** (`VariablePrototype`, `FluentConstraint`, `AllenIntervalConstraint`, `CompoundSymbolicValueConstraint`, `IntegerConstraint`).

The concrete blockers:

1. **`HTNPlanner.addResolverSub`** type-dispatches on `CompoundSymbolicValueConstraint`/`AllenIntervalConstraint`/`IntegerConstraint` to repair resolver scopes after prototype→Fluent materialization. The `ResolverDescription` values (M3) are already produced but `MetaCspResolverAdapter.compile()` still delegates to the legacy `materializeResolverDescriptions()` path — it is a validation wrapper, not a true compiler from descriptions.

2. **6 meta-constraints** still `extend MetaConstraint`/`Schedulable` for the `getMetaVariables`/`getMetaValues` protocol. They are wrapped by `MetaCspFlawProviderAdapter` rather than implementing `FlawProvider` directly.

3. **`Fluent`/`FluentConstraint`/`FluentNetworkSolver`** are Meta-CSP subclasses. They ARE the storage layer. The `unify` and `integers` packages are Meta-CSP solver families.

4. **`CHIMP.extractActions()`** used to return `Variable[]` — migrated to `PlannedAction[]` (M11). No longer forces consumers to import `Variable`.

5. **`ConstraintNetwork`** is the resolver representation throughout `ChimpSearchEngine`/`FlawProvider`/`SearchHooks` and the flaw/resolver annotation system.

6. **`org.metacsp.time.Bounds`** is returned by Meta-CSP solver methods and used in 13 main files. `chimp.model.TemporalBounds` exists but has a different API (record vs. mutable class with `intersect`/`union`).

## Proposed Next Milestones

### 11. Plan Extraction API (done)

`CHIMP.extractActions()` now returns `PlannedAction[]` instead of `Variable[]`.

- Created `chimp.model.PlannedAction` record: immutable snapshot of a planned action (id, component, name, arguments, temporal bounds, integer values). Meta-CSP-free.
- Internal conversion (`toPlannedAction` in `CHIMP.java`) reads Fluent internals at extraction time, replicating the `getShortName()` filtering logic (skip `NONESYMBOL` "n" args, break at first one) for `toString()` compatibility.
- All 10 consumers migrated (5 examples, 4 tests, 1 adapter-boundary test that gets Fluents directly from solver).
- 4 example files now have zero `org.metacsp` imports.
- Main files importing `org.metacsp`: 67→63. Test files: 15→13.
- Full test suite green.

**Remaining**: The internal conversion still reads Meta-CSP types (`CompoundSymbolicVariable`, `AllenInterval`, `IntegerVariable`) — this is expected to access the storage layer at extraction time. The external API is clean.

### 12. Complete Resolver Materialization (M3 completion) — Steps 1–2 done

**Steps 1–2 complete**: `addResolverSub` scope repair is now driven by CHIMP-owned metadata.

- Created `chimp.model.SubVariableKind` enum: `FLUENT`, `SYMBOLIC`, `TEMPORAL`, `INTEGER`.
- Added `MetaCspResolverAdapter.subVariableKindOf(Constraint)` — isolates the `instanceof` dispatch in the adapter.
- Added `MetaCspResolverAdapter.resolveScopeVariable(Variable, SubVariableKind, ConstraintNetwork)` — handles the `Fluent`→sub-variable extraction (including the nested-prototype case for `INTEGER`).
- `compile()` now tags each constraint in the compiled network with its `SubVariableKind` via `tagConstraints()` and stores the map on `ConstraintNetwork.annotation`.
- `HTNPlanner.addResolverSub` reads the tag map and delegates scope repair to `resolveScopeVariable()`. Falls back to `subVariableKindOf()` for untagged resolvers (task unification, non-HTN meta-constraints).
- Removed dead `setAnnotation(flaw)` call in `ChimpSearchEngine` (the flaw annotation was never read).
- **`HTNPlanner` no longer imports `CompoundSymbolicValueConstraint`, `AllenIntervalConstraint`, or `IntegerConstraint`.**
- Full test suite green.

**Remaining (M12 Steps 4–8)**: Make `compile()` truly compile from `ResolverDescription` instead of delegating to `materializeResolverDescriptions()`. The `SubVariableKind` tagging infrastructure is in place; the actual constraint-by-constraint compilation from descriptions is the next work. See `docs/m12-plan.md` for the full plan.

### 13. MetaConstraint/Schedulable Inheritance Break

**Goal**: Drop `extends MetaConstraint`/`extends Schedulable` from the 6 meta-constraint classes. Make them implement `FlawProvider` directly (drop `MetaCspFlawProviderAdapter`).

This requires porting `Schedulable`'s peak-collection logic (`getMetaValues` via `super`) into CHIMP-owned code for `FluentScheduler` and `FluentResourceUsageScheduler`. The other 4 meta-constraints (`HTNMetaConstraint`, `GuessOrderingMetaConstraint`, `MoveBaseMetaConstraint`, `DWRNavigationMetaConstraint`) already have their `getMetaVariables`/`getMetaValues` logic in CHIMP code — the `MetaConstraint` inheritance is only for the protocol interface.

**Scope**: Port `Schedulable` peak-collection (~200 lines) + 6 class declaration changes + drop `MetaCspFlawProviderAdapter`.
**Risk**: Medium — `Schedulable`'s peak-collection is well-tested by resource scheduling tests (DWR 1 resource, TransTerra 23 resources).
**Verify**: `./gradlew test` green; resource scheduling tests pass with identical conflict detection.

### 14. Storage Layer Interface

**Goal**: Define CHIMP-owned interfaces for the storage layer and keep Meta-CSP implementations behind adapters.

This is the largest milestone. It breaks into wedges:

- **14a. Fluent mutation port**: Extend `FluentStore` with write operations (create fluent, add/remove constraint, set marking). Migrate `HTNPlanner.addResolverSub`/`retractResolverSub` to use `FluentStore` writes instead of direct `groundSolver` calls.
- **14b. Resolver network replacement**: Define a CHIMP-owned resolver-network representation to replace `ConstraintNetwork` in `ChimpSearchEngine`/`FlawProvider`/`SearchHooks`. The `ResolverDescription` layer (M3/M12) is the foundation.
- **14c. Temporal bounds**: Make `chimp.model.TemporalBounds` a full drop-in for `org.metacsp.time.Bounds` (add `intersect`/`union`/`isSingleton`/`compareTo`). Convert at the Meta-CSP boundary.
- **14d. Unification and integer solver ports**: Define runtime service ports for symbolic matching/restriction/binding and integer variable creation/arithmetic. Wrap current Meta-CSP-backed implementations.

**Scope**: Major — this is effectively building the CHIMP-native solver kernel.
**Risk**: High — the storage layer is woven into every planning operation. Must be done incrementally, one wedge at a time, with tests green after each.
**Verify**: `./gradlew test` green after each wedge; planning results identical.

### 15. UP Integration

**Goal**: Replace the Meta-CSP solver backend with Unified Planning library implementations.

This is the long-term goal stated in the project instructions. It depends on M14 (storage layer interfaces must exist before the backend can be swapped). The CHIMP-owned interfaces from M14 become the integration points for UP-based implementations.

**Scope**: TBD — depends on M14 outcome and UP library capabilities.
**Risk**: High — new production dependency, semantic alignment between CHIMP's HTN/planning semantics and UP's.

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
