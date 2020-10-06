package planner;

import java.util.*;

import fluentSolver.Fluent;
import fluentSolver.FluentNetworkSolver;
import htn.HTNMetaConstraint;
import hybridDomainParsing.ClassicHybridDomain;
import hybridDomainParsing.HybridDomain;

/**
 * CHIMPProblem that is not based on a PDL file.
 * 
 * @author Sebastian Stock
 *
 */
public class DynamicProblem implements CHIMPProblem {
	
	private class FluentPrototype {
		
		public FluentPrototype(String p, String[] a) {
			this.predicate = p;
			this.args = a;
		}
		
		String predicate;
		String[] args;
	}
	
	private final Set<String> argumentSymbols = new HashSet<String>();
	private final Map<String, String[]> typesInstancesMap = new HashMap<String, String[]>();
	
	private final List<FluentPrototype> stateVars = new ArrayList<FluentPrototype>();
	private final List<FluentPrototype> taskVars = new ArrayList<FluentPrototype>();

	private boolean createdState = false;
	
	public void addArgumentSymbol(String symbol) {
		argumentSymbols.add(symbol);
	}
	
	public void setTypesInstances(String type, String[] instances) {
		typesInstancesMap.put(type, instances);
	}
	
	public void addState(String predicate, String[] args) {
		stateVars.add(new FluentPrototype(predicate, args));
		for (String s : args) {
			argumentSymbols.add(s);
		}
	}
	
	public void addTask(String predicate, String[] args) {
		taskVars.add(new FluentPrototype(predicate, args));
		for (String s : args) {
			argumentSymbols.add(s);
		}
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

	@Override
	public void createState(FluentNetworkSolver fluentSolver, ClassicHybridDomain domain) {
		if (createdState) {
			throw new IllegalStateException("State has already been created.");
		}
		createdState = true;
		System.out.println("Creating state");
		for (FluentPrototype p : stateVars) {

			Fluent fl = (Fluent) fluentSolver.createVariable();
			fl.setName(p.predicate, p.args);
			fl.setMarking(HTNMetaConstraint.markings.OPEN);
			System.out.println("Created state fluent " + fl.toString());
		}

		Set<String> operatorNames = domain.getOperatorNames();

		for (FluentPrototype p : taskVars) {
			String component;
			if (operatorNames.contains(p.predicate)) {
				component = Fluent.ACTIVITY_TYPE_STR;
			} else {
				component = "Task";
			}
			Fluent fl = (Fluent) fluentSolver.createVariable(component);
			fl.setName(p.predicate, p.args);
			fl.setMarking(HTNMetaConstraint.markings.UNPLANNED);
			System.out.println("Created fluent: " + fl.toString());
		}
		
		// TODO Create AllenIntervals
		// TODO Create Ordering Constraints
		// TODO Create FluentResourceUsage constraints
		
		
	}

}
