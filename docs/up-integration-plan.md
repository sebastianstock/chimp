# UP Integration Plan (M16)

Goal: make CHIMP available as a planner in the Unified Planning library (UP)
<https://unified-planning.readthedocs.io>. Per AGENTS.md, this is an
*integration*: CHIMP is a UP planner *provider*; CHIMP itself must not depend
on UP.

## Research findings

### How UP integrates external planners

An integration is a pip package containing an **engine** class
(`unified_planning.engines.Engine` + a mixin such as `OneshotPlannerMixin`)
that declares:

- a `name` (e.g. `"chimp"`),
- a `supported_kind()` — the set of `ProblemKind` features it can handle
  (UP filters engines by comparing against the problem's kind),
- `solve(problem)` returning a `PlanGenerationResult` with a UP plan object.

Discovery: UP's `Factory` tries to import a hardcoded list of module names in
`DEFAULT_ENGINES` (e.g. `"enhsp": ("up_enhsp.enhsp_planner", "ENHSPEngine")`);
if the pip package is installed, the engine is available by name. Engines not
(yet) in that list can be registered via an `up.ini` config file or
programmatically (`Factory.add_engine`). First-class status = a PR to
unified-planning adding the package to `DEFAULT_ENGINES`.

### The three existing integration patterns

1. **PDDL subprocess (ENHSP, Fast-Downward)**: the engine subclasses UP's
   `PDDLPlanner` base class, which writes PDDL files, runs the planner binary
   (`java -jar enhsp.jar -o domain.pddl -f problem.pddl -sp plan.txt`), and
   parses the plan file. The pip package bundles the planner jar/binary.
   <https://github.com/aiplan4eu/up-enhsp>
2. **gRPC protocol (Aries — the FAPE successor, plaans/Arthur Bit-Monnot)**:
   the planner binary implements UP's protobuf/gRPC protocol
   (`unified_planning.grpc.unified_planning.proto`, 932 lines, fully covers
   HTN incl. temporal constraints between subtask endpoints). The Python side
   (`up_aries`, ~600 lines) just starts the server subprocess, converts the
   UP problem to protobuf, and converts the response back. Binaries for all
   platforms are bundled on PyPI; `aries`/`aries-val` are now in UP's
   `DEFAULT_ENGINES`. <https://github.com/plaans/aries/tree/master/planning/unified/plugin>
3. In-process Python planners (tamer, pyperplan) — not applicable to CHIMP.

### UP language coverage relevant to CHIMP

- `HierarchicalProblem` (subclass of `Problem`): types, typed fluents,
  objects, actions (instantaneous + durative with timed conditions/effects),
  abstract **tasks**, **methods** (achieved task, preconditions, typed
  parameters, subtasks with identifiers), initial **task network** with
  total/partial/temporal orderings (`set_ordered`, subtask-endpoint
  references).
- ProblemKind features: `HIERARCHICAL`, `METHOD_PRECONDITIONS`,
  `TASK_ORDER_TOTAL` / `TASK_ORDER_PARTIAL` / `TASK_ORDER_TEMPORAL`,
  `TASK_NETWORK_CONSTRAINTS`, `INITIAL_TASK_NETWORK_VARIABLES`.
- Plans: `TimeTriggeredPlan` (start time + action instance) is the standard
  result type for temporal planners (Aries returns it).
- UP also has ANML and HDDL writers — *not* useful directly: CHIMP's input
  format is its own DDL/PDL hybrid format, so either way a conversion must
  target CHIMP's format.

### CHIMP's current integration surface

- CLI: `ChimpIO` (`chimp <domain.ddl> <problem.pdl> [-o plan.yaml]
  [--horizon N] ...`), plan output as YAML: per activity `id`, `name`,
  `arguments`, `time: [[est, lst], [eet, let]]` (flexible intervals). This is
  the exact code path fixed and verified in the CLI regression fix
  (`2541108`).
- Input format: domain = operators (`Head`/`Pre`/`Add`/`Del` + Allen
  constraints on task/fluents, `Duration[...]`, `Values`/`Type` restrictions,
  `ResourceUsage`) and methods (`Head` task, `Pre`, `Sub` subtasks,
  `Ordering`, Allen constraints between subtasks, `VarEqual`/`VarDifferent`);
  problem = `ArgumentSymbols`, `Instances` (type membership), initial
  `Fluent` + `Release` constraints, initial `Task` + temporal constraints.

## Options

| | A. File-based via ChimpIO (ENHSP pattern) | B. gRPC server (Aries pattern) | C. ANML/HDDL intermediate |
|---|---|---|---|
| Python side | medium (writer + reader) | small (proxy, ~200 lines) | medium |
| Java side | **none** | large (proto→CHIMP mapping + grpc-java server) | none |
| New CHIMP deps | none | grpc-java + protobuf (outside `chimp.kernel`) | none |
| Robustness | own format both sides (stable, we own it) | typed protocol, no text parsing | double conversion, lossy |
| Language-port tolerant | **yes** (contract = CLI + file formats) | no (server must be re-implemented) | yes |
| Effort | **~3–4 weeks** | ~5–7 weeks | ~3–4 weeks + semantic risk |

**Recommendation: A.** It uses the existing, recently verified CLI contract;
adds no production dependencies to CHIMP; and is insensitive to the open
language-port question (any port just has to keep the CLI/file contract).
Option B can be added later as a second front-end without redoing the
model-mapping logic if remote execution ever becomes a requirement. C is
rejected (ANML/HDDL ≠ CHIMP semantics, double conversion).

## Recommended architecture

New Python package `up-chimp`, developed **inside this repository** (Aries
model: the plugin co-evolves with the planner) at `up-chimp/`:

```
up-chimp/
  setup.py / pyproject.toml      # bundles chimp.jar; CHIMP_JAR env var overrides
  up_chimp/
    __init__.py                  # engine classes
    engine.py                    # ChimpEngine(Engine, OneshotPlannerMixin)
    writer.py                    # UP HierarchicalProblem -> .ddl / .pdl
    reader.py                    # plan.yaml -> TimeTriggeredPlan
  test/                          # pytest suite
```

Solve flow: `writer` serialises the UP problem to temp `.ddl`/`.pdl` → run
`java -jar chimp.jar domain.ddl problem.pdl -o plan.yaml` (timeout handling,
exit-code mapping) → `reader` maps YAML activities to UP `ActionInstance`s
(match by name+arguments) → `TimeTriggeredPlan` with start times
(decided: earliest start/eet from the flexible intervals, using UP's
`correct_plan_generation_result` epsilon helper for coinciding times;
`STNPlan` output may be added later as an additive option).

Gradle task to build the bundled jar (fat jar incl. Choco/ANTLR; the
`application` plugin's `installDist` or a manual jar — build detail, decided
in M16.1).

### Feature mapping (UP → CHIMP), v1 subset

| UP | CHIMP |
|---|---|
| `UserType` hierarchy | flatten to `Instances <type> <objects…>` (objects of subtypes included) |
| `Fluent` (symbolic, typed) | `StateVariable <name> <arity>`; value domain = objects of the value type |
| `Object` | `ArgumentSymbols` entry + `Instances` |
| `InstantaneousAction` | `(:operator (Head !name(?p …)) (Pre …) (Add …) (Del …))` |
| `DurativeAction` (at-start/at-end conditions & effects, duration) | operator + `Duration[lb,ub](task)` + Allen constraints (`OverlappedBy(task,pre)`, `Overlaps(task,effect)`, …) — **the core semantic work** |
| `Task` (abstract) | task name introduced by method `Head`s / `Sub` references |
| `Method` (achieved task, preconditions, subtasks) | `(:method (Head task(?p …)) (Pre …) (Sub sN …))` |
| Method subtask orderings (total/partial/temporal) | `Ordering` entries + Allen constraints between subtask idents |
| Initial task network (+ constraints) | `(Task tN name(args…))` + temporal constraints |
| Initial fluent values | `(Fluent cN f(args))` + `(Constraint Release[0,0](cN))` |

v1 `supported_kind` (everything else declared unsupported, like Aries does):
`HIERARCHICAL`, `ACTION_BASED`, `METHOD_PRECONDITIONS`,
`TASK_ORDER_TOTAL`, `TASK_ORDER_PARTIAL`, `TASK_ORDER_TEMPORAL`,
`CONTINUOUS_TIME`/`DISCRETE_TIME`, `DURATION_INEQUALITIES`, `EQUALITIES`
conditions, `FLAT_TYPING`/`HIERARCHICAL_TYPING`, int parameters.
Explicitly **out** for v1: numeric fluents/integer constraints (CHIMP has
them, UP mapping is a separate step), resources, `NEGATIVE_CONDITIONS`,
`DISJUNCTIVE_CONDITIONS`, goals-as-fluents (require a task network),
`TASK_NETWORK_CONSTRAINTS`, `INITIAL_TASK_NETWORK_VARIABLES`.

## Milestones & effort

| Step | Content | Verify | Effort |
|---|---|---|---|
| M16.0 spike | Hand-write a small `HierarchicalProblem` (UP) and its hand-converted `.ddl`/`.pdl`; run ChimpIO; pin down the DurativeAction→Allen-constraint mapping on a real case | plan matches expectation; decisions recorded here | 2–3 d |
| M16.1 packaging | `up-chimp/` skeleton, engine class, jar build + bundling, `up.ini` registration (see Decisions), credits, timeout/exit-code handling | `pip install -e up-chimp`; `OneshotPlanner(name='chimp')` returns "unsupported kind" for a non-HTN problem | 2–3 d |
| M16.2 writer | types/objects/fluents/actions/methods/task-network/initial state → `.ddl`/`.pdl` | golden-file unit tests vs. the M16.0 files; round-trip: converted problem solved by ChimpIO | 5–8 d |
| M16.3 reader | YAML → `TimeTriggeredPlan`, status mapping | unit tests; end-to-end solve of the M16.0 problem through the engine | 2–3 d |
| M16.4 validation | pytest suite on several UP problems (logistics-style HTN example + a RACE/Mobipick-style service task); validate returned plans with UP's `TimeTriggeredPlanValidator` (catches infeasible earliest-start dispatches); README; CI (build jar + pytest) | CI green; `up.engines.get_engine('chimp')` works for a documented UP problem | 3–4 d |
| M16.5 (later) | PR to unified-planning for `DEFAULT_ENGINES`; PyPI release; revisit gRPC/validator/anytime | upstream review | — |

**Total for a working MVP: ~15–20 person-days (3–4 weeks full-time).**
The semantic core (M16.0 + M16.2) is where the risk and most of the effort
sits.

## Risks & mitigations

- **Durative-action semantics**: UP's timed conditions/effects must be
  compiled into CHIMP's Allen-constraint operator model. Mitigation: M16.0
  spike before any infrastructure; restrict `supported_kind` to what was
  actually validated.
- **UP condition expressiveness**: CHIMP preconditions are fluent-equality
  based; general FNode formulas don't map. Mitigation: declare only
  `EQUALITIES` (+ typed parameters) in v1; expand later.
- **UP API churn**: `ProblemKind` versioning means an engine pinned to an old
  feature set stays loadable, but new UP releases may still break details.
  Mitigation: pin a tested `unified-planning` version range in
  `install_requires`; CI matrix over supported versions.
- **JVM startup per solve** (~1–2 s): acceptable for oneshot use; document.
  (gRPC server would amortise this — another argument to keep B as a later
  option.)
- **Jar size on PyPI** (fat jar incl. Choco/ANTLR/JUNG, likely 20–40 MB):
  acceptable (ENHSP bundles a jar too); offer `CHIMP_JAR` env override.

## Decisions (resolved)

1. **Package layout: `up-chimp/` inside this repository** (Aries model).
   Rationale: the writer/reader encode CHIMP's private contracts (DDL/PDL
   syntax, YAML plan format); co-evolution in one repo/CI prevents version
   drift. Extraction later is mechanical if ever needed.
2. **Sequencing: integrate now (provisional)** — the file-based contract
   (CLI + file formats) is language-agnostic, so the integration does not
   foreclose a possible language port, which may be scoped/planned in
   parallel on separate branches. Nothing in M16 becomes throwaway under a
   port; revisit after port scoping.
3. **Plan times: earliest start/end + epsilon correction** (UP's
   `correct_plan_generation_result`), feasibility checked by
   `TimeTriggeredPlanValidator` in the test suite. `STNPlan` output is a
   deferred, additive option. Dispatched schedules (via CHIMP's scheduler)
   rejected for now — would require Java-side CLI changes.
4. **Registration: `up.ini` snippet now, upstream `DEFAULT_ENGINES` PR as
   M16.5, no import-time auto-registration** (fragile; Aries does not do it
   either). The `up.ini` stopgap remains the documented fallback if upstream
   stalls.
