# M15 Revised Steps 9-14 Analysis

**Date**: 2024  
**Status**: Re-evaluation after Steps 1-8 completion  
**Branch**: `remove_metacsp-m15`

## Executive Summary

The original plan assumed incremental migration (adapters → ground solver → delete storage), but this is **fundamentally impossible** due to type incompatibility between CHIMP-native and Meta-CSP types. The migration must be reorganized into 3 phases with an atomic ground solver swap.

**Key Finding**: The ground solver swap is the pivotal atomic change. All consumer migration must happen as part of this swap, not before or after it independently.

---

## Current State Assessment

### Completed Work (Steps 1-8)

**Step 1-5: CHIMP-Native Kernel**
- Created 27 files (~3,900 lines) in `chimp.kernel` package
- Zero Meta-CSP imports in kernel code
- 342 unit tests passing
- Performance: 29x faster variable creation, 6x faster queries
- **Status**: ✅ Complete and tested

**Step 6: Search Tree Types**
- Created `SearchTreeNode` and `SearchTreeEdge` (CHIMP-native)
- Updated `ChimpSearchEngine` to use new types
- **Status**: ✅ Complete

**Step 7: PlanExtractor Migration**
- Migrated to use `FluentStore` interface (CHIMP-neutral)
- Removed Meta-CSP imports from plan extraction
- **Status**: ✅ Complete

**Step 8: PlanReportroryItem Migration**
- Inlined adapter code (temporal, symbolic, integer)
- Removed adapter package dependency
- **Status**: ✅ Complete

### Current Import Inventory

**Main code**: 63 files with `org.metacsp` imports
- 20 storage layer files (to be deleted)
- 13 adapter files (to be deleted)
- ~30 consumer files (to be migrated)

**Test code**: 13 files with `org.metacsp` imports
- Cross-validation tests (keep during migration, delete after)
- Storage layer tests (delete with storage)

---

## The Fundamental Blocker: Type Incompatibility

### The Problem

CHIMP-native and Meta-CSP types are **completely incompatible**:

```java
// Meta-CSP Fluent
public class Fluent extends MultiVariable {
    AllenInterval getAllenInterval() { ... }
    CompoundSymbolicVariable getCompoundSymbolicVariable() { ... }
    IntegerVariable[] getIntegerVariables() { ... }
}

// CHIMP-native Fluent
public class Fluent extends Variable {
    TemporalVariable getTemporalVariable() { ... }
    CompoundSymbolicVariable getCompoundSymbolicVariable() { ... }  // Different type!
    IntegerVariable[] getIntegerVariables() { ... }  // Different type!
}
```

**Key incompatibilities**:
1. `AllenInterval` vs `TemporalVariable` (different temporal representations)
2. `unify.CompoundSymbolicVariable` vs `chimp.kernel.symbolic.CompoundSymbolicVariable` (different packages)
3. `integers.IntegerVariable` vs `chimp.kernel.integer.IntegerVariable` (different packages)

### Why Incremental Migration Fails

The original plan assumed:
1. Migrate adapters to produce CHIMP-native types
2. Swap ground solver to CHIMP-native
3. Delete Meta-CSP storage

**This doesn't work because**:

**Step 1 fails**: If adapters produce CHIMP-native constraints, the Meta-CSP ground solver can't use them (type mismatch).

**Step 2 fails**: If ground solver is swapped to CHIMP-native, but consumers still use Meta-CSP types, the system breaks.

**The chicken-and-egg problem**:
- Can't swap ground solver without migrating all consumers
- Can't migrate consumers without swapping ground solver
- Must do both atomically

### Example: The Temporal Adapter

Attempting to migrate `MetaCspTemporalConstraintAdapter`:

```java
// BEFORE: Produces Meta-CSP constraint (works with Meta-CSP solver)
public AllenIntervalConstraint toAllenIntervalConstraint(TemporalConstraintTemplate template) {
    return new AllenIntervalConstraint(...);
}

// AFTER: Produces CHIMP-native constraint (BROKEN with Meta-CSP solver)
public TemporalConstraint toTemporalConstraint(TemporalConstraintTemplate template) {
    return new TemporalConstraint(...);
}
```

**Result**: `MetaCspProblemCompiler` can't add `TemporalConstraint` to Meta-CSP's `FluentNetworkSolver` because it expects `AllenIntervalConstraint`.

---

## Revised Migration Plan

### Phase 1: Prepare CHIMP-Native Ground Solver (Step 9)

**Goal**: Ensure `chimp.kernel.fluent.FluentNetworkSolver` is production-ready and can replace Meta-CSP's version.

**Tasks**:
1. Verify all consumer methods exist on CHIMP-native `Fluent`:
   - `getAllenInterval()` → returns `TemporalVariable` ✅ (already exists)
   - `getCompoundSymbolicVariable()` → returns CHIMP-native type ✅
   - `getIntegerVariables()` → returns CHIMP-native types ✅
   - `getMarking()` / `setMarking()` ✅
   - `getID()` ✅
   - `getComponent()` ✅
   - `getPossiblePredicateNames()` ✅
   - `isActivity()` ✅
   - `setName()` / `getName()` ✅
   - `getTypeStr()` ✅

2. Add any missing methods:
   - `getSymbols()` - needed for debugging
   - `getVariable()` - needed for compatibility
   - `setDomain()` - needed for initialization

3. Ensure `FluentNetworkSolver` has all methods:
   - `createVariable()` / `createVariables()` ✅
   - `addConstraint()` / `addConstraints()` ✅
   - `removeConstraint()` ✅
   - `getConstraints()` ✅
   - `getVariables()` ✅
   - `propagate()` ✅
   - Any additional methods used by consumers

**Effort**: 1-2 days  
**Risk**: Low (mostly verification and minor additions)  
**Verify**: All CHIMP-native kernel tests pass, API is complete

### Phase 2: Atomic Ground Solver Swap (Step 10)

**Goal**: Replace Meta-CSP ground solver with CHIMP-native ground solver and update all consumers in one atomic change.

**This is the big bang migration.**

**Tasks**:

1. **Swap ground solver in HTNPlanner** (1 file):
   ```java
   // BEFORE
   private final FluentNetworkSolver groundSolver = new FluentNetworkSolver(...);
   
   // AFTER
   private final chimp.kernel.fluent.FluentNetworkSolver groundSolver = 
       new chimp.kernel.fluent.FluentNetworkSolver(...);
   ```

2. **Update HTNPlanner methods** (1 file):
   - `addResolverSub()`: Use CHIMP-native `Fluent` and `VariablePrototype`
   - `retractResolverSub()`: Use CHIMP-native types
   - All other methods that touch Meta-CSP types

3. **Update all meta-constraints** (6 files):
   - `HTNMetaConstraint.java`
   - `DWRNavigationMetaConstraint.java`
   - `MoveBaseMetaConstraint.java`
   - `FluentResourceUsageScheduler.java`
   - `FluentScheduler.java`
   - `GuessOrderingMetaConstraint.java`
   
   Changes:
   - Replace `fluentSolver.Fluent` with `chimp.kernel.fluent.Fluent`
   - Replace `fluentSolver.FluentConstraint` with `chimp.kernel.fluent.FluentConstraint`
   - Replace Meta-CSP constraint types with CHIMP-native types
   - Update type casts and method calls

4. **Update plan extraction** (3 files):
   - `CHIMP.java`
   - `PlanExtractor.java`
   - `HPRGenerator.java`
   
   Changes:
   - Use CHIMP-native `Fluent` types
   - Update constraint access patterns

5. **Update domain parsing** (6 files):
   - `HybridDomain.java`
   - `HTNMethod.java`
   - `HTNOperator.java`
   - `EffectTemplate.java`
   - `OrderingConstraintTemplate.java`
   - `IntegerConstraintTemplate.java`
   
   Changes:
   - Replace Meta-CSP enum imports with CHIMP-native enums
   - Update constraint creation code

6. **Update UI** (3 files):
   - `SearchTreeFrame.java` (use CHIMP-native `SearchTreeNode`)
   - `ConstraintNetworkFrame.java` (use CHIMP-native types)
   - `PlanHierarchyFrame.java` (use CHIMP-native `Fluent`/`FluentConstraint`)

7. **Update all tests** (~15 files):
   - Remove Meta-CSP imports
   - Update type references
   - Delete cross-validation tests (no longer needed)

8. **Run full test suite**:
   - All tests must pass after the swap
   - Fix any runtime errors

**Effort**: 5-7 days  
**Risk**: High (atomic change, but necessary)  
**Verify**: Full test suite green, zero Meta-CSP imports in production code

### Phase 3: Delete Meta-CSP (Step 11)

**Goal**: Remove Meta-CSP storage layer and dependency.

**Tasks**:

1. **Delete storage layer** (20 files):
   - `src/main/java/fluentSolver/` (4 files)
   - `src/main/java/unify/` (7 production + 2 test files)
   - `src/main/java/integers/` (4 production + 2 test files)
   - `src/main/java/chimp/adapter/metacsp/` (10 files)

2. **Remove dependency from build.gradle**:
   ```gradle
   // DELETE this line
   implementation 'org.metacsp:meta-csp-framework:2.4.7'
   ```

3. **Delete cross-validation tests** (no longer needed):
   - `TemporalSolverCrossValidationTest.java`
   - `SymbolicSolverCrossValidationTest.java`
   - `IntegerSolverCrossValidationTest.java`
   - `TemporalPerformanceTest.java`

4. **Update documentation**:
   - Remove references to Meta-CSP from README
   - Update API documentation
   - Remove decoupling plan docs (mission accomplished)

5. **Final verification**:
   - `grep -r "org.metacsp" src/` → zero results
   - `./gradlew test` → all tests pass
   - Manual testing of example domains

**Effort**: 1-2 days  
**Risk**: Low (cleanup, no logic changes)  
**Verify**: Zero Meta-CSP references, all tests pass

### Phase 4: Migrate Examples (Step 12)

**Goal**: Update example files to use CHIMP-native types.

**Tasks**:
- Update ~13 example files
- Ensure all examples compile and run
- Update documentation with new API usage

**Effort**: 1-2 days  
**Risk**: Low  
**Verify**: All examples compile and run successfully

---

## Revised Step Summary

| Step | Description | Files | Effort | Risk |
|------|-------------|-------|--------|------|
| 9 | Prepare CHIMP-native ground solver | 1-2 | 1-2 days | Low |
| 10 | Atomic ground solver swap + consumer migration | ~30 | 5-7 days | High |
| 11 | Delete Meta-CSP storage + dependency | 20+ | 1-2 days | Low |
| 12 | Migrate examples + documentation | ~13 | 1-2 days | Low |

**Total**: 8-13 days (down from original 10-15 days)

---

## Risks and Mitigations

### Risk 1: Atomic Change Complexity
**Risk**: Step 10 requires changing ~30 files in one atomic commit. If something breaks, rollback is difficult.

**Mitigation**:
- Extensive testing in Phase 1 ensures CHIMP-native solver is ready
- Run full test suite after each file change (not just at the end)
- Keep git history clean with logical commits
- Have rollback plan ready (revert to Step 8 commit)

### Risk 2: Missing Methods
**Risk**: CHIMP-native types might be missing methods that consumers need.

**Mitigation**:
- Phase 1 includes comprehensive API verification
- Add any missing methods before Phase 2
- Test with actual consumer code during Phase 1

### Risk 3: Performance Regression
**Risk**: CHIMP-native solver might be slower than Meta-CSP in some scenarios.

**Mitigation**:
- Performance tests show CHIMP-native is 29x faster for variable creation
- 33x performance regression in constraint addition was fixed in Step 5a
- Monitor performance during Phase 2, optimize if needed

### Risk 4: Behavioral Differences
**Risk**: CHIMP-native solver might behave differently than Meta-CSP (edge cases).

**Mitigation**:
- Cross-validation tests (Steps 5b) verified 22 constraint scenarios
- Integration tests (Step 5c) verified 11 planning scenarios
- Full test suite includes 378 tests covering various domains

---

## Recommendations

1. **Execute Phase 1 immediately**: Prepare the CHIMP-native ground solver. This is low-risk and unblocks everything.

2. **Execute Phase 2 carefully**: The atomic swap is the critical step. Allocate sufficient time (5-7 days) and test thoroughly.

3. **Execute Phase 3 and 4 quickly**: These are cleanup tasks with low risk.

4. **Update documentation**: After Phase 3, update the decoupling plan to mark M15 as complete.

5. **Consider porting**: Once Meta-CSP is removed, consider porting CHIMP to another language (as mentioned in project instructions).

---

## Conclusion

The original 6-step plan (Steps 9-14) has been revised to 4 phases that account for the fundamental type incompatibility between CHIMP-native and Meta-CSP. The key insight is that the ground solver swap must be atomic, and all consumer migration must happen as part of that swap.

**Original Plan**: Incremental migration (impossible due to type incompatibility)  
**Revised Plan**: 3-phase migration with atomic ground solver swap

**Next Action**: Begin Phase 1 (prepare CHIMP-native ground solver).
