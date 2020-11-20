package hybridDomainParsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import planner.CHIMPProblem;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariable;

public class ProblemParser implements CHIMPProblem {
		
	public static final String PROBLEM_KEYWORD = "Problem";
	public static final String FLUENT_KEYWORD = "Fluent";
	public static final String TASK_KEYWORD = "Task";
	private static final String ARGUMENT_SYMBOLS_KEYWORD = "ArgumentSymbols";
	private static final String INSTANCES_KEYWORD = "Instances";
	
	private static final String N = "n";

	private final String fileName;
	private final Map<String, String> fluentElementsMap = new HashMap<String, String>();
	private final Map<String, String> taskElementsMap = new HashMap<String, String>();
	private String[] problemStrs;
	private Set<String> argumentSymbols = new HashSet<>();
	private Map<String, String[]> typesInstancesMap;



	public ProblemParser(String fileName) {
		this.fileName = fileName;
		parseProblem();
	}
	
	@Override
	public void createState(FluentNetworkSolver fluentSolver, ClassicHybridDomain domain) {
		Map<String, Variable> varsMap = new HashMap<String, Variable>(fluentElementsMap.size() 
				+ taskElementsMap.size());
		
		// create fluents
		for (Entry<String, String> e : fluentElementsMap.entrySet()) {
			Fluent var = (Fluent) fluentSolver.createVariable();
			int firstClosing = e.getValue().indexOf(')') + 1;
			String predicate = e.getValue();
			String symbolicPart = predicate;
			if (firstClosing != e.getValue().length()) {
				String intPart = predicate.substring(predicate.lastIndexOf('(') + 1 , predicate.length() - 1);
				String[] intElements = intPart.split(" ");
				for (int i = 0; i < intElements.length; i++) {
					var.getIntegerVariables()[i].setConstantValue(Integer.valueOf(intElements[i]));
				}
				symbolicPart = symbolicPart.substring(0, firstClosing);

			}
			var.setName(symbolicPart);
			var.setMarking(HTNMetaConstraint.markings.OPEN);
			varsMap.put(e.getKey(), var);
		}

		Set<String> operatorNames = domain.getOperatorNames();
		// create tasks
		for (Entry<String, String> e : taskElementsMap.entrySet()) {
			String fullName = e.getValue();
			String predicateName = fullName.split("\\(")[0];
			String component;
			if (operatorNames.contains(predicateName)) {
				component = Fluent.ACTIVITY_TYPE_STR;
			} else {
				component = Fluent.TASK_TYPE_STR;
			}
			Fluent var = (Fluent) fluentSolver.createVariable(component);
			var.setName(fullName);
			var.setMarking(HTNMetaConstraint.markings.UNPLANNED);
			varsMap.put(e.getKey(), var);
		}
		
		// create AllenIntervals
		for (String stateStr : problemStrs) {
			fluentSolver.addConstraints(createAllenIntervalConstraints(stateStr, varsMap));
		}
		
		// create Ordering Constraints
		for (String stateStr : problemStrs) {
			fluentSolver.addConstraints(createOrderingConstraints(stateStr, varsMap));
		}

		// create FluentResourceUsage constraints
		fluentSolver.addConstraints(createResourceUsageConstraints(domain.getFluentResourceUsages(), varsMap.values()));

	}

	private Constraint[] createResourceUsageConstraints(
			List<ResourceUsageTemplate> fluentResourceUsages,
			Collection<Variable> vars) {
		List<Constraint> ret = new ArrayList<Constraint>();
		Map<String, List<ResourceUsageTemplate>> usageTemplatesMap = HTNMetaConstraint.createResourceUsagesMap(fluentResourceUsages);
		
		for(Variable var : vars) {
			CompoundSymbolicVariable csv = ((Fluent) var).getCompoundSymbolicVariable();
			String symbol = csv.getPredicateName();
			
			List<ResourceUsageTemplate> rtList = usageTemplatesMap.get(symbol);
			if (rtList != null) {
				for (ResourceUsageTemplate rt : rtList) {
					FluentConstraint resourceCon = 
							new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
					resourceCon.setFrom(var);
					resourceCon.setTo(var);
					ret.add(resourceCon);
				}
			}
		}
		
		return ret.toArray(new Constraint[ret.size()]);
	}

	private void reset() {
		fluentElementsMap.clear();
		taskElementsMap.clear();
	}
	
	private void parseProblem() {
		reset();
		
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.trim().startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				//name = parseKeyword(DOMAIN_KEYWORD, everything)[0];

				problemStrs = HybridDomain.parseKeyword(PROBLEM_KEYWORD, everything);
				for (String problemStr : problemStrs) {
					parseProblem(problemStr);
				}

			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private void parseProblem(String problem) {
		argumentSymbols.addAll(parseArgumentSymbols(problem));
		typesInstancesMap = parseTypesInstancesMap(problem);
		
		String[] fluentStrs = HybridDomain.parseKeyword(FLUENT_KEYWORD, problem);
		for (String flStr : fluentStrs) {
			String key = flStr.substring(0,flStr.indexOf(" ")).trim();
			String fluent = flStr.substring(flStr.indexOf(" ")).trim();
			fluentElementsMap.put(key, fluent);
		}
		
		String[] taskStrs = HybridDomain.parseKeyword(TASK_KEYWORD, problem);
		for (String taskStr : taskStrs) {
			String key = taskStr.substring(0,taskStr.indexOf(" ")).trim();
			String fluent = taskStr.substring(taskStr.indexOf(" ")).trim();
			taskElementsMap.put(key, fluent);
		}
	}
	
	private AllenIntervalConstraint[] createAllenIntervalConstraints(String stateStr,
			Map<String, Variable> fluentsVarsMap) {
		// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.CONSTRAINT_KEYWORD, stateStr);
		AllenIntervalConstraint[] ret = new AllenIntervalConstraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length; i++) {
			String conElement = constraintElements[i];
			String constraintName = null;
			Vector<Bounds> bounds = null;
			if (conElement.contains("[")) {
				constraintName = conElement.substring(0,conElement.indexOf("[")).trim();
				String boundsString = conElement.substring(conElement.indexOf("["),conElement.lastIndexOf("]")+1);
				String[] splitBounds = boundsString.split("\\[");
				bounds = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[")+1,oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",")+1,oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF")) lb = org.metacsp.time.APSPSolver.INF;
						else lb = Long.parseLong(lbString);
						if (ubString.equals("INF")) ub = org.metacsp.time.APSPSolver.INF;
						else ub = Long.parseLong(ubString);
						bounds.add(new Bounds(lb,ub));
					}
				}
			}
			else {
				constraintName = conElement.substring(0,conElement.indexOf("(")).trim();
			}
			String from = null;
			String to = null;
			String fromSeg = null;
			if (constraintName.equals("Duration") || constraintName.equals("Release") 
					|| constraintName.equals("Deadline")) {
				from = conElement.substring(conElement.indexOf("(")+1, conElement.indexOf(")")).trim();
				to = from;
			}
			else {
				fromSeg = conElement.substring(conElement.indexOf("("));
				from = fromSeg.substring(fromSeg.indexOf("(")+1, fromSeg.indexOf(",")).trim();
				to = fromSeg.substring(fromSeg.indexOf(",")+1, fromSeg.indexOf(")")).trim();
			}

			AllenIntervalConstraint con = null;
			if (bounds != null) {
				con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),bounds.toArray(new Bounds[bounds.size()]));
			}
			else con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));
			
			con.setFrom(fluentsVarsMap.get(from));
			con.setTo(fluentsVarsMap.get(to));
			ret[i] = con;			
		}
		return ret;
	}
	
	private Constraint[] createOrderingConstraints(String stateStr,
			Map<String, Variable> fluentsVarsMap) {
		// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, stateStr);
		Constraint[] ret = new Constraint[constraintElements.length];
		for (int i = 0; i < constraintElements.length; i++) {
			String orderingElement = constraintElements[i];
			String fromKey = orderingElement.substring(0,orderingElement.indexOf(" ")).trim();
			String toKey = orderingElement.substring(orderingElement.indexOf(" ")).trim();
			FluentConstraint con = new FluentConstraint(FluentConstraint.Type.BEFORE);
			
			con.setFrom(fluentsVarsMap.get(fromKey));
			con.setTo(fluentsVarsMap.get(toKey));
			ret[i] = con;			
		}
		return ret;
	}
	
	private static List<String> parseArgumentSymbols(String everyting) {
		String[] parsed = HybridDomain.parseKeyword(ARGUMENT_SYMBOLS_KEYWORD, everyting);
		List<String> retL = new ArrayList<>();
		if (parsed.length > 0) {
			String[] parsedSymbols = parsed[0].split("\\s+");
			retL.addAll(Arrays.asList(parsedSymbols));
		} else {
			MetaCSPLogging.getLogger(ProblemParser.class).warning("Warning: No argument symbols specified in problem!");
		}
		retL.add(N);
		return retL;
	}
	
	private Map<String, String[]> parseTypesInstancesMap(String everything) {
		Map<String, String[]> ret = new HashMap<String, String[]>();
		for (String typeDef : HybridDomain.parseKeyword(INSTANCES_KEYWORD, everything)) {
			String[] t = typeDef.split("\\s+");
			ret.put(t[0], Arrays.copyOfRange(t, 1, t.length));
		}
		return ret;
	}

	@Override
	public String[] getArgumentSymbols() {
		return argumentSymbols.toArray(new String[argumentSymbols.size()]);
	}

	@Override
	public void addArgumentSymbols(Collection<String> symbols) {
		argumentSymbols.addAll(symbols);
	}

	@Override
	public Map<String, String[]> getTypesInstancesMap() {
		return typesInstancesMap;
	}
	
}
