CHIMP (Conflict-driven Hierarchical Meta-CSP Planner)
=====================================================

Summary
-------

CHIMP is a hierarchical hybrid planner. It combines HTN-Planning and meta-constraint reasoning and is based on the Meta-CSP Framework. Currently, it supports causal, temporal and resource knowledge as well as knowledge that is provided by an external path planner.

CHIMP aims at enabling a mobile robot to use different forms of knowledge in its planning process. Furthermore, it can produce plans that can be executed in parallel, and additional goal tasks can be inserted into an existing plan during the execution of the plan. For details please see our [paper at IROS 2015][iros-paper].

[iros-paper]: http://aass.oru.se/~mmi/papers/iros15-chimp.pdf


Documentation
-------------

CHIMP can be built through [Gradle](http://gradle.org).
The `clean` target will clean up the build directory. The target `javadoc` can be used to generate the API documentation (Javadoc), which will be placed in build/docs/javadoc.
To import CHIMP into eclipse you can use the target `eclipse` which will generate `.classpath`, `.settings` and `.project` files and directories.

### Examples ###

To test CHIMP you can try different example domains in the package examples/chimp. TestRACEDomain.java uses a domain description from the robot waiter scenario from the [RACE project](http://project-race.eu/). It creates a plan for serving a hot coffee with milk and sugar, i.e., all objects need to be served fast enough so that the coffee will not get cold.

CHIMP needs a domain and a problem description in order to generate a plan. The domain contains the HTN-methods and operators and the problem contains the initial state of the world and the goal tasks.

Other examples how to use CHIMP can be found in the packages `examples` and `examples.chimp`.

### Usage ###

You can start CHIMP from the command line with `gradle run` followed by arguments for the domain file and the problem file, e.g.:

```
./gradlew run -Dexec.args="domains/ordered_domain.ddl problems/test_m_serve_coffee_problem_1.pdl"
```

The output of CHIMP's `--help` command is shown below:
```
Usage: chimp [-hV] [--guess-ordering] [--htn-unification] [--print-stats]
             [-e=<esterelOutputFile>] [--horizon=<horizon>] [-o=<outputFile>]
             <domainFile> <problemFile>
Plan with CHIMP.
      <domainFile>          The file containing the planning domain.
      <problemFile>         The file containing the planning problem.
  -e, --esterel=<esterelOutputFile>
                            Write esterel graph to this output file.
      --guess-ordering      Indicates whether the GuessOrderingMetaConstraint
                              shall be used. (Default: false)
  -h, --help                Show this help message and exit.
      --horizon=<horizon>   Horizon for the temporal variables. (Default:
                              600000)
      --htn-unification     Try to unify tasks with existing ones during
                              HTN-planning. (Default: false)
  -o, --output=<outputFile> Write the plan to this output file.
      --print-stats         Print statistics. (Default: false)
  -V, --version             Print version information and exit.

```




