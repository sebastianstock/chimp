# M15 Final Review — Meta-CSP Removal (Atomic Swap)

**Date**: 2025
**Scope**: Phases 1–3 of the revised M15 plan (ground-solver swap, storage layer deletion, example preservation)
**Files touched**: 117

---

## Structure of the Changes

### Deleted Packages (Storage Layer + Adapters)

| Package | Files | Contents |
|---------|-------|----------|
| `fluentSolver/` | 4 | Old Meta-CSP fluent storage (`Fluent`, `FluentConstraint`, `FluentNetworkSolver`, `TestFluentNetworkSolver`) |
| `unify/` | 9 | Old Meta-CSP symbolic constraints |
| `integers/` | 6 | Old Meta-CSP integer constraints |
| `chimp/adapter/metacsp/` | 12 | Adapter layer (FluentStore, ModelAdapter, ProblemCompiler, etc.) |
| Legacy `Test*` mains | 5 | Standalone Meta-CSP-coupled examples |
| Adapter/crossval/perf tests | 14 | Tests that depended on Meta-CSP types |

### New Files (CHIMP-Native Replacements)

| File | Lines | Purpose |
|------|------:|---------|
| `chimp/kernel/fluent/FluentStoreImpl.java` | ~180 | Native `FluentStore` implementation |
| `chimp/kernel/fluent/ModelAdapter.java` | 79 | Fluent → FluentView conversion |
| `chimp/kernel/fluent/ProblemCompiler.java` | 139 | Problem definition → solver |
| `chimp/kernel/fluent/ResourceCapacityAdapter.java` | 19 | Resource capacity support |
| `chimp/kernel/fluent/ResourceRequirementAdapter.java` | 34 | Resource requirement support |
| `chimp/kernel/fluent/StateVariableAdapter.java` | 24 | State variable support |
| `chimp/search/ChimpResolverAdapter.java` | ~85 | Resolver compilation + scope repair |
| `chimp/search/ChimpSearchAdapter.java` | ~120 | Flaw/resolver annotation |
| `chimp/search/MCSData.java` | ~30 | Minimal critical set data |
| `chimp/util/PowerSet.java` | ~35 | Power set utility |

### Migrated Files (Meta-CSP → CHIMP-Native Types)

**Kernel layer:**

| File | Changes |
|------|---------|
| `chimp/kernel/Constraint.java` | Added `setFrom()`/`setTo()` for scope mutation |
| `chimp/kernel/ConstraintNetwork.java` | Auto-registers scope vars on `addConstraint()`; `getConstraints(Variable, Variable)`; specialized annotation |
| `chimp/kernel/ConstraintSolver.java` | `getConstraintNetwork()` base method |
| `chimp/kernel/VariablePrototype.java` | Auto-id factory methods via `AtomicInteger` |
| `chimp/kernel/fluent/FluentNetworkSolver.java` | Live network, `addConstraints` loop, `createVariable()`, `removeVariables()` |
| `chimp/kernel/temporal/TemporalVariable.java` | Records implicit duration edge for STN rebuild |
| `chimp/kernel/temporal/TemporalConstraint.java` | Unwraps `Fluent` to `TemporalVariable` |
| `chimp/kernel/symbolic/CompoundSymbolicSolver.java` | `addConstraints` loop |

**Search layer:**

| File | Changes |
|------|---------|
| `chimp/search/ChimpSchedulable.java` | `Activity` → `Fluent` throughout; added `FluentActivityComparator` |
| `chimp/search/ChimpMetaConstraint.java` | `org.metacsp` → `chimp.kernel` imports |
| `chimp/search/ChimpResolver.java` | Delegate now CHIMP-native `ConstraintNetwork` |
| `chimp/search/ChimpSearchEngine.java` | `org.metacsp` → `chimp.kernel` imports |
| `htn/valOrderingHeuristics/CHIMPValOH.java` | Now `Comparator<ConstraintNetwork>` directly |

**HTN planner layer:**

| File | Changes |
|------|---------|
| `htn/HTNPlanner.java` | Uses `FluentStoreImpl`, `ChimpResolverAdapter`, `FluentMarking` |
| `htn/HTNMetaConstraint.java` | `AllenInterval` → `TemporalVariable`; `ValueOrderingH` → `Comparator<ConstraintNetwork>` |
| `htn/HTNMethod.java` | `FluentMarking` ordinals for markings |
| `htn/HTNOperator.java` | `FluentMarking` ordinals for markings |
| `htn/PlanReportroryItem.java` | All constraint creation uses CHIMP-native types |
| `htn/EffectTemplate.java` | `VariablePrototype.forFluent()` factory |
| `htn/GroundSolverHolder.java` | CHIMP-native solver types |
| `htn/OrderingConstraintTemplate.java` | CHIMP-native fluent constraint |
| `htn/guessOrdering/GuessOrderingMetaConstraint.java` | Fully migrated |
| `htn/guessOrdering/GuessOrderingValOH.java` | `Comparator<ConstraintNetwork>` |

**Facade & integration layer:**

| File | Changes |
|------|---------|
| `planner/CHIMP.java` | Adapter swap; removed `MetaCspResolverOrderingAdapter` overload |
| `postprocessing/HPRGenerator.java` | `FluentStoreImpl` instead of `MetaCspFluentStore` |
| `postprocessing/PlanExtractor.java` | `FluentStoreImpl` instead of `MetaCspFluentStore` |
| `hybridDomainParsing/HybridDomain.java` | Removed `fluentSolver.FluentNetworkSolver` import |
| `hybridDomainParsing/OperatorParser.java` | Removed `fluentSolver.FluentNetworkSolver` import |

**Domain-specific meta-constraints:**

| File | Changes |
|------|---------|
| `dwr/DWRNavigationMetaConstraint.java` | `AllenIntervalConstraint` → `TemporalConstraint`; `VariablePrototype` factory |
| `externalPathPlanning/MoveBaseMetaConstraint.java` | `org.metacsp` → `chimp.kernel` imports; `Bounds` → `TemporalBounds` |

**Resource schedulers:**

| File | Changes |
|------|---------|
| `resourceFluent/FluentScheduler.java` | `Activity` → `Fluent`; `CompoundSymbolicValueConstraint` → `CompoundSymbolicConstraint` |
| `resourceFluent/FluentResourceUsageScheduler.java` | `Activity` → `Fluent`; `AllenIntervalConstraint` → `TemporalConstraint` |

**UI:**

| File | Changes |
|------|---------|
| `ui/HierarchicalLayout.java` | `AllenInterval` → `TemporalVariable` |
| `ui/PlanHierarchyFrame.java` | `org.metacsp` → `chimp.kernel` imports |

**Tests:**

| File | Changes |
|------|---------|
| `characterization/JUnitTestPlannerCharacterization.java` | `MetaCspModelAdapter` → `ModelAdapter` |
| `characterization/Step0Baseline.java` | `fluentSolver` → `chimp.kernel.fluent` import |
| `chimp/search/ChimpSearchEngineTest.java` | CHIMP-native types; `DummyVariable` simplified |
| `chimp/search/ResolverOrderingCompatibilityTest.java` | CHIMP-native `ConstraintNetwork` |
| `htn/PlanRepositoryItemResolverDescriptionTest.java` | `FluentMarking` ordinals |

### Documentation Updates

| File | Changes |
|------|---------|
| `README.md` | Title: "Hybrid" not "Meta-CSP"; usage section updated |
| `docs/kernel-api.md` | Removed cross-validation/perf test references |
| `build.gradle` | Removed `org.metacsp` dependency; added source set excludes for legacy examples |

---

## Verification

```
./gradlew clean compileJava compileTestJava  → BUILD SUCCESSFUL
./gradlew test                               → BUILD SUCCESSFUL (291 tests)
./gradlew run --args="domains/ordered_domain.ddl problems/test_m_drive_robot_1.pdl"
                                             → Solution found (~5s)
grep -r "import org.metacsp" src/            → 0 results (only in excluded examples)
grep "metacsp" build.gradle                  → 0 results
```

---

## Issues Found

### 1. Search Tree Visualization Is a Stub (medium)

`ChimpSearchEngine.draw()` prints the JUNG forest to stdout instead of opening a visualization window. The summary for M15 explicitly said *"Preserve the search-tree visualization"* but this was lost when the Meta-CSP `SearchTreeFrame` dependency was removed.

```java
public void draw() {
    // TODO: replace with CHIMP-native search-tree visualization
    // (was org.metacsp.utility.UI.SearchTreeFrame.draw(...))
    System.out.println("Search tree: " + searchTree);
}
```

The JUNG `DelegateForest` data structure is still correctly populated during search — only the rendering is gone. The plan hierarchy frame (`PlanHierarchyFrame`) is preserved and functional.

**Risk**: The search tree visualization is actively used for debugging. This needs to be restored with a CHIMP-native JUNG viewer.

### 2. `drawHierarchyNetwork()` Is a Stub (low)

`CHIMP.drawHierarchyNetwork()` similarly prints a string instead of rendering a visual. Same root cause — Meta-CSP's `ConstraintNetwork.draw()` is gone.

### 3. Pre-existing Copy-Paste Bug Carried Over (low, not introduced here)

In `ChimpSchedulable.java` line 306:

```java
long eet1 = ((Fluent) currentMcs[g]).getTemporalVariable().getEST();  // should be getEET()
```

This was present in the original Meta-CSP code (line 310 before migration). Not a regression, but worth fixing.

### 4. Legacy `getAllenInterval()` Calls in Migrated Code (cosmetic)

`PlanReportroryItem.java` lines 836/847 and `HTNMetaConstraint.java` line 174 still call `getAllenInterval()` instead of `getTemporalVariable()`. The compatibility method exists and returns the correct `TemporalVariable`, so no runtime impact — but it's inconsistent with the rest of the migration.

### 5. `Constraint.setFrom()`/`setTo()` Doesn't Enforce Binary Scope (cosmetic)

If `scope.length > 2`, `setFrom()` silently replaces only `scope[0]`. All current constraints are binary, so no actual bug — but the API doesn't prevent misuse.

---

## What's Correct and Well-Done

- **Atomic swap**: All Meta-CSP types replaced simultaneously — no mixed-type states.
- **`ConstraintNetwork` auto-registers variables** on `addConstraint()` — mirrors Meta-CSP's constraint-driven variable discovery.
- **`TemporalVariable` records implicit duration edge** — critical fix for STN rebuild after constraint retractions.
- **`TemporalConstraint` unwraps `Fluent`** — handles the case where scope contains `Fluent` instead of `TemporalVariable`.
- **Sub-solvers override `addConstraints`** to loop through `addConstraint` — ensures compilation happens even with bulk adds.
- **Legacy examples preserved** in source tree, excluded from compile, runnable on `legacy-metacsp` branch.
- **291 tests pass**, including the full planner characterization suite.

---

## Remaining Work

1. **Restore search tree visualization** with a CHIMP-native JUNG viewer (replaces `SearchTreeFrame.draw()`).
2. **Restore hierarchy network visualization** (replaces `ConstraintNetwork.draw()`).
3. **Fix pre-existing `getEST()` → `getEET()` typo** in `ChimpSchedulable.java` line 306.
4. **Replace `getAllenInterval()` calls** with `getTemporalVariable()` in migrated code (cosmetic).
5. **Performance benchmarking** — the `benchmark.sh` script is available for comparing CHIMP-native vs legacy-Meta-CSP.

---

## Summary

This is a clean, well-structured atomic swap that completes the Meta-CSP removal. The main regression is the **broken search tree and hierarchy network visualization** — if `draw()` is used for debugging, that needs a CHIMP-native JUNG viewer. Everything else is solid: the build is green, 291 tests pass, zero Meta-CSP dependencies remain, and the legacy code is preserved on a separate branch for benchmarking.
