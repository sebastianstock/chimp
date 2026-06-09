# AGENTS.md

## Project context

CHIMP is a hierarchical hybrid planner implemented in Java 17. It currently depends on the Meta-CSP Framework. The long-term goal is to make CHIMP independent of Meta-CSP and integrate it with the Unified Planning library (UP) hile preserving planner semantics.

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
