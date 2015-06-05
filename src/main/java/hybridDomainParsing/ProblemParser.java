package hybridDomainParsing;

import htn.HTNMetaConstraint;
import htn.TaskApplicationMetaConstraint.markings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariable;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;

public class ProblemParser {
		
	public static final String PROBLEM_KEYWORD = "Problem";
	public static final String FLUENT_KEYWORD = "Fluent";
	public static final String TASK_KEYWORD = "Task";

	private final String fileName;
	private final Map<String, String> fluentElementsMap = new HashMap<String, String>();
	private final Map<String, String> taskElementsMap = new HashMap<String, String>();
	private String[] problemStrs;



	public ProblemParser(String fileName) {
		this.fileName = fileName;
		parseProblem();
	}
	
	public void createState(FluentNetworkSolver fluentSolver, HybridDomain domain) {
		Map<String, Variable> varsMap = new HashMap<String, Variable>(fluentElementsMap.size() 
				+ taskElementsMap.size());
		
		// create fluents
		for (Entry<String, String> e : fluentElementsMap.entrySet()) {
			Variable var = fluentSolver.createVariable();
			((Fluent) var).setName(e.getValue());
			var.setMarking(markings.OPEN);
			varsMap.put(e.getKey(), var);
		}
		
		// create tasks
		for (Entry<String, String> e : taskElementsMap.entrySet()) {
			String name = e.getValue();
			String component;
			if (name.startsWith("!")) {
				component = "Activity";
			} else {
				component = "Task";
			}
			Variable var = fluentSolver.createVariable(component);
			((Fluent) var).setName(name);
			var.setMarking(markings.UNPLANNED);
			varsMap.put(e.getKey(), var);
		}
		
		// create AllenIntervals
		for (String stateStr : problemStrs) {
			fluentSolver.addConstraints(createAllenIntervalConstraints(stateStr, varsMap));
		}

		// create FluentResourceUsage constraints
		fluentSolver.addConstraints(createResourceUsageConstraints(domain.getFluentResourceUsages(), varsMap.values()));

	}

	private Constraint[] createResourceUsageConstraints(
			Vector<ResourceUsageTemplate> fluentResourceUsages,
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
}
