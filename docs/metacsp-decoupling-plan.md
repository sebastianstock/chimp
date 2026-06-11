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

The first Meta-CSP-independent value layer lives in `chimp.model`. It is not yet integrated into the planner.

| Independent model | Current representation | Initial mapping |
| --- | --- | --- |
| `FluentId` | `Fluent.getID()` | Stable non-negative fluent identity. |
| `FluentType` | `Fluent.getTypeStr()` and component strings | Maps `Activity`, `State`, `PlannedState`, and `Task` to explicit enum values. |
| `FluentSignature` | `CompoundSymbolicVariable` through `Fluent.getName()` | Predicate plus an immutable ordered argument list. It does not model symbolic domains or unresolved alternatives yet. |
| `TemporalBounds` | `org.metacsp.time.Bounds` | Immutable minimum and maximum range for duration or relation parameters. |
| `TemporalInterval` | `Fluent.getAllenInterval()` | Immutable snapshot of EST, LST, EET, and LET. |
| `FluentNode` | `Fluent` | Immutable semantic snapshot composed from ID, type, signature, and temporal interval. It intentionally excludes solver references, markings, and integer-variable domains. |
| `ConstraintKind` | `FluentConstraint.Type` | Uses domain names for the current relation set: `DC` becomes `DECOMPOSITION`, `PRE` becomes `PRECONDITION`, `UNARYAPPLIED` becomes `APPLIED`, and `MOVEDURATION` becomes `MOVE_DURATION`. |
| `ChimpConstraint` | `FluentConstraint` | Immutable kind and endpoint IDs, with optional temporal bounds for bounded relations. Planner-specific payloads such as repository items, resource templates, axiom text, connection arrays, depth, and additional Allen constraints remain outside this initial model. |

`chimp.adapter.metacsp.MetaCspModelAdapter` provides the initial one-way boundary. It reads a grounded Meta-CSP `Fluent` into a `FluentNode` and maps a `FluentConstraint` into a `ChimpConstraint`. Duration bounds are copied when present. The adapter fails explicitly for unknown fluent components, non-ground signatures, and non-fluent constraint endpoints. Conversion remains outside the independent model package.

## Import Inventory

There are 97 Java files importing `org.metacsp.*`: 85 under `src/main/java` and 12 under `src/test/java`.

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

The initial value layer and one-way Meta-CSP adapter are now established. `TemporalBounds` represents a minimum/maximum range, `TemporalInterval` represents a solved interval snapshot, and `MetaCspModelAdapter` converts grounded `Fluent` and `FluentConstraint` instances without changing planner behavior.

The obsolete `CHIMPConnector` JNI integration and its dedicated tests have been removed. Future integration with other languages should use a new boundary designed for that environment rather than preserve the legacy JNI transfer structs.

`PlanSnapshot` now provides an immutable grounded Activity/Task graph with ID, type, and constraint queries. `MetaCspModelAdapter` creates it from the solved fluent network, and `PlanExtractor` uses it for ROS and YAML action output. Hierarchy/debug output remains on the legacy solver path because it depends on markings and raw fluent rendering.

The next safe integration is to introduce CHIMP-owned temporal relation and constraint-template values for parser output, then compile those values to Meta-CSP constraints in the adapter layer. HPR and Esterel remain later milestones because they require temporal-distance and repository metadata not represented by the current model.

`Fluent`, `FluentConstraint`, `FluentNetworkSolver`, `HTNPlanner`, and `HTNMetaConstraint` should otherwise remain unchanged until neutral parser definitions and resolver descriptions exist.
