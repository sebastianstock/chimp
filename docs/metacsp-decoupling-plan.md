# Meta-CSP Coupling and Decoupling Plan

## Summary

CHIMP is tightly coupled to Meta-CSP at three architectural levels:

```text
HTN meta-solving
  HTNPlanner -> MetaConstraintSolver
  HTNMetaConstraint -> MetaConstraint
          |
          v
Fluent model and ground solver
  Fluent -> MultiVariable + Activity
  FluentConstraint -> MultiBinaryConstraint
  FluentNetworkSolver -> MultiConstraintSolver
          |
          +-> compound-symbolic/unification solvers
          +-> AllenIntervalNetworkSolver
          +-> IntegerConstraintSolver
```

The build directly depends on `org.metacsp:meta-csp-framework:1.3.5`.

The dependency is not limited to infrastructure. Core CHIMP concepts such as fluents, decomposition constraints, flaws, resolver alternatives, temporal constraints, unification, integer expressions, and resource conflicts are represented using Meta-CSP classes.

## Initial Independent Model

The Meta-CSP-independent value layer lives in `chimp.model` and `chimp.search`. Model and resolver descriptions are integrated incrementally while Meta-CSP remains the active backend.

| Independent model | Current representation | Initial mapping |
| --- | --- | --- |
| `FluentId` | `Fluent.getID()` | Stable non-negative fluent identity. |
| `FluentType` | `Fluent.getTypeStr()` and component strings | Maps `Activity`, `State`, `PlannedState`, and `Task` to explicit enum values. |
| `FluentSignature` | `CompoundSymbolicVariable` through `Fluent.getName()` | Predicate plus an immutable ordered argument list. It does not model symbolic domains or unresolved alternatives yet. |
| `TemporalBounds` | `org.metacsp.time.Bounds` | Immutable minimum and maximum range for duration or relation parameters. |
| `TemporalInterval` | `Fluent.getAllenInterval()` | Immutable snapshot of EST, LST, EET, and LET. |
| `TemporalRelation` | `AllenIntervalConstraint.Type` | CHIMP-owned names for the supported Allen relations and temporal operators. |
| `TemporalConstraintTemplate` | Parser-created `AllenIntervalConstraint` | Immutable relation and ordered bounds used before solver materialization. |
| `IntegerConstraintKind` | `IntegerConstraint.Type` in parser templates | CHIMP-owned names for arithmetic, all-different, and all-equal integer relations. |
| `ResourceRequirement` | `ResourceUsageTemplate` in parser output | Immutable resource name, optional fluent type, parameter requirements, and usage level. |
| `ResourceCapacity` | Parser-created `FluentResourceUsageScheduler` | Immutable resource name and capacity definition. |
| `FluentNode` | `Fluent` | Immutable semantic snapshot composed from ID, type, signature, and temporal interval. It intentionally excludes solver references, markings, and integer-variable domains. |
| `ConstraintKind` | `FluentConstraint.Type` | Uses domain names for the current relation set: `DC` becomes `DECOMPOSITION`, `PRE` becomes `PRECONDITION`, `UNARYAPPLIED` becomes `APPLIED`, and `MOVEDURATION` becomes `MOVE_DURATION`. |
| `ChimpConstraint` | `FluentConstraint` | Immutable kind and endpoint IDs, with optional temporal bounds for bounded relations. Planner-specific payloads such as repository items, resource templates, axiom text, connection arrays, depth, and additional Allen constraints remain outside this initial model. |
| `PlanningFlaw` / `TaskPlanningFlaw` | Meta-variable `ConstraintNetwork` | Immutable ordered IDs of the eligible tasks grouped into the current HTN flaw. |
| `ResolverCandidate` | Resolver `ConstraintNetwork` | Neutral expansion or task-unification candidate with immutable search metadata. |
| `ResolverCandidateMetadata` | Network and fluent inspection in value heuristics | Candidate kind, task and optional repository/match IDs, depth, optional weight, subtask count, task EST, selected-precondition EST sum, and binding count. |

`chimp.adapter.metacsp.MetaCspModelAdapter` provides the initial one-way boundary. It reads a grounded Meta-CSP `Fluent` into a `FluentNode` and maps a `FluentConstraint` into a `ChimpConstraint`. Duration bounds are copied when present. The adapter fails explicitly for unknown fluent components, non-ground signatures, and non-fluent constraint endpoints. Conversion remains outside the independent model package.

`chimp.adapter.metacsp.MetaCspSearchAdapter` is the active search boundary. It attaches neutral flaws and resolver candidates to Meta-CSP networks through specialized annotations and retains network inspection as a compatibility fallback. `MetaCspResolverOrderingAdapter` lets Meta-CSP call CHIMP-owned lexicographic resolver comparators; the existing `*ValOH` classes remain source-compatible wrappers.

Resolver descriptions and search-facing ordering metadata are complete for Milestone 5. Fluent-store and solver-service interfaces remain deferred to Milestone 6.

## Import Inventory

There are 98 Java files importing `org.metacsp.*`: 84 under `src/main/java` and 14 under `src/test/java`.

### Production and Example Sources

- `dispatching`: `FluentDispatcher`, `FluentDispatchingFunction`
- `dwr`: `DWRNavigationMetaConstraint`
- `examples`: `ChimpIO`, `TestDWRDomain`, `TestTransTerraProblems`, `VariousTests`
- `examples.chimp`: `TestICODomainCHIMP`, `TestIncrementalMerging`, `TestIncrementalMergingDispatching`, `TestMobipickSimple`, `TestPlanningWithIntegerConstraints`, `TestRACEDomain`
- `examples.eval`: `EvalDWRDomain`, `EvalDWRDomainArgs`, `EvalRACEDomain`
- `examples.negative_precondition_resource`: `TestRemoveAllSamples`
- `externalPathPlanning`: `LookUpTableDurationEstimator`, `LookUpTableDurationEstimatorICO`, `MoveBaseDurationEstimator`, `MoveBaseMetaConstraint`, `TestMoveBaseLookup`
- `fluentSolver`: `Fluent`, `FluentConstraint`, `FluentNetworkSolver`, `TestFluentNetworkSolver`
- `htn`: `AdditionalConstraintTemplate`, `EffectTemplate`, `HTNMetaConstraint`, `HTNMethod`, `HTNOperator`, `HTNPlanner`, `PlanReportroryItem`, `TestFluentScheduling`
- `htn.guessOrdering`: `GuessOrderingMetaConstraint`, `GuessOrderingValOH`, `TestGuessOrdringDWR`
- `htn.valOrderingHeuristics`: `CHIMPValOH` and all concrete ordering heuristics
- `hybridDomainParsing`: `HybridDomain`, `PlanReportroiryItemParser`, `ProblemParser`
- `hybridDomainParsing.classic.antlr`: `ChimpClassicReader`, `TestDomainReader`
- `integers`: `IntegerConstraint`, `IntegerConstraintSolver`, `IntegerDomain`, `IntegerVariable`
- `old_dev.axioms`: `AxiomMetaConstraint`, `TestAxiomMetaConstraint`
- `planner`: `CHIMP`
- `postprocessing`: `EsterelGenerator`, `HPRGenerator`, `PlanExtractor`, `HPRtypes.Duration`
- `resourceFluent`: `FluentResourceUsageScheduler`, `FluentScheduler`, `TestResourceUsageScheduler`
- `sensing`: `FluentConstraintNetworkAnimator`, `FluentSensor`, `TestFluentConstraintNetworkAnimator`, `TestFluentDispatching`
- `ui`: `HierarchicalLayout`, `PlanHierarchyFrame`
- `unify`: `CompoundSymbolicValueConstraint`, `CompoundSymbolicVariable`, `CompoundSymbolicVariableConstraintSolver`, `NameDomain`, `NameMatchingConstraint`, `NameMatchingConstraintSolver`, `NameVariable`, and three solver tests

### Test Sources

- `domains`: `JUnitTestDWRDomain`, `JUnitTestRACEDomain`, `JUnitTestRACEDomainMethods`
- `htn`: `JUnitTestFluentNetworkSolver`
- `output`: `JUnitTestCHIMPIOOutput`
- `postprocessing`: `JUnitTestEsterelGeneration`, `JUnitTestHPRGeneration`
- `transterra_domains`: `JUnitTestTransterraDomain`
- `unify`: `JUnitTestNameMatchingConstraintSolver`

## Meta-CSP Inheritance

| CHIMP class | Meta-CSP superclass or interface |
| --- | --- |
| `Fluent` | `MultiVariable`, `Activity` |
| `FluentConstraint` | `MultiBinaryConstraint` |
| `FluentNetworkSolver` | `MultiConstraintSolver` |
| `HTNPlanner` | `MetaConstraintSolver` |
| `HTNMetaConstraint` | `MetaConstraint` |
| `FluentScheduler` | `Schedulable` |
| `FluentResourceUsageScheduler` | `Schedulable` |
| `CompoundSymbolicVariable` | `MultiVariable` |
| `CompoundSymbolicValueConstraint` | `MultiBinaryConstraint` |
| `CompoundSymbolicVariableConstraintSolver` | `MultiConstraintSolver` |
| `NameVariable` | `Variable` |
| `IntegerVariable` | `Variable` |
| `NameDomain` | `Domain` |
| `IntegerDomain` | `Domain` |
| `NameMatchingConstraint` | `BinaryConstraint` |
| `NameMatchingConstraintSolver` | `ConstraintSolver` |
| `IntegerConstraintSolver` | `ConstraintSolver` |
| `IntegerConstraint` | `Constraint` |
| `DWRNavigationMetaConstraint` | `MetaConstraint` |
| `MoveBaseMetaConstraint` | `MetaConstraint` |
| `GuessOrderingMetaConstraint` | `MetaConstraint` |
| `AxiomMetaConstraint` | `MetaConstraint` |
| `CHIMPValOH` | `ValueOrderingH` |
| `GuessOrderingValOH` | `ValueOrderingH` |

## Requested Type Dependency Map

### `Variable`

Used centrally by `Fluent`, `FluentConstraint`, `FluentNetworkSolver`, `HTNPlanner`, `HTNMetaConstraint`, `HTNMethod`, `PlanReportroryItem`, integer and unification classes, schedulers, problem parsing, dispatching, sensing, postprocessing, UI, examples, and evaluations.

### `Constraint`

Used by the fluent solver and HTN layers, `HTNMethod`, `HTNOperator`, resource schedulers, meta-constraints, integer and unification solvers, planner APIs, dispatching, postprocessing, UI, examples, and tests.

### `ConstraintNetwork`

Used by `HTNPlanner`, `HTNMetaConstraint`, `PlanReportroryItem`, all custom meta-constraints, value-ordering heuristics, resource schedulers, dispatching, sensing, the planner facade, examples, and solver tests.

Within HTN planning, a `ConstraintNetwork` serves several CHIMP-level roles:

- A flaw or meta-variable description.
- A candidate resolver.
- A collection of newly created fluents and constraints.
- A substitution map from `VariablePrototype` to concrete variables.

### `VariablePrototype`

Used by `EffectTemplate`, `PlanReportroryItem`, `HTNMethod`, `HTNOperator`, `HTNMetaConstraint`, `HTNPlanner`, and `DWRNavigationMetaConstraint`.

It represents both future fluents and, through nested parameters, future integer variables belonging to future fluents. `HTNPlanner.addResolverSub()` contains type-specific materialization logic for these prototypes.

### `MultiVariable`

Used by:

- `Fluent`, combining symbolic, temporal, and optional integer variables.
- `CompoundSymbolicVariable`, combining name variables.
- `CompoundSymbolicValueConstraint`, when inspecting internal variables.

### `MultiBinaryConstraint`

Extended by:

- `FluentConstraint`
- `CompoundSymbolicValueConstraint`

Both translate one CHIMP-level relation into multiple lower-level solver constraints.

### `MetaConstraint`

Extended by `HTNMetaConstraint`, `DWRNavigationMetaConstraint`, `MoveBaseMetaConstraint`, `GuessOrderingMetaConstraint`, and `AxiomMetaConstraint`. It is also exposed through planner and domain configuration APIs and used directly by examples and evaluations.

### `MetaConstraintSolver`

Extended only by `HTNPlanner`. This is the highest-level Meta-CSP dependency because it supplies search, resolver application, retraction, conflict handling, and meta-constraint orchestration.

### `MetaVariable`

Used by `HTNPlanner`, `HTNMetaConstraint`, `FluentResourceUsageScheduler`, and the custom meta-constraints for navigation, move-base planning, ordering, and axioms.

### `AllenInterval`

Used by `Fluent`, `FluentConstraint`, `FluentNetworkSolver`, `HTNMetaConstraint`, `PlanExtractor`, and `HierarchicalLayout`.

The temporal extent of a fluent is therefore exposed directly as a Meta-CSP object throughout planning and output code.

### `AllenIntervalConstraint`

Used by the fluent constraint and solver layers, HTN templates and expansion, both domain parsing implementations, problem parsing, resource schedulers, dispatching, sensing, and temporal examples.

### `Bounds`

Used by the fluent constraint and solver layers, HTN repository items and parsers, resources, dispatching, sensing, external path planning, and temporal examples.

## CHIMP Semantic Coupling

### Fluent

`Fluent` extends `MultiVariable` and implements Meta-CSP `Activity`. Its internal-variable layout is part of its behavior:

1. `CompoundSymbolicVariable`
2. `AllenInterval`
3. Zero or more `IntegerVariable` instances

Access to integer variables depends on solver ingredient offsets. The class also uses Meta-CSP marking, component, solver, variable ID, domain, and temporal APIs. It is therefore both the CHIMP semantic entity and a Meta-CSP storage object.

### FluentConstraint

`FluentConstraint` extends `MultiBinaryConstraint` and combines CHIMP relations with solver compilation.

Its relation types include:

- `MATCHES`
- `DC`
- `PRE`
- `OPENS`
- `CLOSES`
- `BEFORE`
- `UNARYAPPLIED`
- `MOVEDURATION`
- `AXIOM`
- `RESOURCEUSAGE`

`createInternalConstraints()` translates these relations directly into `AllenIntervalConstraint` and `CompoundSymbolicValueConstraint` objects. This mixes semantic intent, temporal defaults, unification, and Meta-CSP implementation details in one class.

### FluentNetworkSolver

`FluentNetworkSolver` extends `MultiConstraintSolver` and hard-codes the composition of:

- `CompoundSymbolicVariableConstraintSolver`
- `AllenIntervalNetworkSolver`
- Optional `IntegerConstraintSolver`

It also queries the temporal solver directly for admissible distance bounds and exposes variables and constraints through Meta-CSP types. The fixed ingredient ordering is relied upon by `Fluent` and resolver materialization.

### HTNPlanner

`HTNPlanner` extends `MetaConstraintSolver`. It is responsible for converting `VariablePrototype`s into real fluents and repairing the scopes of cloned constraints.

The conversion is type-specific:

- `CompoundSymbolicValueConstraint` scopes receive compound symbolic variables.
- `AllenIntervalConstraint` scopes receive Allen intervals.
- `IntegerConstraint` scopes receive integer variables selected from nested prototypes.
- Other constraints receive concrete fluent substitutions.

Resolver retraction also restores CHIMP markings and resource state through Meta-CSP network and variable APIs.

### HTNMetaConstraint

`HTNMetaConstraint` extends `MetaConstraint` and represents open tasks as Meta-CSP meta-variables. Its values are `ConstraintNetwork` resolver candidates produced by either task unification or plan-repository expansion.

This binds CHIMP decomposition semantics to the Meta-CSP protocol of:

- `getMetaVariables()`
- `getMetaValues()`
- `MetaVariable`
- Resolver `ConstraintNetwork`s

### Plan Repository Expansion

`PlanReportroryItem.expand()` performs semantic matching and immediately constructs Meta-CSP resolver networks. A resolver can contain:

- New fluent `VariablePrototype`s.
- `FluentConstraint`s for preconditions, effects, decomposition, opening, closing, and application.
- `AllenIntervalConstraint`s for duration and additional temporal relations.
- `CompoundSymbolicValueConstraint`s for bindings and value restrictions.
- `IntegerConstraint`s and nested integer prototypes.
- Resource-usage constraints.

This method is the main convergence point for HTN, temporal, unification, integer, and resource semantics.

### Resource Constraints

`FluentScheduler` and `FluentResourceUsageScheduler` extend Meta-CSP `Schedulable`. Resource conflicts are represented as `MetaVariable`s and repaired with resolver networks containing Allen constraints.

Resource usage templates themselves are CHIMP concepts, but conflict detection and resolution are inherited from Meta-CSP. The classic domain parser also constructs scheduler instances directly, coupling parser output to the execution framework.

### Integer Constraints

The integer subsystem is implemented as a Meta-CSP solver family:

- `IntegerVariable extends Variable`
- `IntegerDomain extends Domain`
- `IntegerConstraint extends Constraint`
- `IntegerConstraintSolver extends ConstraintSolver`

HTN expansion creates integer constraints over concrete variables and nested `VariablePrototype`s. `HTNPlanner` must understand this special prototype representation during resolver materialization.

### Unification

The symbolic subsystem is similarly implemented as Meta-CSP variables, constraints, domains, and solvers. `CompoundSymbolicVariable` is a `MultiVariable`, and `CompoundSymbolicValueConstraint` compiles matching and restriction operations into lower-level name constraints.

HTN matching and expansion call directly into `CompoundSymbolicVariable` methods and create `CompoundSymbolicValueConstraint` instances. Unification is therefore both a semantic service and part of the Meta-CSP object graph.

### Domain Parsing

The parsers are not independent of the solver implementation.

`PlanReportroiryItemParser` and `ChimpClassicReader` construct:

- `AllenIntervalConstraint.Type`
- `AllenIntervalConstraint`
- `Bounds`
- Integer constraint templates based on `IntegerConstraint.Type`
- Resource scheduler objects

`ProblemParser` goes further by creating solver variables and adding concrete temporal, ordering, and resource constraints directly to `FluentNetworkSolver`.

Parser output therefore embeds executable solver objects instead of producing a neutral CHIMP domain model.

## Proposed Decoupling Order

### 1. Introduce Neutral Domain Value Types

Create CHIMP-owned immutable representations for:

- Temporal bounds.
- Allen relation names and parameters.
- Symbolic matches and restrictions.
- Integer expressions and comparisons.
- Resource requirements.

These types should contain no Meta-CSP imports and preserve all information required by the current implementation.

### 2. Decouple Domain Parsing

Change both parser implementations to produce neutral domain values. Move construction of `Bounds`, `AllenIntervalConstraint`, integer constraints, and resource schedulers into a dedicated Meta-CSP compilation layer.

This establishes a boundary without changing the solver or search algorithm.

### 3. Separate Semantic Expansion From Resolver Materialization

Split `PlanReportroryItem.expand()` into two phases:

1. Produce a CHIMP resolver description containing fluent prototypes and semantic constraints.
2. Compile that description into a Meta-CSP `ConstraintNetwork`.

Keep the existing Meta-CSP compiler initially so behavior remains unchanged.

### 4. Encapsulate Meta-Solving Data Structures

Introduce CHIMP abstractions for:

- Flaws.
- Resolver candidates.
- Fluent prototypes.
- Prototype substitutions.

Adapt these to `MetaVariable`, `ConstraintNetwork`, and `VariablePrototype`. This removes Meta-CSP types from HTN semantics before changing the search engine.

### 5. Introduce Fluent Access Interfaces

Add APIs such as `FluentView` and `FluentStore` for identity, component, marking, symbolic values, temporal state, integer values, and constraint queries.

Migrate HTN expansion, resources, dispatching, sensing, postprocessing, and UI to these APIs while retaining `Fluent` as the initial adapter implementation.

### 6. Hide Temporal Solver Types

Introduce a temporal service that exposes CHIMP-owned interval snapshots, bounds, and relation requests. Stop exposing `AllenInterval`, `AllenIntervalConstraint`, and `AllenIntervalNetworkSolver` outside the Meta-CSP adapter.

### 7. Hide Unification and Integer Solvers

Define ports for:

- Symbolic compatibility, matching, restrictions, and binding.
- Integer variable creation, arithmetic constraints, and consistency checks.

Wrap the current Meta-CSP-backed implementations first. Replace implementations only after callers no longer depend on their inheritance hierarchy.

### 8. Decouple Resource Scheduling

Move resource conflict detection and resolver generation into CHIMP-owned services. Keep temporary adapters that expose those conflicts as Meta-CSP meta-variables while `HTNPlanner` still uses `MetaConstraintSolver`.

### 9. Replace HTN Meta-CSP Inheritance

Only after resolver descriptions and solver services are isolated should `HTNMetaConstraint` and `HTNPlanner` stop extending `MetaConstraint` and `MetaConstraintSolver`.

This step must preserve:

- Search ordering.
- Resolver application.
- Retraction and rollback.
- Conflict-clause behavior.
- Marking transitions.
- Resource conflict integration.

It is the highest-risk migration step and should not be attempted first.

### 10. Migrate Peripheral Code

After the core APIs stabilize, migrate:

- Examples and evaluation programs.
- Sensing and dispatching.
- Postprocessing and plan extraction.
- UI and visualization.
- Test utilities.

These classes contain broad but mostly shallow coupling and should follow the new APIs rather than drive their design.

## Verification Strategy

Each stage should preserve existing planning results before removing the corresponding Meta-CSP adapter. Verification should include:

- Existing domain JUnit tests.
- Fluent network solver tests.
- Unification tests, including retraction.
- Integer planning examples.
- Resource and negative-effect examples.
- Esterel and HPR output tests.
- Representative DWR, RACE, and TransTerra plans.
- Comparisons of resolver ordering and final constraint networks where practical.

Add parser-focused tests as neutral domain values are introduced. These tests should assert semantic parser output without initializing Meta-CSP solvers.

## Current Boundary and Next Safe Change

Milestones 1 through 5 have established CHIMP-owned model, parser, resolver-description, and search-facing boundaries while retaining Meta-CSP as the active solver and search backend.

The independent model now includes temporal, symbolic, integer, resource, state-variable, problem, fluent-prototype, and resolver values. Both domain parser paths produce these values, and dedicated adapters under `chimp.adapter.metacsp` compile them into the existing Meta-CSP representation. The obsolete `CHIMPConnector` JNI integration and its dedicated tests have been removed.

Milestone 6 has begun. Read-oriented `FluentView` and `FluentStore` interfaces are defined in `chimp.model`, backed by `MetaCspFluentStore` over `FluentNetworkSolver`. `FluentView` carries id, type, signature, marking, temporal interval, and integer values; `FluentStore` provides fluent lookup, incoming/outgoing constraint queries by id and kind, and a read-only `temporalDistance(FluentId, FluentId, TemporalEndpoint, TemporalEndpoint)` query backed by the APSP solver. `PlanExtractor` is the first migrated consumer and now drives both ROS/YAML action output and hierarchy/debug output through `FluentStore`. `HPRGenerator` is the second migrated consumer: its fluent/constraint reads and temporal-edge extraction now go through `FluentStore` and `temporalDistance`, removing all `org.metacsp.*` imports from `HPRGenerator` and deleting the `HPRtypes.Duration` TimePoint wrapper. HPR YAML output is byte-identical to the legacy implementation (a deterministic fluent-id tiebreak replaces the previous HashMap-iteration order for fluents sharing EST and EET). The previously separate `PlanSnapshot` value type and `MetaCspModelAdapter.toPlanSnapshot` have been removed in favor of this single read boundary. `HPRGenerator` still keeps `htn.PlanReportroryItem` for operator/method argument-name lookup; that coupling belongs to the HTN layer and remains until a later milestone.

The Esterel export has been removed entirely as a non-core feature. `EsterelGenerator`, the `estereltypes` package (except the shared `KeyValue` type, moved into `postprocessing.HPRtypes`), the `--esterel`/`-e` CLI option, and `JUnitTestEsterelGeneration` were deleted. With this, the `postprocessing` package contains no `org.metacsp.*` imports at all; only `PlanExtractor` and `HPRGenerator` remain, both migrated to `FluentStore`. The temporal-service ("Hide Temporal Solver Types") milestone is no longer forced by a postprocessing consumer.

Repository expansion now first produces neutral `ResolverDescription` values. `MetaCspResolverAdapter` materializes them as resolver networks without changing resolver application or rollback. Search-facing `PlanningFlaw`, `ResolverCandidate`, and `ResolverCandidateMetadata` values are attached to Meta-CSP networks as specialized annotations. `HTNMetaConstraint` consumes the neutral flaw representation, and the existing value-ordering classes delegate to CHIMP-owned lexicographic resolver comparators. Legacy network inspection remains as a compatibility fallback.

The active runtime remains coupled at the solver-service level:

- `Fluent` and `FluentConstraint` are still Meta-CSP storage and constraint objects.
- `FluentNetworkSolver` still owns symbolic, temporal, and integer solver composition.
- HTN expansion and supporting services still query solver objects directly.
- Resource conflict resolution still uses Meta-CSP scheduling and meta-variables.
- `HTNMetaConstraint` and `HTNPlanner` still inherit Meta-CSP search classes and retain resolver application, retraction, and rollback responsibilities.

Milestone 6 should introduce CHIMP-owned fluent access and solver-service interfaces, beginning with read-oriented `FluentView` and `FluentStore` APIs. The initial implementation should be a Meta-CSP adapter over `FluentNetworkSolver`; it should not replace the solver or change planner behavior.

The first Milestone 6 changes should be small and ordered:

1. Define stable fluent identity, type, signature, marking, interval, and integer-value access through `FluentView`.
2. Define fluent lookup and constraint-query operations through `FluentStore` based on concrete current call sites.
3. Add a Meta-CSP-backed implementation and characterization tests for ordering and query semantics.
4. Migrate one narrow read-only consumer before expanding the interface or moving HTN expansion. `PlanExtractor` has been migrated, and the overlapping `PlanSnapshot` read path was consolidated into `FluentStore` to avoid maintaining two parallel read APIs.
5. Add the first interface addition driven by a concrete consumer: a read-only `temporalDistance` query over `TemporalEndpoint` pairs, backed by the APSP solver and characterized against raw APSP values in a real solved plan. This supports `HPRGenerator`'s temporal-edge extraction without exposing APSP types to callers.
6. Migrate `HPRGenerator`'s fluent and constraint reads to `FluentStore`/`FluentView`, and its temporal-edge extraction to `temporalDistance`. Drop the `HPRtypes.Duration` TimePoint wrapper. HPR YAML output is preserved byte-for-byte. The Esterel export (`EsterelGenerator`, `estereltypes`, the `--esterel` CLI flag, and its test) was removed as a non-core feature rather than migrated, leaving `postprocessing` free of `org.metacsp.*` imports and removing the last postprocessing consumer that would have forced the temporal-service milestone.

Temporal mutation, symbolic binding, integer propagation, resource scheduling, resolver application, prototype substitution, and rollback should remain on the existing backend during this milestone. Those capabilities belong behind later focused service interfaces before Meta-CSP inheritance is removed.

## Milestone 7 (proposed) — Complete the Temporal Boundary: Read Side Done, Write Side Next

### Where the read boundary leaves us

The Milestone 6 read work is effectively complete for real consumers. `FluentView` already exposes `TemporalInterval` (EST/LST/EET/LET), and `FluentStore.temporalDistance` covers the APSP read path. An audit of the remaining 72 main files importing `org.metacsp` shows that every remaining coupling is blocked, directly or transitively, by either the temporal **write/mutation** surface or by Meta-CSP **inheritance** in the core classes:

| Cluster | Files | Blocked by |
| --- | --- | --- |
| Core storage: `Fluent`, `FluentConstraint`, `FluentNetworkSolver` | 3 | M9 inheritance (they ARE Meta-CSP objects) |
| HTN core: `HTNPlanner`, `HTNMetaConstraint`, `PlanReportroryItem`, `HTNMethod`, `HTNOperator` | 5+ | M9 inheritance + resolver materialization |
| Unification | 10 | M7 ports + M9 inheritance |
| Integer | 4 | M7 ports + M9 inheritance |
| Resource scheduling | 3 | M8 + `Schedulable`/`MetaVariable` (M9) |
| Mutation consumers: `dispatching`, `sensing` | 6 | Temporal write/mutation port (this milestone) |
| Peripheral: `ui`, `examples`, `eval`, `dwr`, `externalPathPlanning`, `old_dev` | ~25 | Core types are still Meta-CSP (M10) |

There is no remaining shallow, speculative-free read migration: `ui.HierarchicalLayout` is a JUNG `Layout<Fluent, FluentConstraint>` typed on the core classes, and the `examples.*` temporal reads operate on `Variable[]` from the solver. Both are blocked until the core stops being Meta-CSP. So the next milestone is necessarily a step up in scope: it begins the **write/mutation** service boundary, which is the plan's prescribed item 6 ("Hide Temporal Solver Types") minus the read half already done.

### Decision to surface

Three directions are consistent with the plan. Per the AGENTS.md rule of not picking silently when interpretations differ, they are stated explicitly; the recommendation follows.

- **(A) Commit to the core decoupling chain** — complete the temporal write/mutation port (this proposal), then unification/integer ports, then resource, then inheritance removal (M9). Ends with CHIMP off Meta-CSP. Largest commitment; M9 is explicitly the highest-risk step in the plan.
- **(B) Pause Meta-CSP removal; pivot toward UP integration.** The long-term goal is UP integration. The existing read boundary (`FluentStore`/`FluentView`) may be enough to build a first UP bridge against a solved plan, which could be more valuable for a research project than finishing the removal. This defers the risky M9 work indefinitely.
- **(C) A bounded first wedge of (A): only the temporal write/mutation port, driven by the smallest mutation consumer (`dispatching`), then stop and reassess.** Lower commitment than full (A); produces a concrete, tested port without committing to M9.

**Recommendation: (C).** It is the plan's prescribed next milestone (the write half of "Hide Temporal Solver Types"), it has a forcing consumer so it is not speculative, it is bounded, and it leaves the option open for (A) or (B) at the next reassessment. (A) in one go is too large given the simplicity mandate; (B) is a direction change the user should choose explicitly rather than drift into.

### Scope for (C): Temporal Write/Mutation Service Boundary

Define a CHIMP-owned mutation port for the operations the non-core mutation consumers currently perform directly on `AllenIntervalConstraint`/`FluentNetworkSolver`. Concretely, from `dispatching.FluentDispatcher`:

- Add a temporal relation between two fluents (Allen `Type` + `Bounds`), e.g. `Overlaps`.
- Add a self deadline constraint (`Deadline` + `Bounds`).
- Add/remove constraints on the fluent network and observe success/failure (propagation result).
- Read EST/EET for bounds construction (already available via `FluentView`; reuse it).

Likely interface shape (to be confirmed against real call sites, not designed up front):

```java
// in chimp.model, alongside FluentStore
public interface FluentMutationStore extends FluentStore {
    boolean addTemporalRelation(FluentId from, FluentId to, TemporalRelation relation, TemporalBounds bounds);
    boolean addDeadline(FluentId fluent, TemporalBounds bounds);
    boolean removeConstraint(ChimpConstraint constraint);
}
```

`TemporalRelation` already exists in the independent model. The Meta-CSP adapter (`MetaCspFluentStore`) implements these by constructing `AllenIntervalConstraint` and calling `FluentNetworkSolver.addConstraint`, exactly as `FluentDispatcher` does today, so behavior is preserved.

In-scope files:
- `chimp.model.FluentMutationStore` (new interface) and any small value additions.
- `chimp.adapter.metacsp.MetaCspFluentStore` (implement the mutation methods).
- `dispatching.FluentDispatchingFunction` (40 lines, framework-only) and `dispatching.FluentDispatcher` (193 lines) — migrate to the port; drop their `org.metacsp.*` imports.
- Characterization test asserting dispatching behavior is unchanged.

Out of scope (explicitly deferred):
- `sensing.*` (marking transitions + `HTNMetaConstraint` coupling; migrate only if the port covers it trivially, otherwise leave).
- `HTNPlanner`/`HTNMetaConstraint` resolver application and rollback (M9).
- Unification, integer, resource ports (M7/M8).
- Any change to `Fluent`/`FluentConstraint`/`FluentNetworkSolver` inheritance (M9).

### Ordered steps

1. Enumerate the exact `AllenIntervalConstraint` constructions and `addConstraint`/`removeConstraint` calls in `FluentDispatcher` and `FluentDispatchingFunction`; confirm the minimal port method set. → verify: written list matches every `org.metacsp` usage in those two files.
2. Add `FluentMutationStore` to `chimp.model` and implement in `MetaCspFluentStore` by delegating to the existing Meta-CSP calls. → verify: compiles; adapter unit test for each method against a small solved plan.
3. Migrate `FluentDispatchingFunction` then `FluentDispatcher` to the port. → verify: no `org.metacsp.*` imports remain in `dispatching`; `./gradlew test` green; dispatching output/behavior byte-identical on a representative plan.
4. Reassess: decide (A) continue the core chain, or (B) pivot to UP integration.

### Verification

- `./gradlew test --rerun-tasks` green (full suite, including dispatching/sensing tests).
- `dispatching` package has zero `org.metacsp.*` imports.
- Diff dispatching behavior (constraint networks produced) before/after on a plan that exercises dispatching; expect identical.
- No new production dependencies.

### Risks

- The mutation port is a real abstraction (it has a forcing consumer), but getting the method shapes right matters: if `sensing` needs marking transitions the port doesn't cover, it stays on Meta-CSP and we accept a split boundary for now. Flagged, not forced.
- `FluentDispatcher` sorts fluents by EST and reads `AllenIntervalConstraint.Type.Equals`; the port must expose relation-type reads or reuse `ChimpConstraint`/`ConstraintKind`. Confirm in step 1.
- Behavior preservation depends on the adapter issuing the same `addConstraint` calls in the same order; verify by diff.
