package hybridDomainParsing;

import chimp.model.FluentSignature;
import chimp.model.InitialFluentDefinition;
import chimp.model.InitialTaskDefinition;
import chimp.model.ProblemDefinition;
import chimp.model.ProblemOrderingEdge;
import chimp.model.ProblemTemporalConstraint;
import chimp.model.TemporalBounds;
import chimp.model.TemporalConstraintTemplate;
import chimp.model.TemporalRelation;
import planner.CHIMPProblem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ProblemParser implements CHIMPProblem {
    private static final Logger LOGGER = Logger.getLogger(ProblemParser.class.getName());
    public static final String PROBLEM_KEYWORD = "Problem";
    public static final String FLUENT_KEYWORD = "Fluent";
    public static final String TASK_KEYWORD = "Task";
    private static final String ARGUMENT_SYMBOLS_KEYWORD = "ArgumentSymbols";
    private static final String INSTANCES_KEYWORD = "Instances";
    private static final String N = "n";

    private final String fileName;
    private final Set<String> additionalArgumentSymbols = new LinkedHashSet<>();
    private ProblemDefinition problemDefinition;

    public ProblemParser(String fileName) {
        this.fileName = fileName;
        parseProblem();
    }

    private void parseProblem() {
        String everything = readProblem();
        List<String> argumentSymbols = new ArrayList<>();
        Map<String, List<String>> typeInstances = new LinkedHashMap<>();
        List<InitialFluentDefinition> fluents = new ArrayList<>();
        List<InitialTaskDefinition> tasks = new ArrayList<>();
        List<ProblemTemporalConstraint> temporalConstraints = new ArrayList<>();
        List<ProblemOrderingEdge> orderingEdges = new ArrayList<>();

        for (String problem : inSourceOrder(HybridDomain.parseKeyword(PROBLEM_KEYWORD, everything))) {
            argumentSymbols.addAll(parseArgumentSymbols(problem));
            typeInstances.putAll(parseTypesInstancesMap(problem));
            for (String fluent : inSourceOrder(HybridDomain.parseKeyword(FLUENT_KEYWORD, problem))) {
                fluents.add(parseFluent(fluent));
            }
            for (String task : inSourceOrder(HybridDomain.parseKeyword(TASK_KEYWORD, problem))) {
                tasks.add(parseTask(task));
            }
            for (String constraint : inSourceOrder(
                    HybridDomain.parseKeyword(HybridDomain.CONSTRAINT_KEYWORD, problem))) {
                temporalConstraints.add(parseTemporalConstraint(constraint));
            }
            for (String ordering : inSourceOrder(
                    HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, problem))) {
                orderingEdges.add(parseOrderingEdge(ordering));
            }
        }
        problemDefinition = new ProblemDefinition(
                distinct(argumentSymbols), typeInstances, fluents, tasks, temporalConstraints, orderingEdges);
    }

    private String readProblem() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("#")) {
                    content.append(line).append('\n');
                }
            }
            return content.toString();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Problem file not found: " + fileName, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read problem: " + fileName, e);
        }
    }

    private InitialFluentDefinition parseFluent(String declaration) {
        String id = declaration.substring(0, declaration.indexOf(' ')).trim();
        ParsedSignature parsed = parseSignature(declaration.substring(declaration.indexOf(' ')).trim(), true);
        return new InitialFluentDefinition(id, parsed.signature(), parsed.integerConstants());
    }

    private InitialTaskDefinition parseTask(String declaration) {
        String id = declaration.substring(0, declaration.indexOf(' ')).trim();
        ParsedSignature parsed = parseSignature(declaration.substring(declaration.indexOf(' ')).trim(), false);
        return new InitialTaskDefinition(id, parsed.signature());
    }

    private ParsedSignature parseSignature(String value, boolean parseIntegers) {
        int open = value.indexOf('(');
        int close = value.indexOf(')', open);
        String predicate = value.substring(0, open).trim();
        String argumentsText = value.substring(open + 1, close).trim();
        List<String> arguments = argumentsText.isEmpty()
                ? List.of()
                : Arrays.asList(argumentsText.split("\\s+"));
        List<Integer> integerConstants = new ArrayList<>();
        if (parseIntegers && close + 1 < value.length()) {
            String integersText = value.substring(value.lastIndexOf('(') + 1, value.length() - 1).trim();
            if (!integersText.isEmpty()) {
                for (String integer : integersText.split("\\s+")) {
                    integerConstants.add(Integer.valueOf(integer));
                }
            }
        }
        return new ParsedSignature(new FluentSignature(predicate, arguments), integerConstants);
    }

    private ProblemTemporalConstraint parseTemporalConstraint(String declaration) {
        int argumentsOpen = declaration.lastIndexOf('(');
        String relationText = declaration.substring(0, argumentsOpen).trim();
        List<TemporalBounds> bounds = new ArrayList<>();
        int firstBounds = relationText.indexOf('[');
        String relationName = firstBounds < 0
                ? relationText
                : relationText.substring(0, firstBounds).trim();
        if (firstBounds >= 0) {
            String remaining = relationText.substring(firstBounds);
            for (String bound : remaining.split("\\[")) {
                if (!bound.isBlank()) {
                    String content = bound.substring(0, bound.indexOf(']'));
                    String[] endpoints = content.split(",");
                    bounds.add(new TemporalBounds(parseBound(endpoints[0]), parseBound(endpoints[1])));
                }
            }
        }

        String[] endpoints = declaration.substring(argumentsOpen + 1, declaration.lastIndexOf(')'))
                .split(",");
        String from = endpoints[0].trim();
        String to = endpoints.length == 1 ? from : endpoints[1].trim();
        TemporalConstraintTemplate template = new TemporalConstraintTemplate(
                TemporalRelation.valueOf(relationName), bounds);
        return new ProblemTemporalConstraint(from, to, template);
    }

    private long parseBound(String value) {
        String trimmed = value.trim();
        return trimmed.equals("INF") ? TemporalBounds.INFINITY : Long.parseLong(trimmed);
    }

    private ProblemOrderingEdge parseOrderingEdge(String declaration) {
        String[] endpoints = declaration.trim().split("\\s+");
        return new ProblemOrderingEdge(endpoints[0], endpoints[1]);
    }

    private List<String> parseArgumentSymbols(String problem) {
        String[] parsed = HybridDomain.parseKeyword(ARGUMENT_SYMBOLS_KEYWORD, problem);
        List<String> symbols = new ArrayList<>();
        if (parsed.length > 0) {
            symbols.addAll(Arrays.asList(parsed[0].split("\\s+")));
        } else {
            LOGGER.warning("Warning: No argument symbols specified in problem!");
        }
        symbols.add(N);
        return symbols;
    }

    private Map<String, List<String>> parseTypesInstancesMap(String problem) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String typeDefinition : inSourceOrder(HybridDomain.parseKeyword(INSTANCES_KEYWORD, problem))) {
            String[] elements = typeDefinition.split("\\s+");
            result.put(elements[0], Arrays.asList(Arrays.copyOfRange(elements, 1, elements.length)));
        }
        return result;
    }

    private static List<String> inSourceOrder(String[] reverseOrder) {
        List<String> ordered = new ArrayList<>(Arrays.asList(reverseOrder));
        Collections.reverse(ordered);
        return ordered;
    }

    private static List<String> distinct(List<String> values) {
        return List.copyOf(new LinkedHashSet<>(values));
    }

    @Override
    public String[] getArgumentSymbols() {
        LinkedHashSet<String> symbols = new LinkedHashSet<>(problemDefinition.argumentSymbols());
        symbols.addAll(additionalArgumentSymbols);
        return symbols.toArray(String[]::new);
    }

    @Override
    public void addArgumentSymbols(Collection<String> symbols) {
        additionalArgumentSymbols.addAll(symbols);
    }

    @Override
    public Map<String, String[]> getTypesInstancesMap() {
        Map<String, String[]> result = new LinkedHashMap<>();
        problemDefinition.typeInstances().forEach(
                (type, instances) -> result.put(type, instances.toArray(String[]::new)));
        return result;
    }

    @Override
    public ProblemDefinition getProblemDefinition() {
        if (additionalArgumentSymbols.isEmpty()) {
            return problemDefinition;
        }
        return new ProblemDefinition(
                Arrays.asList(getArgumentSymbols()),
                problemDefinition.typeInstances(),
                problemDefinition.initialFluents(),
                problemDefinition.initialTasks(),
                problemDefinition.temporalConstraints(),
                problemDefinition.orderingEdges());
    }

    private record ParsedSignature(FluentSignature signature, List<Integer> integerConstants) {}
}
