CHIMP (Conflict-driven Hierarchical Hybrid Planner)
===================================================

Summary
-------

CHIMP is a hierarchical hybrid planner. It combines HTN-Planning and meta-constraint reasoning using a CHIMP-native solver kernel. Currently, it supports causal, temporal and resource knowledge as well as knowledge that is provided by an external path planner.

CHIMP aims at enabling a mobile robot to use different forms of knowledge in its planning process. Furthermore, it can produce plans that can be executed in parallel, and additional goal tasks can be inserted into an existing plan during the execution of the plan. For details please see our [paper at IROS 2015][iros-paper].

[iros-paper]: http://aass.oru.se/~mmi/papers/iros15-chimp.pdf


Documentation
-------------

Additional documentation lives in `docs/`:

- `docs/kernel-api.md` — the CHIMP-native solver kernel (`chimp.kernel`): base types,
  temporal (STN), symbolic (AC-3), integer (Choco-backed) and fluent solvers.
- `docs/metacsp-decoupling-plan.md` — how the Meta-CSP dependency was removed
  (milestones, defects found and fixed, verification strategy against the
  `legacy-metacsp` reference branch).
- `docs/stn-performance-improvements.html` — measured STN performance report.

CHIMP can be built through [Gradle](http://gradle.org).
The `clean` target will clean up the build directory. The target `javadoc` can be used to generate the API documentation (Javadoc), which will be placed in build/docs/javadoc.
To import CHIMP into eclipse you can use the target `eclipse` which will generate `.classpath`, `.settings` and `.project` files and directories.

Run the test suite with `./gradlew test`. The 93-problem benchmark against the
`legacy-metacsp` reference branch is `./scripts/compare-benchmark.sh 3`
(see the decoupling plan for how to read its output).

### Examples ###

To test CHIMP you can try the command-line runner (`examples.ChimpIO`) with a domain and problem description. The domain contains the HTN-methods and operators and the problem contains the initial state of the world and the goal tasks.

An example domain for serving coffee in the robot waiter scenario from the [RACE project](http://project-race.eu/) is provided under `domains/ordered_domain.ddl`.

### Usage ###

You can start CHIMP from the command line with `gradle run` followed by arguments for the domain file and the problem file, e.g.:

```
./gradlew run -Dexec.args="domains/ordered_domain.ddl problems/test_m_serve_coffee_problem_1.pdl"
```

The output of CHIMP's `--help` command is shown below:
```
Usage: chimp [-hV] [--guess-ordering] [--htn-unification] [--print-stats]
             [--horizon=<horizon>] [--hpr=<hprOutputFile>]
             [-o=<outputFile>] <domainFile> <problemFile>
Plan with CHIMP.
      <domainFile>          The file containing the planning domain.
      <problemFile>         The file containing the planning problem.
      --guess-ordering      Indicates whether the GuessOrderingMetaConstraint
                              shall be used. (Default: false)
  -h, --help                Show this help message and exit.
      --horizon=<horizon>   Horizon for the temporal variables. (Default:
                              3600000)
      --hpr=<hprOutputFile> Write HPR graph to this output file.
      --htn-unification     Try to unify tasks with existing ones during
                              HTN-planning. (Default: false)
  -o, --output=<outputFile> Write the plan to this output file.
      --print-stats         Print statistics. (Default: false)
  -V, --version             Print version information and exit.

```




