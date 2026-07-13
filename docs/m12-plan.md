# M12: Complete Resolver Materialization — Implementation Plan

## Current State

### The Resolver Flow

```
PlanReportroryItem.expand()
  → describeResolvers()          → List<ResolverDescription>   (neutral, feasibility-checked)
  → RESOLVER_ADAPTER.compile()
      → item.materializeResolverDescriptions()  → List<ConstraintNetwork>  (LEGACY 200-line method)
      → validate by matching precondition IDs
      → annotate each network with ResolverCandidate
      → return List<ConstraintNetwork>

ChimpSearchEngine.addResolver(flaw, resolver)
  → HTNPlanner.addResolverSub(flaw, resolver)
      Phase 1: materialize VariablePrototypes → real Fluents
      Phase 2: repair constraint scopes (instanceof dispatch)
      Phase 3: set CLOSED markings
  → add constraints to ground solver
```

### The Core Coupling: `addResolverSub` Phase 2

```java
if (con instanceof CompoundSymbolicValueConstraint)       → fluent.getCompoundSymbolicVariable()
else if (con instanceof AllenIntervalConstraint)           → fluent.getAllenInterval()
else if (con instanceof IntegerConstraint)                 → fluent.getIntegerVariables()[index] (nested prototype)
else                                                       → fluent itself
```

This `instanceof` dispatch maps each Meta-CSP constraint type to the sub-variable
it operates on. It is the primary coupling point that M12 must eliminate.

### What `ResolverDescription` Already Carries

| Field | Content |
|---|---|
| `selectedPreconditions` | Map<preconditionKey, fluentId> — which open fluents satisfy which preconditions |
| `prototypes` | List<FluentPrototypeDescription> — effect/subtask prototypes (key, component, signature, int args, marking) |
| `symbolicConstraints` | bindings (EQUAL), differents (DIFFERENT), value restrictions (ALLOWED/FORBIDDEN) |
| `orderingEdges` | BEFORE ordering between subtasks |
| `temporalConstraints` | AllenInterval temporal relations |
| `integerConstraints` | integer arithmetic, ALL_EQUAL, constants |
| `resourceRequirements` | resource usage |
| `semanticKinds` | summary of what semantics are present |
| `bindingCount` | number of binding constraints |

### What `materializeResolverDescriptions()` Does (that descriptions don't capture)

The `ResolverDescription` is a **structural summary**, not a **blueprint**. The legacy
method has access to internal templates (`additionalConstraints`, `integerConstraintTemplates`,
`variableOccurrencesMap`, `subDifferentDefinitions`) and builds constraints by iterating
over them and wiring to actual Fluent objects. The descriptions capture the *what* but
not the *how-to-wire*.

Key gap: there is no explicit correspondence between `ResolverDescription` entries and
the `Constraint` objects in the compiled `ConstraintNetwork`. The constraints are created
in one order by `materializeResolverDescriptions()`, the description entries in another
order by `createResolverDescription()`, and nothing links them.

## The Problem

Two sub-goals from the plan:
1. **`compile()` truly compiles from `ResolverDescription`** — not just validate legacy output
2. **`addResolverSub` repairs scopes without `instanceof` dispatch** — using CHIMP-owned metadata

Both are large. The AGENTS.md mandates small, reviewable changes. This plan breaks M12
into incremental steps that each compile, keep tests green, and move one concept behind
a CHIMP-owned abstraction.

## Step-by-Step Plan

### Step 1: Introduce `SubVariableKind` — isolate the scope-repair dispatch

**Goal**: Move the `instanceof` dispatch into a single CHIMP-owned function.

- Create `chimp.model.SubVariableKind` enum: `FLUENT`, `SYMBOLIC`, `TEMPORAL`, `INTEGER`.
- Add `MetaCspResolverAdapter.subVariableKindOf(Constraint)` that does the `instanceof`
  dispatch and returns the `SubVariableKind`.
- Add `MetaCspResolverAdapter.extractSubVariable(Fluent, VariablePrototype, SubVariableKind)`
  that materializes the right sub-variable given the kind.
- Refactor `HTNPlanner.addResolverSub` to call these adapter methods instead of inline
  `instanceof`. The `IntegerConstraint` nested-prototype case is handled inside
  `extractSubVariable`.
- **Verify**: `./gradlew test` green. No behavior change — same dispatch, just relocated.
- **Value**: The coupling is now isolated in one adapter method, not spread across
  `addResolverSub`. `HTNPlanner` no longer imports `CompoundSymbolicValueConstraint`,
  `AllenIntervalConstraint`, or `IntegerConstraint`.

### Step 2: Tag constraints with `SubVariableKind` during materialization

**Goal**: `addResolverSub` reads CHIMP-owned tags instead of deriving kind from constraint type.

- In `materializeResolverDescriptions()`, build a `Map<Constraint, SubVariableKind>` as
  constraints are created. Each adapter call (`SYMBOLIC_ADAPTER.createSubMatches`,
  `TEMPORAL_ADAPTER.toAllenIntervalConstraint`, `INTEGER_ADAPTER.toIntegerConstraint`,
  `new FluentConstraint`, etc.) knows its kind — tag the result.
- Store the map on the `ConstraintNetwork` via the `annotation` field (public, separate
  from `specilizedAnnotation` which holds `ResolverCandidate`).
- Refactor `addResolverSub` to read the tag map. Fall back to `subVariableKindOf()` for
  untagged constraints (safety net).
- **Verify**: `./gradlew test` green. `addResolverSub` no longer uses `instanceof` on the
  primary path — it reads CHIMP-owned metadata.
- **Value**: `addResolverSub`'s scope repair is now driven by CHIMP-owned metadata, not
  Meta-CSP type dispatch. This is the "carry metadata alongside the compiled resolver"
  goal from the plan.

### Step 3: CHIMP-owned `ResolverNetwork` wrapper

**Goal**: Provide a CHIMP-owned container for the compiled resolver + metadata.

- Create `chimp.model.ResolverNetwork` that wraps:
  - The `ConstraintNetwork` (delegate)
  - The `Map<Constraint, SubVariableKind>` tag map
  - The `ResolverCandidate` / `ResolverDescription`
- `MetaCspResolverAdapter.compile()` returns `List<ResolverNetwork>` instead of
  `List<ConstraintNetwork>`.
- `ChimpSearchEngine` and `SearchHooks` are updated to accept `ResolverNetwork`.
- `addResolverSub` reads sub-variable kinds from `ResolverNetwork` directly.
- **Verify**: `./gradlew test` green.
- **Value**: The search engine and hooks no longer pass raw `ConstraintNetwork` — they
  pass a CHIMP-owned wrapper that carries metadata. This is a prerequisite for Step 4+
  because it gives `compile()` a place to attach description-derived metadata.

**Tradeoff**: This changes the `SearchHooks` interface signature, which touches
`ChimpSearchEngine`, `HTNPlanner`, `MetaCspFlawProviderAdapter`, and tests. It's a
wider change but sets up the compilation step. **Alternative**: keep
`ConstraintNetwork` as the carrier and use the `annotation` field (Step 2 approach).
This is less clean but avoids interface churn. I recommend the wrapper only if the
user prefers the cleaner long-term path; otherwise Step 2's annotation approach is
sufficient for now.

### Step 4: Compile PRE/CLOSES/UNARYAPPLIED from `ResolverDescription`

**Goal**: Begin replacing `materializeResolverDescriptions()` — start with the simplest
constraints.

- In `compile()`, for each `ResolverDescription`:
  - Look up the task fluent from the flaw network
  - Look up precondition fluents by ID from `selectedPreconditions`
  - Create PRE FluentConstraints (wired to open fluents)
  - Create CLOSES FluentConstraints for negative effects (operator + negative effect)
  - Create UNARYAPPLIED FluentConstraint
- Build the `ConstraintNetwork` from these, tag each with `SubVariableKind.FLUENT`.
- Compare the output to the legacy path for the same description — must match.
- **Verify**: New test asserting compiled network matches legacy for PRE/CLOSES/UNARYAPPLIED.
  `./gradlew test` green.
- **Value**: First concrete compilation from description. Proves the wiring approach works.

### Step 5: Compile effects/decomposition (VariablePrototypes) from `ResolverDescription`

**Goal**: Compile OPENS/DC constraints with VariablePrototypes from `prototypes`.

- For each `FluentPrototypeDescription`:
  - Create a `VariablePrototype` (component, name, arguments from signature)
  - Create OPENS (operator) or DC (method) FluentConstraint from task to prototype
  - Handle `createConnections` (argument binding between task and effect)
- For methods: compile ordering edges (BEFORE constraints) from `orderingEdges`.
  Also handle the "subtasks without successor inherit task's BEFORE constraints" logic.
- **Verify**: Compiled networks match legacy for simple operator and method domains.
  `./gradlew test` green.
- **Value**: VariablePrototype creation is now driven by descriptions. This is the
  hardest wiring step because it involves `createConnections` and BEFORE inheritance.

### Step 6: Compile symbolic constraints (bindings, differents, value restrictions)

**Goal**: Compile `CompoundSymbolicValueConstraint`s from `symbolicConstraints`.

- For each `SymbolicConstraintDescription`:
  - Resolve `ResolverArgumentReference` (fluentKey + argumentIndex) to the actual
    Variable (task fluent's CSV, precondition fluent's CSV, or effect prototype)
  - Create the appropriate constraint via `MetaCspSymbolicConstraintAdapter`
    (SubMatches for EQUAL, SubDifferent for DIFFERENT, value restrictions for
    ALLOWED/FORBIDDEN)
- Tag each with `SubVariableKind.SYMBOLIC`.
- **Verify**: Compiled networks match legacy. `./gradlew test` green.
- **Value**: Symbolic constraint creation is description-driven.

### Step 7: Compile temporal, integer, and resource constraints

**Goal**: Compile remaining constraint types from descriptions.

- Temporal: for each `ResolverTemporalConstraintDescription`, resolve from/to keys to
  AllenInterval variables, create AllenIntervalConstraint via adapter. Tag as TEMPORAL.
- Integer: for each `ResolverIntegerConstraintDescription`, resolve references to
  IntegerVariables (including nested prototypes), create IntegerConstraint via adapter.
  Tag as INTEGER.
- Resource: for each `ResourceRequirement`, create RESOURCEUSAGE FluentConstraint.
  Tag as FLUENT.
- Duration: from `temporalConstraints` entry with fromKey==toKey==HEAD.
- **Verify**: Compiled networks match legacy for all domains, especially integer
  planning (battery values). `./gradlew test` green.
- **Value**: All constraint types are now description-driven.

### Step 8: Switch over — `compile()` builds from descriptions, delete legacy path

**Goal**: `materializeResolverDescriptions()` is no longer called.

- `compile()` builds the full `ConstraintNetwork` from `ResolverDescription` values.
- Remove the validation-by-precondition-ID matching (no longer needed — we build
  directly, no legacy output to validate).
- Delete `materializeResolverDescriptions()` and its helper methods that are now
  unused (`createPreconditionsEffectsBindings`, `createSubDifferents`,
  `createValueRestrictions`, `setVarsInAdditionalConstraints`).
- Keep `describeResolvers()` and `createResolverDescription()` — they are the source
  of truth.
- **Verify**: `./gradlew test` green on all domains. Integer planning battery values
  unchanged. Resolver ordering unchanged. HPR generation byte-identical.
- **Value**: M12 complete. The resolver representation is compiled from CHIMP-owned
  descriptions. `addResolverSub` uses CHIMP-owned metadata for scope repair.

## Risk Assessment

| Step | Risk | Mitigation |
|---|---|---|
| 1 | Very low — pure refactor, same dispatch relocated | Tests unchanged |
| 2 | Low — tagging is additive, fallback ensures safety | Fallback to `subVariableKindOf()` |
| 3 | Medium — interface change touches search engine | Can be deferred; annotation approach works |
| 4 | Medium — first compilation step, wiring must be exact | Compare against legacy output |
| 5 | High — VariablePrototype creation + BEFORE inheritance is subtle | Extensive domain test comparison |
| 6 | Medium — symbolic constraint wiring via ResolverArgumentReference | Symbolic feasibility tests |
| 7 | Medium-high — integer constraint nested prototypes are tricky | Integer planning test is the canary |
| 8 | Medium — deletion of legacy path, ensure no orphan references | Full test suite + grep for dead code |

## Key Decisions to Surface

1. **Step 3 (ResolverNetwork wrapper) vs. annotation-only approach**: The wrapper is
   cleaner long-term but changes `SearchHooks` signatures. The annotation approach is
   less invasive. **Recommendation**: Start with annotation (Step 2), defer wrapper
   decision until Step 4 proves the compilation approach works.

2. **Step 4-7 ordering**: I propose constraint-type-by-constraint-type (simplest first).
   Alternative: do it domain-by-domain (compile all constraint types for one domain,
   then next). The type-by-type approach is more reviewable but requires partial
   compilation + legacy fallback during transition. **Recommendation**: type-by-type,
   with `compile()` using a hybrid approach (description-compiled constraints + legacy
   fallback for not-yet-converted types) until Step 8.

3. **`describeResolvers()` as single source of truth**: Currently both
   `describeResolvers()` and `materializeResolverDescriptions()` iterate preconditions
   and do feasibility checking independently. After M12, `describeResolvers()` is the
   only feasibility checker; `compile()` trusts its output. This means
   `describeResolvers()` must be provably complete — any feasibility check in the legacy
   path that's not in `describeResolvers()` would be lost. **Risk**: The
   `isSymbolicallyFeasible()` check in `describeResolvers()` mirrors the feasibility
   check in `materializeResolverDescriptions()`, but subtle differences could cause
   different candidate sets. **Mitigation**: Step 4's comparison test catches this.

4. **Hybrid compilation during transition (Steps 4-7)**: During the transition, `compile()`
   would build some constraints from descriptions and fall back to legacy for others.
   This is complex to implement correctly (the legacy path creates ALL constraints in
   one pass, intermixed). **Alternative**: Build the full network from descriptions in
   one shot (Step 4-7 combined), validate against legacy, then switch over. This is a
   bigger change but avoids the hybrid complexity. **Recommendation**: Present both
   options and let the user decide.

## Summary

- Steps 1-2 are low-risk refactors that isolate the coupling. They provide value
  independently and can be merged even if the rest of M12 is deferred.
- Steps 4-7 are the core compilation work, broken by constraint type.
- Step 8 completes the milestone by removing the legacy path.
- Each step compiles, keeps tests green, and moves one concept behind a CHIMP-owned
  abstraction.
