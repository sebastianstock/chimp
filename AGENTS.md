# AGENTS.md

## General coding behaviour


### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---


## Project context

CHIMP is a research project. Keep the overall architecture simple and do not introduce speculative abstractions, layers, or indirection beyond what the current step requires.

CHIMP is a hierarchical hybrid planner implemented in Java 17. It is fully independent of the Meta-CSP Framework: constraint solving is provided by the CHIMP-native solver kernel in `chimp.kernel` (see `docs/kernel-api.md`), which may use any suitable constraint programming library. The Meta-CSP removal is complete and verified; `docs/metacsp-removal.md` records the history, current status, and verification strategy. The `legacy-metacsp` branch (tag `legacy-metacsp-pre-removal`) preserves the pre-removal code as a benchmarking reference.

The next planned goal is to make CHIMP available as a planner in the Unified Planning library (UP) — this is an integration (CHIMP as a UP planner provider), not a dependency (CHIMP must not depend on UP). CHIMP may be ported to another programming language before UP integration.

## Main engineering rule

Do not perform large rewrites. Make small, reviewable changes. Every change should compile and should either add tests, add documentation, or improve the code while preserving behavior.

## Build and test

- Prefer `./gradlew test` if the Gradle wrapper exists.
- After any behavior-affecting change, run the benchmark gate `./scripts/compare-benchmark.sh 3` (93-problem suite against the `legacy-metacsp` worktree; see the decoupling plan for how to interpret its output).
- Also run the example planner commands when relevant.
- If a command fails because of missing local setup, report exactly what failed and do not invent results.

## Architecture rules

- Preserve existing public behavior unless the task explicitly says otherwise.
- Keep Java 17 compatibility.
- Avoid adding production dependencies without asking.
- Keep the CHIMP-native kernel (`chimp.kernel`) free of external framework dependencies.

## Review expectations

Before finishing a task:
- summarize changed files;
- explain whether behavior changed;
- state which commands were run;
- state remaining risks.
