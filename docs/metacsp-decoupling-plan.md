# Removing the Meta-CSP Dependency

This document records how the Meta-CSP Framework dependency was removed from CHIMP:
the approach, the milestones, the defects found and fixed along the way, and the
verification strategy used to prove behavior parity.

## Summary

CHIMP is a hierarchical hybrid planner implemented in Java 17. It used to depend on
the Meta-CSP Framework (`org.metacsp:meta-csp-framework:1.3.5`), whose storage layer
(fluent, symbolic, integer, temporal constraint networks) was entangled with CHIMP's
HTN search. The dependency was removed by first isolating every Meta-CSP concept
behind CHIMP-owned interfaces and data structures, then replacing the storage layer
with a CHIMP-native solver kernel (`chimp.kernel`), and finally deleting the
Meta-CSP code entirely in an atomic swap. The removal is **complete and verified**.

## Final State (branch `remove_metacsp-m15`)

- `build.gradle` has **no Meta-CSP dependency**; `grep -r "org.metacsp" src/main src/test`
  returns zero compiled imports.
- The former Meta-CSP storage layer (`fluentSolver/`, `unify/`, `integers/`) is deleted;
  the CHIMP-native kernel lives in `chimp.kernel` (base types, symbolic, temporal,
  integer, fluent solvers).
- The adapter package `chimp.adapter.metacsp` was retired during the M15 migration
  (Steps 7–8 inlined the remaining adapters; Phase 3 deleted the storage layer).
- The 10 legacy example sources that referenced the removed API were deleted
  (`dce822f`); the `legacy-metacsp` branch retains its own copies for reference.
- Test suite green (295 `@Test` methods across 39 files); behavior verified against
  the legacy branch on a 93-problem benchmark suite (see Verification Strategy).
- Search-tree visualization restored and verified in `chimp.ui` (`SearchTreeFrame`,
  `e567faf`): `CHIMP.drawSearchSpace()` opens the CHIMP-native forest in the retyped
  Meta-CSP viewer; SUCCESS/FAILURE terminals are colored green/red.

## Milestones (history)

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

## Defects Found and Fixed During the Removal

These surfaced through benchmark-parity testing and deterministic-replay checks after
the kernel swap; all were verified plan-neutral or parity-restoring before being kept.

- **Search-tree visualization gap** — restored and verified (`e567faf`):
  `ui.SearchTreeFrame` re-hosts the Meta-CSP viewer for the CHIMP-native forest;
  `CHIMP.drawSearchSpace()` opens it. Verified end-to-end on a live display (mobipick
  and RACE serve_coffee render flaw chains, edge labels, terminal coloring) plus
  headless forest assertions in `ChimpSearchEngineTest`. Also fixed a latent bug:
  `TerminalNode.isTerminal()` was always false because the terminal flag did not flow
  through the constructor chain.
- **Schedulable conflict-detection bug** — fixed (`a66c2d7`). The `eet1` register read
  `getEST()` — a defect inherited verbatim from upstream Meta-CSP 1.3.5 (verified by
  decompiling `Schedulable.getConflict`). An experiment with the fix in place showed
  **0 of 93 benchmark plans change** (`dmin` dominates the pair bounds in practice on
  these domains), so the fix was adopted: conflict detection now matches the
  algorithm's intent.
- **Static variable-prototype counter drift** — fixed (`98cc6c7`):
  `VariablePrototype.resetIds()` is called at the start of each solve, and
  `describeResolvers` uses a `LinkedHashSet` (HTNPrecondition is identity-hashed).
  Running the 93-problem suite in reversed order now produces byte-identical plans,
  i.e. results are independent of prior solves in the same JVM.

## Verification Strategy

- `./gradlew test` — full unit/integration suite (295 tests) must stay green.
- **Benchmark gate**: `./scripts/compare-benchmark.sh 3` — 93-problem suite run against
  the `legacy-metacsp` worktree. Required after any behavior-affecting change.
  Final result: 93/93 solve on both sides, 0 solvability differences, 3/93
  equivalent-variant mismatches (see below), native total solver time faster than
  legacy.
- **Known legacy nondeterminism**: the legacy (Meta-CSP) branch flips 7 problems
  between JVM invocations (symmetric-variant plans: `leftArm1`↔`rightArm1` arm choices,
  `r1`↔`r2` robot identity in DWR). The native kernel is deterministic across runs.
  Plan/action-sequence mismatches on these problems are equivalent-variant plans, not
  regressions; compare against the set of problems whose legacy output is stable.
- Domain tests (DWR, RACE, TransTerra, mobipick, gecko, integer planning, resource,
  HPR generation, characterization) cover solver-level and end-to-end behavior.
- Kernel solver cross-validation tests compare native solvers against reference
  implementations (retained as regression nets).

## Engineering Rules (followed during the removal)

- Small, reviewable changes. Every change compiles and keeps `./gradlew test` green.
- No large rewrites. Each step moved one concept behind a CHIMP-owned abstraction.
- No speculative abstractions or layers beyond what the current step required.
- Preserve existing public behavior unless the task explicitly says otherwise.
- Keep Java 17 compatibility.
- Avoid adding production dependencies without asking.
