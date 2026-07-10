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

## Direction reassessment (post-M6)

After M6, the user redirected: **focus on the planner core, not peripheral services.** Dispatching/sensing are non-crucial and may be deleted rather than migrated. This makes the temporal write/mutation port (the earlier "M7 (C)" proposal) a non-goal: it existed only to serve `dispatching`, which is no longer a priority.

### Dispatching and sensing: cleanly removable, not blocking

An audit confirms `dispatching` and `sensing` are purely peripheral:
- Neither the planner (`planner/`, `htn/`, `fluentSolver/`) nor `CHIMP.java` references them.
- They are not JUnit tests; they are `main()` demo programs and are not wired into the gradle test suite.
- The only external reference is `examples/chimp/TestIncrementalMergingDispatching.java`, itself a `main()` demo.

They can be deleted wholesale (6 files, ~870 lines) with no impact on planning or tests. This is an optional cleanup, not a prerequisite for planner work. It is recorded here as approved-but-deferred; it can be done in a single small commit whenever it is useful to reduce noise.

## Milestone 7 (revised) — Hide Unification and Integer Solvers (planner-relevant service ports)

### Why M7, not the M9 search-loop wedge

After the post-M6 reassessment, the user chose to proceed with M7 rather than jumping to the M9 search-inheritance break. M7 is not a peripheral detour: its consumers are the planner core itself. `HTNPlanner.addResolverSub` type-dispatches on `CompoundSymbolicValueConstraint`, `AllenIntervalConstraint`, and `IntegerConstraint` to repair resolver scopes, and HTN expansion (`PlanReportroryItem`) calls directly into `CompoundSymbolicVariable` matching/restriction methods and constructs `IntegerConstraint`s. The plan's original ordering put M7 before M9 precisely so that two of the three `addResolverSub` type-dispatches disappear before the search loop is ported. Doing M9 first (the earlier proposal) would yield a CHIMP-owned search engine that still reaches into Meta-CSP storage via type-dispatch &mdash; an awkward intermediate state. M7-first avoids that.

M1&ndash;M5 already built the *description* layer (`IntegerConstraintKind`, `ResolverIntegerConstraintDescription`, `SymbolicRelationKind`, `SymbolicConstraintDescription`, `MetaCspIntegerConstraintAdapter`). What does not yet exist is the **runtime service port** &mdash; the live `CompoundSymbolicVariable`/`IntegerVariable` objects inside `Fluent`, manipulated during search. M7 is that runtime port.

M7 is split into two wedges:
- **M7-integer** (first): smaller, partly scaffolded, simpler semantics (arithmetic / all-different / all-equal).
- **M7-symbolic** (second): the CHIMP task-matching core (`possibleMatch`, `possibleArgumentsMatch`, `possibleArgumentDifferent`, predicate/argument reads and mutations). Most semantically delicate part outside the search engine.

### M7-integer: prerequisite behavioral contract (done)

There was no JUnit domain test exercising integer constraints in an end-to-end plan &mdash; `examples/chimp/TestPlanningWithIntegerConstraints` is a `main()` demo, not `@Test`. A behavioral contract is required before migrating the integer runtime. `src/test/java/htn/JUnitTestIntegerPlanning.java` was added: it plans over `domains/simple_integer_domain.ddl` (battery-level integer variable; `!move_base` requires `?batteryStart > 2` and sets `?batteryEnd = ?batteryStart - 2`, initial battery 100) and asserts a 7-action plan with two `!move_base` actions whose integer values are `(100, 98)` and `(98, 96)`. Those battery values are domain-defined semantics (initial 100, decrement 2), not implementation artifacts, so they are a robust, non-brittle contract. The test is non-vacuous (verified by temporarily flipping an assertion). This is the contract that must stay green across the M7-integer migration.

### M7-integer: progress and honest boundary

**Done:**
- [x] Behavioral contract: `JUnitTestIntegerPlanning` (7-action plan, battery `(100,98)` and `(98,96)`).
- [x] All `IntegerConstraint` construction in `PlanReportroryItem` now routes through `MetaCspIntegerConstraintAdapter` (`createAllEqualConstraint`, `createConstantConstraint`, plus the existing `toIntegerConstraint`). `PlanReportroryItem` has zero `integers.*` imports.
- [x] All `CompoundSymbolicValueConstraint` construction in `PlanReportroryItem` now routes through `MetaCspSymbolicConstraintAdapter` (`createPositiveValueRestriction`, `createNegativeValueRestriction`, `createSubDifferent`, `createSubMatches`). `PlanReportroryItem` no longer imports `unify.CompoundSymbolicValueConstraint`. The remaining `unify` import (`CompoundSymbolicVariable`) stays — it is used for live variable reads (`possibleArgumentDifferent`) that are M9-coupled.

**What remains and where it blocks:**

The remaining integer-runtime couplings are all blocked by the Meta-CSP storage/resolver-network layer (M9), not by a missing port:

- `HTNPlanner.addResolverSub` does `con instanceof IntegerConstraint` and repairs scope via `fl.getIntegerVariables()[intVarIndex]`. This operates on the raw Meta-CSP `ConstraintNetwork` resolver values; the type-dispatch cannot be hidden until the resolver network representation is neutralized (M9 / resolver materialization).
- `Fluent.getIntegerVariables()` returns `IntegerVariable[]` by slicing `getInternalVariables()`. The elements are used as `Constraint` scope elements inside Meta-CSP `ConstraintNetwork`s, so the return type cannot be neutralized until `Fluent` stops being a Meta-CSP `MultiVariable` (M9 storage).
- `FluentNetworkSolver` composes `IntegerConstraintSolver` as a Meta-CSP ingredient. It IS a Meta-CSP `MultiConstraintSolver`; this is the storage layer (M9).

So M7-integer has reached its natural boundary: **construction is isolated behind the adapter, but the live `IntegerVariable` objects and the `instanceof` dispatch remain because they are woven into the Meta-CSP resolver-network representation.** Further integer isolation is an M9 task, not an M7 task.

The plan's original sequencing (M7 before M9) is validated: when M9 neutralizes the resolver network, `addResolverSub`'s integer dispatch disappears naturally because the resolver description already carries `ResolverIntegerConstraintDescription` values and the adapter already compiles them.

### Next: M9 or reassess

Both M7 wedges (integer and symbolic) have now hit the same M9 wall: construction is isolated behind adapters, but live variable reads (`Fluent.getIntegerVariables()`, `CompoundSymbolicVariable.possibleArgumentDifferent()`, `getCompoundSymbolicVariable().getPredicateName()`, etc.) and the `addResolverSub` type-dispatches (`instanceof IntegerConstraint`, `instanceof CompoundSymbolicValueConstraint`, `instanceof AllenIntervalConstraint`) remain because they operate on the raw Meta-CSP `ConstraintNetwork` resolver representation.

The honest assessment is that **M7 construction isolation is complete** — all constraint construction in `PlanReportroryItem` now goes through `chimp.adapter.metacsp` adapters. What remains is M9: neutralize the resolver-network representation so the `addResolverSub` type-dispatches and live variable reads fall away. The `ResolverDescription` values (M3) and the adapter factory methods (M7) are already in place to make that possible.

### Verification (M7-integer)

- `./gradlew test --rerun-tasks` green.
- `JUnitTestIntegerPlanning`: 7 actions, `!move_base` `(100, 98)` and `(98, 96)` unchanged.
- No new production dependencies.

### Risks (M7-integer)

- The integer runtime port must carry the variable-identity information that `addResolverSub` needs (nested `VariablePrototype` &rarr; `Fluent.getIntegerVariables()[index]`). Confirm the port can express this without leaking `IntegerVariable`; if it cannot, keep that one dispatch on Meta-CSP for now and accept a partial boundary.
- `IntegerConstraintSolver` may propagate via Choco or similar internals; the port wraps, not replaces, so this is not a risk for this wedge.

### Deferred

- M7-symbolic (the matching/restriction computation port) &mdash; after M7-integer.
- M8 resource scheduling, M9 search-inheritance break, M10 peripheral &mdash; after M7.
- Dispatching/sensing removal &mdash; approved-but-deferred cleanup, independent of M7.

### What "focus on the planner" means concretely

The planner core is `HTNPlanner extends MetaConstraintSolver` and `HTNMetaConstraint extends MetaConstraint`. The CHIMP-specific search behavior lives in overridden hooks (`addResolverSub`, `retractResolverSub`, `preBacktrack`, `postBacktrack`, bounds, conflict clauses), but the **search engine itself** — backtracking, flaw enumeration, resolver application/retraction orchestration, rollback — is inherited from `MetaConstraintSolver` (755 lines). Removing this inheritance is the plan's item 9, explicitly flagged as the highest-risk step.

This is the only real planner work left. There is no shallow planner migration: every remaining `org.metacsp` coupling in the planner is either the inheritance itself or the `ConstraintNetwork`/`VariablePrototype`/`Constraint` types that flow through the inherited search protocol.

This inheritance break (M9) remains the eventual planner goal, but it is now sequenced **after** M7 per the plan's original ordering, so that `addResolverSub`'s type-dispatches on `CompoundSymbolicValueConstraint` and `IntegerConstraint` are already gone when the search loop is ported. The grounded scope reading below is retained for when M9 is reached.

### Grounded scope (from reading `MetaConstraintSolver` + `HTNPlanner`)

The inherited search engine decomposes into:

| Piece | Size | CHIMP-owned? | Coupling |
| --- | --- | --- | --- |
| `backtrack()` + `backtrackHelper()` — the recursive backtracking loop | ~70 lines | No (inherited) | `MetaVariable`, `ConstraintNetwork`, `valOH` |
| `getConflict()` — enumerate flaws from meta-constraints | ~10 lines | No (inherited) | `MetaConstraint.getMetaVariable()` |
| `addResolver()` / `retractResolver()` — distribute resolver constraints to ground solvers, propagate, rollback on failure | ~50 lines | No (inherited) | `Constraint`, `ConstraintSolver.addConstraints/removeConstraints` |
| `addResolverSub()` / `retractResolverSub()` — CHIMP prototype→Fluent materialization, scope repair, marking | ~75 + ~20 lines | **Yes** (already overridden in `HTNPlanner`) | `VariablePrototype`, `ConstraintNetwork` substitution, `FluentConstraint.Type`, `Fluent` |
| Bounds, conflict clauses, pre/post backtrack | small | **Yes** (already overridden) | minimal |
| Meta-constraint list management, ground-solver access, resolver tracking maps, drawing, serialization backup | rest of 755 lines | No (inherited) | framework |

The encouraging finding: the **CHIMP-specific logic is already owned** (the `@Override` hooks). What is not owned is the generic backtracking orchestration, which is a standard recursive search (~130 lines of actual algorithm across `backtrack`/`backtrackHelper`/`addResolver`/`retractResolver`/`getConflict`).

### The hard part, stated honestly

`HTNPlanner.addResolverSub` is deeply coupled to Meta-CSP: it materializes `VariablePrototype`s into `Fluent`s via `groundSolver.createVariable`, repairs constraint scopes by type-dispatching on `CompoundSymbolicValueConstraint`/`AllenIntervalConstraint`/`IntegerConstraint`, and sets markings via `FluentConstraint.Type.CLOSES`. This is the resolver-materialization path that Milestone 3 (`ResolverDescription` → `MetaCspResolverAdapter`) began to separate, but `addResolverSub` still operates on raw `ConstraintNetwork` resolver values.

The plan's original ordering puts M9 **after** solver-service isolation (M6 read done; M7 unification/integer, M8 resource not started). Jumping to M9 now skips that sequencing. The user has explicitly prioritized the planner over peripheral services, which is a legitimate call for a research project whose core is the planner — but it means M9 will touch the resolver-materialization path while it still uses Meta-CSP storage types. The first wedge keeps that storage underneath; only the search *loop* becomes CHIMP-owned.

### Proposed wedge: own the search loop (first step of M9)

Introduce a CHIMP-owned `ChimpSearchEngine` that owns `backtrack()` / `backtrackHelper()` / `getConflict()` / `addResolver()` / `retractResolver()`, ported from `MetaConstraintSolver` but operating against CHIMP-defined interfaces (a `FlawProvider`/`ResolverSource` port realized initially by `HTNMetaConstraint`, and the existing `FluentNetworkSolver` for ground-solver propagation). `HTNPlanner` stops extending `MetaConstraintSolver` and instead holds and delegates to `ChimpSearchEngine`.

Still Meta-CSP underneath: the resolver networks remain `ConstraintNetwork`, `addResolver` still calls `FluentNetworkSolver.addConstraints`, and `addResolverSub` keeps its current `VariablePrototype` materialization. The headline goal — `HTNPlanner` no longer `extends MetaConstraintSolver` — is achieved without yet replacing the storage layer.

This is intentionally the **first step** of M9, not all of M9. It is bounded: one new class + rewiring `HTNPlanner` delegation + a `FlawProvider` port for `HTNMetaConstraint`. Planning results must be identical on all existing domain tests.

### Ordered steps

1. **Enumerate the exact `MetaConstraintSolver` surface `HTNPlanner` and `CHIMP.java` rely on** — every `protected`/`public` method called or overridden, every field accessed. → verify: written list; every usage in `HTNPlanner.java` and `CHIMP.java` accounted for; nothing hidden.
2. **Define `ChimpSearchEngine` + a minimal `FlawProvider`/`ResolverSource` port** in `chimp.search`, porting `backtrack`/`backtrackHelper`/`getConflict`/`addResolver`/`retractResolver`. Keep `ConstraintNetwork`/`Constraint` as the internal resolver representation for now. → verify: compiles in isolation; unit test with a stub flaw provider exercising the loop (find flaw → try resolvers → backtrack → succeed).
3. **Repoint `HTNPlanner` to delegate to `ChimpSearchEngine`** instead of extending `MetaConstraintSolver`; move the `addResolverSub`/`retractResolverSub`/bounds/conflict hooks into delegates. `HTNMetaConstraint` implements the new port instead of extending `MetaConstraint`. → verify: `./gradlew test --rerun-tasks` green on all domain tests (DWR, RACE, TransTerra, mobipick, integer, resource, negative-precondition); resolver ordering and final plans identical.
4. **Reassess.** At this point `HTNPlanner`/`HTNMetaConstraint` no longer inherit Meta-CSP search classes. Remaining Meta-CSP coupling is the storage layer (`Fluent`/`FluentConstraint`/`FluentNetworkSolver`) and `ConstraintNetwork`-based resolver materialization. Decide whether to continue down the storage layer (the rest of M9) or pause.

### Step 3 result: search loop delegated to ChimpSearchEngine (done)

`HTNPlanner` now overrides `backtrack()` to create a `ChimpSearchEngine` that drives the search loop through the CHIMP-owned `FlawProvider`/`SearchHooks` ports. All meta-constraints (HTN, resource schedulers, guess-ordering, move-base) are wrapped as `FlawProvider`s via `MetaCspFlawProviderAdapter`. `HTNPlanner` implements `SearchHooks`, so the engine calls its existing `addResolverSub`/`retractResolverSub` for prototype&rarr;Fluent materialization and marking restoration. `draw()` delegates to the engine's search-tree visualization.

**Honest assessment &mdash; the inheritance is not yet broken.** `HTNPlanner` still `extends MetaConstraintSolver` because the resource schedulers (`FluentResourceUsageScheduler extends Schedulable extends MetaConstraint`) need `setMetaSolver(MetaConstraintSolver)` to access the ground solver via `this.metaCS.getConstraintSolvers()[0]`. Breaking the inheritance now would require either a `MetaConstraintSolver` shim (complexity) or also decoupling all resource schedulers (too much for one step). The pragmatic choice: the search *algorithm* is now CHIMP-owned and tested; the *plumbing* (`addMetaConstraint`, `getConstraintSolvers`, `setMetaSolver`) remains Meta-CSP. The full inheritance break is deferred until resource schedulers are decoupled (M8) or the storage layer is replaced.

`HTNMetaConstraint` still `extends MetaConstraint` &mdash; it is wrapped by `MetaCspFlawProviderAdapter` rather than implementing `FlawProvider` directly. This avoids touching `HTNMetaConstraint`'s `getGroundSolver()` (which uses `this.metaCS`) and the `Schedulable` inheritance chain. Making `HTNMetaConstraint` implement `FlawProvider` directly is a follow-up that depends on giving it an independent ground-solver reference.

Full test suite green: DWR, RACE, TransTerra, mobipick, integer planning (battery `(100,98)`/`(98,96)`), resource, HPR generation, characterization &mdash; all pass with identical planning results.

### Step 1 result: `MetaConstraintSolver` surface inventory (done)

The full inheritance chain is `HTNPlanner extends MetaConstraintSolver extends MultiConstraintSolver extends ConstraintSolver`.

#### A. `HTNPlanner` overrides (11 methods — all CHIMP-owned hooks, already `@Override`)

| # | Method | Abstract in parent? | HTNPlanner body |
| --- | --- | --- | --- |
| 1 | `preBacktrack()` | yes | empty |
| 2 | `postBacktrack(MetaVariable)` | yes | empty |
| 3 | `retractResolverSub(ConstraintNetwork, ConstraintNetwork)` | yes | CHIMP marking restoration + variable removal |
| 4 | `addResolverSub(ConstraintNetwork, ConstraintNetwork)` | yes | prototype→Fluent materialization + scope repair |
| 5 | `getUpperBound()` | yes | returns `Double.POSITIVE_INFINITY` |
| 6 | `setUpperBound()` | yes | empty |
| 7 | `getLowerBound()` | yes | returns `0` |
| 8 | `setLowerBound()` | yes | empty |
| 9 | `hasConflictClause(ConstraintNetwork)` | yes | returns `false` |
| 10 | `resetFalseClause()` | yes | empty |
| 11 | `getFluentNetworkSolver()` | no — from `HybridDomainPlanner` interface | returns `groundSolver` |

Key finding: 7 of the 11 overrides are trivial (empty/constant-return). Only `addResolverSub` and `retractResolverSub` carry real logic.

#### B. `HTNPlanner` constructor → `super` call

`super(new Class[]{FluentConstraint.class}, animationTime, new FluentNetworkSolver(...))` → `MetaConstraintSolver(Class<?>[], long, ConstraintSolver...)`. The constructor passes the constraint type array, animation time, and the ground solver. HTNPlanner then stores `getConstraintSolvers()[0]` as its own `groundSolver` field.

#### C. Parent-chain methods called by `HTNPlanner` body

Only one: `this.getConstraintSolvers()[0]` (from `MetaConstraintSolver`, which returns `this.constraintSolvers`). **No `super.` calls at all. No direct field access from any parent class.** HTNPlanner's own fields: `groundSolver`, `typesInstancesMap`, `logger`, `serialVersionUID`.

#### D. Parent-chain methods called by `CHIMP.java` on the `planner` instance

| Call | Source | Purpose |
| --- | --- | --- |
| `planner.getConstraintSolvers()[0]` | `MetaConstraintSolver` | gets `FluentNetworkSolver` |
| `planner.backtrack()` | `MetaConstraintSolver` | the search entry point |
| `planner.addMetaConstraint(...)` ×5 | `MetaConstraintSolver` | registers HTN/resource/guess/move-base meta-constraints |
| `planner.draw()` | `MetaConstraintSolver` | visualization (called from `drawSearchSpace`) |
| `planner.getDescription()` | `MetaConstraintSolver` (overrides `MultiConstraintSolver`) | string description (called from `printDescription`) |
| `planner.setTypesInstancesMap(...)` | `HTNPlanner` own | not from parent |

#### E. Other consumers of `HTNPlanner` (via `CHIMP.getPlanner()`)

| Consumer | Call | Source |
| --- | --- | --- |
| `HPRGenerator` | `getHTNMetaConstraint().getOperators()/getMethods()` | `HTNPlanner` own |
| `ChimpIO` | `getPlanner().draw()` | `MetaConstraintSolver` |

All example/eval `main()` demos also call `planner.backtrack()` and `planner.addMetaConstraint(...)`. These are the same two methods CHIMP uses.

#### F. The search loop internals (what `ChimpSearchEngine` must own)

| Method | Lines | Calls |
| --- | --- | --- |
| `backtrack()` | ~20 | `getConflict()`, `backtrackHelper()` |
| `backtrackHelper()` | ~75 | `getMetaValues()`, `addResolver()`, `getConflict()`, `retractResolver()`, `markResolvedSub()`, `preBacktrack()`, `postBacktrack()`, JUNG `g.addEdge/addVertex` |
| `getConflict()` | ~10 | iterates `metaConstraints`, calls `MetaConstraint.getMetaVariable()` |
| `addResolver()` | ~25 | `addResolverSub()`, `ConstraintSolver.addConstraints()`, rollback on failure |
| `retractResolver()` | ~20 | `ConstraintSolver.removeConstraints()`, `retractResolverSub()` |

Total: ~150 lines of algorithm to port.

#### G. The `MetaConstraint`/`MetaVariable` coupling (the port boundary)

The search loop uses these from `MetaConstraint`:
- `getMetaVariable()` → returns `ConstraintNetwork` (the current flaw)
- `getMetaValues(MetaVariable)` → returns `ConstraintNetwork[]` (resolver candidates)
- `markResolvedSub(MetaVariable, ConstraintNetwork)` → notification (no-op in HTNMetaConstraint)
- `valOH` field → `ValueOrderingH` for resolver ordering (already neutralized by M5's lexicographic comparator)

`MetaVariable` wraps `(MetaConstraint, ConstraintNetwork)` and provides `getConstraintNetwork()`, `getMetaConstraint()`. It is a thin wrapper — the port can replace it with a CHIMP-owned `Flaw` value carrying the same two references.

The `FlawProvider`/`ResolverSource` port needs exactly: get-current-flaw, get-resolvers-for-flaw, notify-resolved, value-ordering. That's 4 methods.

#### H. What can be omitted (confirmed unused — no caller in CHIMP, examples, eval, or tests)

| Method | Status |
| --- | --- |
| `branchAndBound()` | **unused** — no caller anywhere; omit from port |
| `backtrackHelperWithSerialization()` | **unused** — no caller; omit |
| `backupCNs()` / `restoreCNs()` | only used by serialization variant; omit |
| `getAddedResolvers()` / `retractResolvers()` / `clearResolvers()` | **unused**; omit |
| `breakSearch()` | **unused**; omit |
| `propagate()` | **unused**; omit |
| `getMetaConstraints()` | **unused** by external callers (the field is used internally by the search loop) |

#### I. What must be re-hosted from the parent chain on `HTNPlanner`/`ChimpSearchEngine`

| Method | Why |
| --- | --- |
| `getConstraintSolvers()` | already effectively re-hosted — HTNPlanner stores `groundSolver` directly |
| `addMetaConstraint(MetaConstraint)` | must re-host: adds to a list + sets meta-constraint's solver reference + extends constraint type array |
| `draw()` | preserved — delegates to `ChimpSearchEngine`'s search-tree visualization |
| `getDescription()` | can be simplified |
| `backtrack()` | becomes `ChimpSearchEngine.backtrack()` |
| `getConflict()` / `addResolver()` / `retractResolver()` / `backtrackHelper()` | become `ChimpSearchEngine` internals |

#### Summary

The surface is **smaller than feared**. The real port is:
- `ChimpSearchEngine`: ~150 lines of search algorithm (the 5 methods in F), re-hosted from `MetaConstraintSolver`.
- `FlawProvider`/`ResolverSource` port: 4 methods (G).
- `HTNPlanner` re-hosts: `addMetaConstraint` (trivial), `draw` (preserved — search-tree visualization), `getDescription` (simplified), `backtrack` (delegates to engine).
- The 11 override hooks (A) move to delegates or stay as CHIMP-owned callbacks.
- 7 framework methods confirmed unused (H) — omitted per simplicity mandate.

The `addResolverSub` hard coupling (prototype→Fluent materialization, type-dispatch on `CompoundSymbolicValueConstraint`/`AllenIntervalConstraint`/`IntegerConstraint`) remains in place for this wedge — it stays as a CHIMP-owned callback that the engine calls, still operating on `ConstraintNetwork` resolver values. That's the M9 storage-layer work that follows this wedge.

The JUNG `DelegateForest<MetaVariable, ConstraintNetwork>` search-tree visualization (`g`) is built by `backtrackHelper()` and exposed via `draw()`. It is actively used for debugging/inspection, so it must be preserved in the port: `ChimpSearchEngine` keeps the JUNG graph building and a working `draw()` method. The JUNG dependency stays in the search engine. The `MetaVariable`/`ConstraintNetwork` vertex/edge types can remain for this wedge since the resolver representation is still Meta-CSP.

### Verification

- `./gradlew test --rerun-tasks` green on the full suite (the domain JUnit tests are the behavioral contract for the search engine).
- `HTNPlanner` no longer `extends MetaConstraintSolver`; `HTNMetaConstraint` no longer `extends MetaConstraint`.
- No new production dependencies.
- Planning results (plan found/not-found, action sequence, resolver counts where checked) unchanged on representative DWR/RACE/TransTerra/mobipick plans.

### Risks

- **Highest-risk item by the plan's own designation.** The search engine owns backtracking, rollback, and conflict-clause behavior. A faithful port must reproduce `MetaConstraintSolver`'s exact resolver application/retraction semantics, including the rollback-on-failure inside `addResolver` and the `markResolvedSub` calls. Mitigation: the existing domain tests are a strong behavioral contract; porting is mechanical (copy + retype), not a redesign.
- `MetaConstraintSolver extends MultiConstraintSolver extends ConstraintSolver`. Breaking the inheritance means `HTNPlanner` loses framework-provided ground-solver management (`getConstraintSolvers()`, `addMetaConstraint()`, etc.). Some of this must be re-hosted on `HTNPlanner`/`ChimpSearchEngine` directly. Step 1 surfaces the full surface so there are no surprises.
- `HTNMetaConstraint.getMetaValues()` returns `ConstraintNetwork[]` and uses `valOH` ordering; the `FlawProvider`/`ResolverSource` port must carry the value-ordering hook. The M5 lexicographic comparator work already neutralized the `valOH` types, so this should compose cleanly — confirm in step 2.
- Branch-and-bound (`branchAndBound()`) and the serialization-based `backtrackHelperWithSerialization` exist in `MetaConstraintSolver`. CHIMP uses `backtrack()` (confirmed: `CHIMP.java` calls `planner.backtrack()`). Verify no consumer uses branch-and-bound or serialization backup; if unused, omit them from the port (simplicity mandate).
