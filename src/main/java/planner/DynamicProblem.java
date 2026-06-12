package planner;

import chimp.model.FluentSignature;
import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** CHIMP problem assembled programmatically rather than parsed from PDL. */
public class DynamicProblem implements CHIMPProblem {
    private final Set<String> argumentSymbols = new LinkedHashSet<>();
    private final Map<String, List<String>> typeInstances = new LinkedHashMap<>();
    private final List<InitialFluentDefinition> initialFluents = new ArrayList<>();
    private final List<InitialTaskDefinition> initialTasks = new ArrayList<>();

    public void addArgumentSymbol(String symbol) {
        argumentSymbols.add(symbol);
    }

    public void setTypesInstances(String type, String[] instances) {
        typeInstances.put(type, List.of(instances));
    }

    public void addState(String predicate, String[] args) {
        initialFluents.add(new InitialFluentDefinition(
                "state" + initialFluents.size(),
                new FluentSignature(predicate, args),
                List.of()));
        argumentSymbols.addAll(List.of(args));
    }

    public void addTask(String predicate, String[] args) {
        initialTasks.add(new InitialTaskDefinition(
                "task" + initialTasks.size(),
                new FluentSignature(predicate, args)));
        argumentSymbols.addAll(List.of(args));
    }

    @Override
    public String[] getArgumentSymbols() {
        return argumentSymbols.toArray(String[]::new);
    }

    @Override
    public void addArgumentSymbols(Collection<String> symbols) {
        argumentSymbols.addAll(symbols);
    }

    @Override
    public Map<String, String[]> getTypesInstancesMap() {
        Map<String, String[]> result = new LinkedHashMap<>();
        typeInstances.forEach((type, instances) -> result.put(type, instances.toArray(String[]::new)));
        return result;
    }

    @Override
    public ProblemDefinition getProblemDefinition() {
        return new ProblemDefinition(
                List.copyOf(argumentSymbols),
                typeInstances,
                initialFluents,
                initialTasks,
                List.of(),
                List.of());
    }
}
