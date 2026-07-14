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

CHIMP is a research project. Keep the overall architecture simple and avoid overcomplicating it during the Meta-CSP removal process. Do not introduce speculative abstractions, layers, or indirection beyond what the current step requires.

CHIMP is a hierarchical hybrid planner implemented in Java 17. It currently depends on the Meta-CSP Framework. The primary goal is to make CHIMP completely independent of Meta-CSP, replacing the Meta-CSP storage layer with a CHIMP-native solver kernel. A separate, later goal is to make CHIMP available as a planner in the Unified Planning library (UP) — this is an integration (CHIMP as a UP planner provider), not a dependency (CHIMP must not depend on UP). CHIMP may be ported to another programming language before UP integration. For constraint propagation, the CHIMP-native solver kernel may use any suitable constraint programming library.

## Main engineering rule

Do not perform large rewrites. Make small, reviewable changes. Every change should compile and should either add tests, add documentation, or move one concept behind a CHIMP-owned abstraction.

## Build and test

- Prefer `./gradlew test` if the Gradle wrapper exists.
- Also run the example planner commands when relevant.
- If a command fails because of missing local setup, report exactly what failed and do not invent results.

## Architecture rules

- Do not remove Meta-CSP directly.
- First isolate Meta-CSP dependencies behind CHIMP-owned interfaces and data structures.
- Preserve existing public behavior unless the task explicitly says otherwise.
- Keep Java 17 compatibility.
- Avoid adding production dependencies without asking.

## Refactoring target

The desired direction is:
- CHIMP model classes independent of Meta-CSP.
- CHIMP search concepts independent of Meta-CSP.
- Meta-CSP kept temporarily as an adapter/backend.
- Eventually replace the backend with a CHIMP-native solver kernel.

## Review expectations

Before finishing a task:
- summarize changed files;
- explain whether behavior changed;
- state which commands were run;
- state remaining risks.
