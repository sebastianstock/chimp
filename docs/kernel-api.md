# CHIMP-Native Solver Kernel API

## Overview

The kernel (`chimp.kernel` package) replaces the Meta-CSP storage layer with
CHIMP-owned constraint solving. It is a **pure additive** implementation —
no Meta-CSP imports, no framework inheritance.

### Package structure

```
chimp.kernel/
├── Variable.java              # Base variable type
├── Constraint.java            # Base constraint type
├── ConstraintNetwork.java     # Network container (reference equality)
├── ConstraintSolver.java      # Abstract solver with variable/constraint bookkeeping
├── VariablePrototype.java     # Placeholder for unbound resolver variables
│
├── temporal/                  # Temporal solver (STN + Allen relations)
│   ├── SimpleTemporalNetwork.java
│   ├── TemporalVariable.java
│   ├── TemporalConstraint.java
│   ├── TemporalSolver.java
│   └── DistanceEdge.java
│
├── symbolic/                  # Symbolic solver (AC-3)
│   ├── SymbolicVariable.java
│   ├── SymbolicDomain.java
│   ├── SymbolicConstraint.java
│   ├── SymbolicSolver.java
│   ├── CompoundSymbolicVariable.java
│   ├── CompoundSymbolicConstraint.java
│   ├── CompoundSymbolicSolver.java
│   └── RoutedConstraint.java
│
├── integer/                   # Integer solver (Choco-backed)
│   ├── IntegerVariable.java
│   ├── IntegerDomain.java
│   ├── IntegerConstraint.java
│   └── IntegerSolver.java
│
└── fluent/                    # Fluent solver (composes temporal + symbolic + integer)
    ├── Fluent.java
    ├── FluentConstraint.java
    └── FluentNetworkSolver.java
```

### Design principles

1. **Reference equality**: `ConstraintNetwork` uses identity-based equality
   (`==`/`hashCode` from `Object`). This avoids JUNG duplicate-edge bugs
   when the same constraint values appear at different search-tree nodes.

2. **Delegated propagation**: Sub-solvers propagate independently on
   `addConstraint`. The `FluentNetworkSolver` orchestrates sub-constraint
   compilation and routing.

3. **Incremental propagation**: The temporal solver uses incremental
   Floyd-Warshall O(n²) for edge tightenings, with fallback to full O(n³)
   when time points are added or constraints are removed.

---

## Base Types (`chimp.kernel`)

### Variable

```java
public abstract class Variable {
    public Variable(ConstraintSolver solver, int id);
    public int id();
    public ConstraintSolver solver();
    public int marking();
    public void setMarking(int marking);
    public int getID();             // legacy, use id()
    public int getMarking();        // legacy, use marking()
}
```

Each variable is owned by a `ConstraintSolver` and has a unique integer `id`.
The `marking` is an application-specific tag (e.g., OPEN/CLOSED/UNPLANNED).

### Constraint

```java
public abstract class Constraint {
    public void setScope(Variable[] scope);
    public Variable[] scope();
    public Variable from();          // scope[0]
    public Variable to();            // scope[1] (or null for unary)
    public void setFrom(Variable v); // scope[0] = v
    public void setTo(Variable v);   // scope[1] = v

    public abstract Constraint clone();
    public abstract String getEdgeLabel();
    public abstract boolean isEquivalent(Constraint other);
}
```

Binary constraints have `from`/`to` convenience accessors. Unary constraints
have `from == to`.

### ConstraintNetwork

```java
public class ConstraintNetwork {
    public ConstraintNetwork(VariablePrototype[] prototypes);
    public void add(Constraint constraint);
    public void remove(Constraint constraint);
    public void addVariables(Variable[] variables);
    public Variable[] getVariables();
    public Constraint[] getConstraints();
    public void setName(String name);
    public String getName();

    // Reference equality: equals() == (this == other)
    // hashCode() from Object
}
```

A container for variables and constraints. Uses **reference equality** —
two networks compare equal only if they are the same object instance.

### ConstraintSolver

```java
public abstract class ConstraintSolver {
    // Variable management
    protected int nextVariableId();
    protected void registerVariable(Variable v);
    protected void unregisterVariable(Variable v);
    public Variable[] getVariables();
    public Variable getVariable(int id);

    // Constraint management
    public boolean addConstraint(Constraint c);
    public boolean addConstraints(Constraint[] cs);
    public void removeConstraint(Constraint c);
    public void removeConstraints(Constraint[] cs);
    public Constraint[] getConstraints();
    public List<Constraint> constraintList();  // direct list access

    // Subclasses implement
    public abstract boolean propagate();
}
```

Base class for all solvers. `addConstraint` auto-propagation is
overridable by subclasses. `constraintList()` provides direct access
for compound solvers that batch sub-constraint routing.

### VariablePrototype

```java
public class VariablePrototype extends Variable {
    public VariablePrototype(int id);
    public String getType();          // fluent type name if set
    public void setType(String type);
    public int[] getConnections();    // argument connections if set
    public void setConnections(int[] connections);
}
```

A placeholder used during resolver building, before the actual fluent
variables are materialized. Migrated from the Meta-CSP `VariablePrototype`.

---

## Temporal Solver (`chimp.kernel.temporal`)

### SimpleTemporalNetwork

```java
public class SimpleTemporalNetwork {
    public SimpleTemporalNetwork(long horizon, int maxTimePoints);

    // Time point management
    public int addTimePoint();
    public int timePointCount();
    public long horizon();

    // Constraint management
    public boolean addConstraint(int from, int to, long min, long max);
    public boolean removeConstraint(int from, int to, long min, long max);
    public void removeConstraintSilent(int from, int to, long min, long max);
    public boolean rebuild();

    // Batch operations
    public void addEdgeSilent(int from, int to, long bound);
    public void recordConstraint(DistanceEdge edge);

    // Propagation
    public boolean propagate();

    // Queries
    public TemporalBounds getBounds(int from, int to);
    public long getMinDistance(int from, int to);
    public long getMaxDistance(int from, int to);

    static final long INF = TemporalBounds.INFINITY;
}
```

The STN is a distance graph over time points. Time point 0 is the origin
(value 0). All non-origin time points are bounded to `[0, horizon]`.

**Propagation strategy**:
- **Incremental** (default): When `addEdgeSilent` tightens an edge, the
  next `propagate()` call runs incremental O(n²) — only paths through
  tightened edges are recomputed.
- **Full Floyd-Warshall**: When new time points are added or constraints
  are removed, `propagate()` runs full O(n³).

Use `addEdgeSilent` + `propagate()` for batches: add all edges silently,
then propagate once.

### TemporalVariable

```java
public class TemporalVariable extends Variable {
    public TemporalVariable(TemporalSolver solver, int id, SimpleTemporalNetwork stn);

    // Time point indices in the STN
    public int start();    // getStart()
    public int end();      // getEnd()

    // Temporal bounds
    public long getEST();  // Earliest start time = -distance[start][0]
    public long getLST();  // Latest start time   = distance[0][start]
    public long getEET();  // Earliest end time   = -distance[end][0]
    public long getLET();  // Latest end time     = distance[0][end]

    public TemporalBounds getBoundsBetween(int fromTp, int toTp);
}
```

An interval with `start` and `end` time points. The constructor adds
an implicit `Duration[0, INF]` constraint (start ≤ end) via `addEdgeSilent`.

### TemporalConstraint

```java
public class TemporalConstraint extends Constraint {
    public TemporalConstraint(TemporalRelation relation, TemporalBounds... bounds);
    public TemporalConstraint(TemporalRelation relation);

    public TemporalRelation relation();

    // Compiles to STN distance edges
    public List<DistanceEdge> compileToDistanceConstraints();
}
```

Represents an Allen interval relation between two `TemporalVariable`s.
The `compileToDistanceConstraints` method expands the relation into
STN distance edges (typically 2–4 edges per constraint).

### DistanceEdge

```java
public record DistanceEdge(int fromTimePoint, int toTimePoint, long min, long max) {
    public int fromTimePoint();
    public int toTimePoint();
    public long min();
    public long max();
}
```

An STN distance constraint: `t_to - t_from ∈ [min, max]`.

### TemporalSolver

```java
public class TemporalSolver extends ConstraintSolver {
    public TemporalSolver(long origin, long horizon, int maxTimePoints);
    public TemporalSolver(long origin, long horizon);

    public SimpleTemporalNetwork stn();
    public long origin();
    public long horizon();

    public TemporalVariable[] createVariables(int num);
    public TemporalBounds getAdmissibleDistanceBounds(int fromTp, int toTp);

    @Override public boolean addConstraint(Constraint c);
    @Override public void removeConstraint(Constraint c);
    @Override public boolean propagate();
}
```

Manages `TemporalVariable`s and `TemporalConstraint`s. `addConstraint`
batches the STN edges (via `addEdgeSilent`) and propagates once.

### TemporalRelation

All ~35 Allen interval relations are enumerated in
`chimp.model.TemporalRelation`.

---

## Symbolic Solver (`chimp.kernel.symbolic`)

### SymbolicVariable

```java
public class SymbolicVariable extends Variable {
    public SymbolicVariable(ConstraintSolver solver, int id, int symbolCount);

    public SymbolicDomain domain();
    public SymbolicDomain getDomain();

    public void setConstant(String symbol);
    public String[] possibleSymbols();
    public boolean isGround();
    public boolean hasModel();

    public String toString();
    public String toStringShort();
}
```

A named variable whose domain is a subset of the solver's symbol table.
`setConstant` permanently grounds the variable to a single symbol.
`possibleSymbols` returns the symbols currently in the domain.

### SymbolicDomain

```java
public final class SymbolicDomain {
    public SymbolicVariable variable();

    public int[] possibleIndices();
    public boolean hasModel();
    public boolean isGround();

    // AC-3 revision operations
    boolean setUnaryEquals(int[] unaryValues);
    boolean setUnaryDifferent(int[] unaryValues);
    int applyEqualsConstr(SymbolicDomain to);
    int applyDifferentConstr(SymbolicDomain to);

    void reset();
    void copyFrom(SymbolicDomain other);
}
```

Maintains a boolean array of possible symbols. The AC-3 revision
operations return: 0 (no change), 1 (from changed), 2 (to changed),
or 3 (both changed).

### SymbolicConstraint

```java
public class SymbolicConstraint extends Constraint {
    public enum Type { EQUALS, DIFFERENT, UNARYEQUALS, UNARYDIFFERENT }

    public SymbolicConstraint(Type type);
    public Type type();
    public void setUnaryValues(int[] unaryValues);
    public int[] getUnaryValues();

    @Override public SymbolicConstraint clone();
    @Override public String getEdgeLabel();
    @Override public boolean isEquivalent(Constraint other);
}
```

### SymbolicSolver

```java
public class SymbolicSolver extends ConstraintSolver {
    public SymbolicSolver(String[] symbols);

    public SymbolicVariable[] createVariables(int num);
    public String[] getSymbols();
    public int indexOfSymbol(String symbol);
    public int[] indexArrayOfSymbols(String[] symbols);

    @Override public boolean propagate();  // AC-3
}
```

Parameterized by a fixed set of symbols. Propagation uses AC-3
arc-consistency with backup/restore on failure.

### CompoundSymbolicVariable

```java
public class CompoundSymbolicVariable extends Variable {
    public CompoundSymbolicVariable(ConstraintSolver solver, int id,
                                     SymbolicVariable[] internalVars);

    public SymbolicVariable[] getInternalVariables();

    // Name setting
    public void setName(String type, String... arguments);
    public void setFullName(String name);  // e.g., "On(mug1 table1)"

    // Name retrieval
    public String getName();       // "On(mug1 table1)"
    public String getShortName();  // "On(mug1 {...})"
    public String[] getGroundArgs();
    public String[] getArgs();
    public String getPredicateName();
    public String[] getPossiblePredicateNames();

    // Argument queries
    public String[] getSymbolsAt(int position);
    public String getGroundSymbolAt(int position);

    // Matching queries
    public boolean possibleMatch(String fluentType, String[] arguments);
    public boolean possibleArgumentsMatch(CompoundSymbolicVariable other, int[] connections);
    public boolean possibleArgumentMatch(CompoundSymbolicVariable other, int from, int to);
    public boolean possibleArgumentDifferent(CompoundSymbolicVariable other, int fromId, int toId);

    static final String NONESYMBOL = "n";
}
```

A predicate with arguments. Internal variables are distributed across
the sub-solvers of a `CompoundSymbolicSolver`. The first internal
variable holds the predicate name.

### CompoundSymbolicConstraint

```java
public class CompoundSymbolicConstraint extends Constraint {
    public enum Type {
        MATCHES, SUBMATCHES, SUBDIFFERENT,
        POSITIVEVALUERESTRICTION, NEGATIVEVALUERESTRICTION
    }

    public CompoundSymbolicConstraint(Type type);
    public CompoundSymbolicConstraint(Type type, int[] connections);
    public CompoundSymbolicConstraint(Type type, int[] indices, String[][] restrictions);

    public Type type();
    public List<RoutedConstraint> createInternalConstraints(Variable f, Variable t);
    List<RoutedConstraint> getRoutedConstraints();
}
```

Generates sub-constraints between internal variables of two
`CompoundSymbolicVariable`s. The `RoutedConstraint` pairs each
`SymbolicConstraint` with the index of the sub-solver that should
receive it.

### RoutedConstraint

```java
public record RoutedConstraint(SymbolicConstraint constraint, int solverIndex) {}
```

### CompoundSymbolicSolver

```java
public class CompoundSymbolicSolver extends ConstraintSolver {
    public CompoundSymbolicSolver(String[][] symbols, int[] ingredients);

    public SymbolicSolver[] getSubSolvers();
    public SymbolicSolver subSolver(int index);
    public int[] getIngredients();
    public int[] getVarIndexToSolverIndex();

    public CompoundSymbolicVariable[] createVariables(int num);

    @Override public boolean addConstraint(Constraint c);
    @Override public void removeConstraint(Constraint c);
    @Override public boolean propagate();

    public void propagateAllSub();
    public void propagateAllSubFull();  // legacy alias
}
```

Composes multiple `SymbolicSolver`s — one per argument position.
When a `CompoundSymbolicConstraint` is added, it generates sub-constraints
and routes each to the appropriate sub-solver. On propagation failure,
all added sub-constraints are rolled back.

---

## Integer Solver (`chimp.kernel.integer`)

### IntegerVariable

```java
public class IntegerVariable extends Variable {
    public IntegerVariable(ConstraintSolver cs, int id);

    public IntegerDomain getDomain();
    public void setDomain(IntegerDomain d);
    public void setValue(int value);
    public void setConstantValue(int value);
    public boolean isInstantiated();
    public boolean isConstant();

    public int compareTo(Variable variable);
}
```

### IntegerDomain

```java
public class IntegerDomain {
    public void setValue(int value);
    public int getValue();
    public void setConstantValue(int value);
    public boolean isConstant();
    public boolean isInstantiated();
    public void uninstantiate();
}
```

### IntegerConstraint

```java
public class IntegerConstraint extends Constraint {
    public enum Type { ARITHM, ALLDIFFERENT, ALLEQUAL }

    public IntegerConstraint(Type type, Variable[] scope, String op1, String op2, int cste);
    public IntegerConstraint(Type type, Variable[] scope);
    public IntegerConstraint(Type type, Variable[] scope, String op1, int cste);
    public IntegerConstraint(Type type, Variable[] scope, String op1, String op2);

    public Type getType();
    public String getOp1();
    public String getOp2();
    public int getCste();
}
```

### IntegerSolver

```java
public class IntegerSolver extends ConstraintSolver {
    public IntegerSolver(int minIntValue, int maxIntValue);

    public IntegerVariable[] createVariables(int num);

    @Override public boolean propagate();  // Choco-based
}
```

Backed by the Choco constraint solver. Propagation rebuilds the Choco
model from scratch on each `propagate()` call.

---

## Fluent Solver (`chimp.kernel.fluent`)

### Fluent

```java
public class Fluent extends Variable {
    public Fluent(ConstraintSolver solver, int id,
                   CompoundSymbolicVariable symbolicVar,
                   TemporalVariable temporalVar,
                   IntegerVariable[] integerVars);

    public CompoundSymbolicVariable getCompoundSymbolicVariable();
    public TemporalVariable getTemporalVariable();
    public IntegerVariable[] getIntegerVariables();

    public void setName(String name);             // "On(mug1 table1)"
    public void setName(String type, String[] arguments);
}
```

Composes one symbolic, one temporal, and N integer variables into
a single planning entity. Fluent is the variable type that search
engine and meta-constraints operate on.

### FluentConstraint

```java
public class FluentConstraint extends Constraint {
    public enum Type {
        MATCHES, DC, PRE, OPENS, CLOSES, BEFORE,
        UNARYAPPLIED, MOVEDURATION, AXIOM, RESOURCEUSAGE
    }

    // Constructors
    public FluentConstraint(Type type);
    public FluentConstraint(Type type, int[] connections);
    public FluentConstraint(Type type, PlanReportroryItem plannedWith);
    public FluentConstraint(Type type, TemporalBounds bounds);
    public FluentConstraint(Type type, String axiom);
    public FluentConstraint(Type type, ResourceUsageTemplate resourceIndicator);

    public Type getType();
    public TemporalBounds getBounds();

    // Compiles to sub-constraints dispatched to sub-solvers
    public Constraint[] compileToSubConstraints(Variable f, Variable t);
}
```

Each type compiles to specific sub-constraints:

| Type | Temporal sub-constraint | Symbolic sub-constraint |
|------|------------------------|------------------------|
| `MATCHES` | `Equals` | `MATCHES` |
| `DC` | `DuringOrEqualsOrStartsOrFinishes` | — |
| `PRE` | `MeetsOrOverlapsOrFinishedByOrContains` | — |
| `OPENS` | `Overlaps` | — |
| `CLOSES` | `OverlappedBy` | — |
| `MOVEDURATION` | `Duration(bounds)` | — |

### FluentNetworkSolver

```java
public class FluentNetworkSolver extends ConstraintSolver {
    public FluentNetworkSolver(long origin, long horizon,
                               String[][] symbols, int[] symbolicingredients);
    public FluentNetworkSolver(long origin, long horizon,
                               String[][] symbols, int[] symbolicingredients,
                               int minIntValue, int maxIntValue, int numIntVars);

    // Fluent creation
    public Fluent[] createVariables(int num);

    // Constraint management
    @Override public boolean addConstraint(Constraint c);
    @Override public boolean propagate();  // delegated

    // Queries
    public Fluent[] getOpenFluents();
    public Fluent[] getOpenFluents(TemporalVariable taskTemporal);
    public List<Constraint> getConstraintsTo(Variable to);
    public List<FluentConstraint> getFluentConstraintsOfTypeTo(Variable to, FluentConstraint.Type type);
    public List<FluentConstraint> getFluentConstraintsOfTypeFrom(Variable from, FluentConstraint.Type type);

    // Accessors
    public CompoundSymbolicSolver getSymbolicSolver();
    public TemporalSolver getTemporalSolver();
    public IntegerSolver getIntegerSolver();
    public long getOrigin();
    public long getHorizon();
    public boolean hasIntegerConstraintSolver();
    public int getNumIntVars();
}
```

The top-level solver that composes all three sub-solvers. When a
`FluentConstraint` is added, it is compiled to sub-constraints
(temporal, symbolic, integer) which are routed to the appropriate
sub-solver.

---

## Performance Characteristics

### Temporal Solver

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| Variable creation (per var) | O(n) | Adds 2 time points with origin bounds |
| Initial propagation | O(n³) | One-time full Floyd-Warshall |
| Constraint addition (incremental) | O(k·n²) | k = edges per constraint (~2–4) |
| Constraint removal | O(n³) | Rebuilds from scratch |
| Bound query | O(1) | Direct matrix lookup |

For 100 variables (~200 time points):
- Initial propagation: ~22ms
- Incremental per constraint: ~0.4ms (vs ~4ms with full FW)
- Total for 50 constraints: ~40ms (vs ~207ms with full FW)

### Symbolic Solver

| Operation | Notes |
|-----------|-------|
| Variable creation (per var) | O(symbols) — domain initialization |
| AC-3 propagation | Depends on constraint density |
| Unary constraint | O(symbols) |
| Binary constraint | O(symbols) |

### Integer Solver

| Operation | Notes |
|-----------|-------|
| Propagation | Full Choco model rebuild per `propagate()` |
| ARITHM constraint | Fast — single constraint post |
| ALLDIFFERENT / ALLEQUAL | Fast — single constraint post |

### Compound Symbolic Solver

| Operation | Notes |
|-----------|-------|
| Variable creation | O(ingredients) per sub-solver |
| MATCHES constraint | N sub-constraints (N = internal vars) |
| SUBMATCHES | O(connections) sub-constraints |

---

## Migration Guide: Meta-CSP → CHIMP-Native

### Temporal: APSPSolver → SimpleTemporalNetwork / TemporalSolver

| Meta-CSP | CHIMP-Native |
|----------|-------------|
| `APSPSolver` | `SimpleTemporalNetwork` |
| `AllenIntervalNetworkSolver` | `TemporalSolver` |
| `AllenInterval` | `TemporalVariable` |
| `AllenIntervalConstraint` | `TemporalConstraint` + `TemporalRelation` |
| `TimePoint` | `int` index |
| `Solver.getAdmissibleDistanceBounds(tp1, tp2)` | `stn.getBounds(fromTP, toTP)` |
| `Bounds(min, max)` | `TemporalBounds(min, max)` |

**Key differences**:
- Time points are integer indices, not objects
- `TemporalBounds` is a record, `Bounds` is a mutable class
- No explicit `propagate()` needed per constraint (auto-incremental)

### Symbolic: NameMatching → SymbolicSolver

| Meta-CSP | CHIMP-Native |
|----------|-------------|
| `NameMatchingConstraintSolver` | `SymbolicSolver` |
| `NameVariable` | `SymbolicVariable` |
| `NameMatchingConstraint` | `SymbolicConstraint` |
| `NameDomain` | `SymbolicDomain` |
| `CompoundSymbolicVariableConstraintSolver` | `CompoundSymbolicSolver` |
| `CompoundSymbolicVariable` | `CompoundSymbolicVariable` |
| `CompoundSymbolicValueConstraint` | `CompoundSymbolicConstraint` |

**Key differences**:
- `SymbolicSolver.addConstraint` auto-propagates (AC-3) and rolls back on failure
- Domain backup/restore is internal (callers don't need to manage it)
- Use `setConstant` for permanent grounding, constraints for temporary

### Integer: IntegerConstraintSolver → IntegerSolver

| Meta-CSP | CHIMP-Native |
|----------|-------------|
| `IntegerConstraintSolver` | `IntegerSolver` |
| `IntegerVariable` | `IntegerVariable` |
| `IntegerConstraint` | `IntegerConstraint` |

**Key differences**:
- API is essentially identical (same Choco backend)
- `IntegerSolver` is a standalone solver (not tied to FluentNetworkSolver)

### Fluent: FluentNetworkSolver → FluentNetworkSolver

| Meta-CSP | CHIMP-Native |
|----------|-------------|
| `fluentSolver.FluentNetworkSolver` | `chimp.kernel.fluent.FluentNetworkSolver` |
| `fluentSolver.Fluent` | `chimp.kernel.fluent.Fluent` |
| `fluentSolver.FluentConstraint` | `chimp.kernel.fluent.FluentConstraint` |

**Key differences**:
- No Meta-CSP framework inheritance
- `FluentConstraint.compileToSubConstraints` returns `Constraint[]` instead of
  directly manipulating the Meta-CSP solver
- Constraint types use `chimp.kernel` types internally
- `FluentNetworkSolver` is a standalone top-level solver, not a `MultiConstraintSolver`

---

## Common Patterns

### Creating a temporal network

```java
TemporalSolver solver = new TemporalSolver(ORIGIN, HORIZON, maxTimePoints);
TemporalVariable[] vars = solver.createVariables(5);

TemporalConstraint c = new TemporalConstraint(
    TemporalRelation.Before,
    new TemporalBounds(10, 100));
c.setFrom(vars[0]);
c.setTo(vars[1]);
solver.addConstraint(c);

long est = vars[0].getEST();
long lst = vars[0].getLST();
```

### Matching symbolic variables

```java
String[][] symbols = {{"On", "Under"}, {"a", "b", "none"}, {"x", "y", "none"}};
int[] ingredients = {1, 1, 1};
CompoundSymbolicSolver solver = new CompoundSymbolicSolver(symbols, ingredients);

CompoundSymbolicVariable[] vars = solver.createVariables(2);
vars[0].setFullName("On(a x)");
// vars[1] is unbound: On(? ?)

CompoundSymbolicConstraint match = new CompoundSymbolicConstraint(
    CompoundSymbolicConstraint.Type.MATCHES);
match.setScope(new Variable[]{vars[0], vars[1]});
solver.addConstraint(match);

assertEquals("On(a x)", vars[1].getName());
```

### Fluent constraint composition

```java
FluentNetworkSolver planner = new FluentNetworkSolver(
    0, 100_000, symbols, ingredients);

Fluent[] fluents = planner.createVariables(2);
fluents[0].setName("on(mug1 loc1 none none)");
fluents[1].setName("on(?mug loc1 none none)");

FluentConstraint matches = new FluentConstraint(FluentConstraint.Type.MATCHES);
matches.setScope(new Variable[]{fluents[0], fluents[1]});
planner.addConstraint(matches);

// fluent[1] is now ground to "on(mug1 loc1 none none)"
```

### Adding temporal ordering on fluents

```java
FluentConstraint pre = new FluentConstraint(FluentConstraint.Type.PRE);
pre.setScope(new Variable[]{task, precondition});
solver.addConstraint(pre);

FluentConstraint opens = new FluentConstraint(FluentConstraint.Type.OPENS);
opens.setScope(new Variable[]{task, effect});
solver.addConstraint(opens);
```

---

## Known Limitations

1. **Constraint removal with integer solver**: The `IntegerSolver` doesn't
   support incremental constraint removal — Choco model is fully rebuilt.

2. **PRE + CLOSES on same fluent**: Adding both PRE and CLOSES constraints
   between the same pair creates incompatible Allen relations
   (`MeetsOrOverlapsOrFinishedByOrContains` vs `OverlappedBy`). Use
   additional constraint templates to combine preconditions and delete
   effects on the same fluent.

3. **No incremental constraint removal in STN**: Removal triggers full
   Floyd-Warshall rebuild. Use batched `removeConstraintSilent` +
   `rebuild()` for multiple removals.

4. **Integer solver uses Choco.findSolution()**: Only finds one solution
   per `propagate()` call. Does not maintain full domain-state like
   AC-3 for symbolic domains.

---

## Related Tests

| Test file | What it validates |
|-----------|-------------------|
| `temporal/AllRelationTypesTest` | All 35 Allen relation types |
| `fluent/KernelIntegrationTest` | End-to-end integration scenarios |

Cross-validation suites against the Meta-CSP framework were used during the
kernel migration and have been removed now that the Meta-CSP dependency is
gone.
