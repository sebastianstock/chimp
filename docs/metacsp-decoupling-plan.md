# Meta-CSP Coupling and Decoupling Plan

## Summary

CHIMP is a hierarchical hybrid planner implemented in Java 17. It depended on the
Meta-CSP Framework (`org.metacsp:meta-csp-framework:1.3.5`). The primary goal — make
CHIMP completely independent of Meta-CSP, replacing the Meta-CSP storage layer with a
CHIMP-native solver kernel — is **achieved** (branch `remove_metacsp-m15`). A separate,
later goal is to make CHIMP available as a planner in the Unified Planning library (UP)
— an *integration* (CHIMP as a UP planner provider), not a *dependency* (CHIMP must not
depend on UP). CHIMP may also be ported to another programming language before UP
integration. For constraint propagation, the CHIMP-native solver kernel may use any
suitable constraint programming library, not necessarily UP.

## Status Snapshot (as of commit `7d2fc1a`)

- `build.gradle` has **no Meta-CSP dependency**; `grep -r "org.metacsp" src/main src/test`
  returns zero compiled imports.
- The former Meta-CSP storage layer (`fluentSolver/`, `unify/`, `integers/`) is deleted;
  the CHIMP-native kernel lives in `chimp.kernel` (base types, symbolic, temporal,
  integer, fluent solvers).
- The adapter package `chimp.adapter.metacsp` was retired during the M15 migration
  (Steps 7–8 inlined the remaining adapters; Phase 3 deleted the storage layer).
- **8 legacy example source files** still reference the old API
  (`examples/TestDWRDomain.java`, `examples/TestRuntimes.java`,
  `examples/TestTransTerraProblems.java`, `examples/VariousTests.java`,
  `examples/negative_precondition_resource/TestRemoveAllSamples.java`,
  `examples/eval/*`, `examples/chimp/TestIncrementalMerging.java`,
  `examples/chimp/TestPlanningWithIntegerConstraints.java`). They are excluded from
  compilation in `build.gradle` and kept only as reference for the `legacy-metacsp`
  branch.
- Test suite green (~290 `@Test` methods); behavior verified against the legacy branch
  on a 93-problem benchmark suite (see Verification Strategy).
- Search-tree visualization is preserved in `chimp.ui` (JUNG `DelegateForest` retyped,
  Meta-CSP frames copied/retyped during M15 Step 12).

## Completed Milestones (history)

Details for each milestone live in the git history (per-milestone plan docs
were removed once superseded).

- **M1–M5**: CHIMP-owned model and search-facing types (`chimp.model`, `chimp.search`),
  domain parsers emit neutral values, resolver-description layer (M3), value-ordering
  comparator hierarchy (`CHIMPValOH`), `MetaCspResolverOrderingAdapter`.
- **M6**: Fluent read boundary (`FluentView`/`FluentStore`); `PlanExtractor` and
  `HPRGenerator` fully migrated (`postprocessing` Meta-CSP-free).
- **M7**: Unification and integer constraint construction hidden behind factory
  adapters in `PlanReportroryItem`.
- **M8**: Resource scheduling absorbed into M1–M5 + M9 (no independent wedge).
- **M9**: Search algorithm ported to `ChimpSearchEngine`; `HTNPlanner` broke the
  `MetaConstraintSolver` inheritance; meta-constraints hold `groundSolver` directly
  (`GroundSolverHolder`).
- **M10**: Peripheral migration — dispatching/sensing deleted, `CHIMPValOH` migration
  (16 files), `ChimpLogging` replaces `MetaCSPLogging`.
- **M11**: `CHIMP.extractActions()` returns CHIMP-owned `PlannedAction[]`.
- **M12**: Resolver materialization is description-driven
  (`buildResolverFromDescription`); legacy `materializeResolverDescriptions()` deleted.
- **M13**: All 6 meta-constraints/schedulers broke `MetaConstraint`/`Schedulable`
  inheritance via `ChimpMetaConstraint`/`ChimpSchedulable`; `MetaCspFlawProviderAdapter`
  deleted.
- **M14**: `TemporalBounds` drop-in for `org.metacsp.time.Bounds`; `FluentStore` gained
  write operations.
- **M15 Steps 0–5**: CHIMP-native solver kernel implemented (`chimp.kernel`, 24 files,
  ~3,300 lines): base types, symbolic (AC-3), integer, temporal (STN), fluent solvers.
  Cross-validation against Meta-CSP caught 11 temporal bugs that unit tests missed.
- **M15 Steps 6–14 + Phases 1–4**: All consumers migrated to the kernel (resolver
  representation, `addResolverSub`, `buildResolverFromDescription`, meta-constraints,
  facade, parsing, UI); storage layer deleted in an atomic swap (`51a07ba`);
  `org.metacsp` removed from `build.gradle`; docs/examples updated.
- **Legacy-branch parity hardening** (`eb7b407` → `7fce835`): stale live-network and
  sub-constraint bugs after backtracking, transactional (all-or-nothing)
  `addConstraints` rollback in all four solvers, and four search-ordering fixes
  required for exact legacy search parity. These were the difference between partial
  and full benchmark parity.
- **Benchmark expansion** (`f389e74`): 40 → 93 problems (DWR, RACE, TransTerra,
  mobipick, gecko, ICO), including RACE/ICO method-level tests.
- **Performance follow-ups**: incremental STN propagation + O(1) infeasibility
  pre-check + time-point pooling (`7a9279b`, report in
  `docs/stn-performance-improvements.html`); symbolic solver bitset domains,
  incremental adjacency map, AC-3 worklist dedup (`7d2fc1a`).
- **Reference branch**: `legacy-metacsp` (tag `legacy-metacsp-pre-removal`, commit
  `c56dd39`) preserves the pre-removal code for benchmarking;
  `scripts/compare-benchmark.sh` runs both sides.

## Remaining Work

In rough priority order:

1. **Legacy example cleanup** — decide the fate of the 8 excluded Meta-CSP example
   files: delete them, or keep them as reference-only (they are already excluded from
   compilation and documented in `build.gradle`).
2. **Search-tree visualization gap** — the retyped JUNG viewer exists in `chimp.ui`,
   but the interactive `draw()` flow should be re-verified end-to-end after the
   storage-layer swap (user-facing debugging tool).
3. **Known small defects**:
   - `ChimpSchedulable.java` (~line 306): pre-existing `getEST()` → `getEET()` typo.
   - Static variable-prototype counter drift: search trees are benchmark-order
     sensitive; consider a per-problem counter reset or a stable non-wrapping id
     ordering.
4. **Optional performance** — the temporal solver (STN) is now the dominant cost
   (~48% of solve time on RACE-serve_coffee) if further speedup is wanted.
5. **Documentation refresh** — keep this plan and `docs/kernel-api.md` in sync with
   the final state; the STN report (`docs/stn-performance-improvements.html`) is
   current.

## Future: UP Integration (Milestone 16, separate goal)

**Goal**: Make CHIMP available as a planner in the Unified Planning library (UP).

This is **not** a dependency — CHIMP must not depend on UP. CHIMP acts as a UP planner
provider, wrapping its planning capabilities behind the UP `Planner` interface. This
may happen after the decoupling is finalized, or after a port to another programming
language.

**Scope**: TBD — depends on the final CHIMP API surface and UP library capabilities.
**Risk**: Medium — adapter layer, no changes to CHIMP core.

## Verification Strategy

- `./gradlew test` — full unit/integration suite (~290 tests) must stay green.
- **Benchmark gate**: `./scripts/compare-benchmark.sh 3` — 93-problem suite run against
  the `legacy-metacsp` worktree. Required after any behavior-affecting change.
  Current state: 93/93 solve on both sides, 0 solvability differences.
- **Known legacy nondeterminism**: the legacy (Meta-CSP) branch flips 7 problems
  between JVM invocations (symmetric-variant plans: `leftArm1`↔`rightArm1` arm choices,
  `r1`↔`r2` robot identity in DWR). The native kernel is deterministic across runs.
  Plan/action-sequence mismatches on these problems are equivalent-variant plans, not
  regressions; compare against the set of problems whose legacy output is stable.
- Domain tests (DWR, RACE, TransTerra, mobipick, gecko, integer planning, resource,
  HPR generation, characterization) cover solver-level and end-to-end behavior.
- Kernel solver cross-validation tests compare native solvers against reference
  implementations (retained as regression nets).

## Engineering Rules

- Small, reviewable changes. Every change compiles and keeps `./gradlew test` green.
- No large rewrites. Each step moves one concept behind a CHIMP-owned abstraction.
- No speculative abstractions or layers beyond what the current step requires.
- Preserve existing public behavior unless the task explicitly says otherwise.
- Keep Java 17 compatibility.
- Avoid adding production dependencies without asking.
