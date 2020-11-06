package htn;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import org.metacsp.framework.*;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenInterval;
import resourceFluent.ResourceUsageTemplate;

import java.util.*;


public class HTNMetaConstraint extends MetaConstraint {

	public enum markings {UNPLANNED, SELECTED, PLANNED, OPEN, CLOSED, UNJUSTIFIED, JUSTIFIED, UNIFIED}

	private static final boolean DEBUG = true;
	public int getVarsCNT = 0;
	
	private static final long serialVersionUID = 4546697317217126280L;
	private final List<PlanReportroryItem> operators = new ArrayList<PlanReportroryItem>();
	private final Set<String> operatorNames = new HashSet<>();
	private final List<PlanReportroryItem> methods = new ArrayList<PlanReportroryItem>();

	private Map<String, List<ResourceUsageTemplate>> resourcesTemplatesMap;  //resource type -> templates
	
	// try to unify tasks with planned tasks?
	private boolean tryUnification = false;
	
	public HTNMetaConstraint(ValueOrderingH valOH) {
		super(null, valOH);
	}
	
	public HTNMetaConstraint() {
		super(null, null);
	}
	
	private boolean checkApplied(Fluent task) {
		for (Constraint con : this.getGroundSolver().getConstraintNetwork().getConstraints(task, task)) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint)con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) return true;
			}
		}
		return false;
	}
	
	/** 
	 * @return All {@link MetaVariable}s with the marking UNPLANNED and which have no unplanned 
	 * predecessors.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that has the marking UNPLANNED and that has no unplanned predecessors 
		// add it to the ConstraintNetwork network
		ConstraintNetwork nw = new ConstraintNetwork(null);
		List<Variable> tasks = new LinkedList<Variable>(Arrays.asList(groundSolver.getVariables("Activity")));
		tasks.addAll(Arrays.asList(groundSolver.getVariables("Task")));
		for (Variable var : tasks) {
			if (!checkApplied((Fluent)var)) {
				if (checkPredecessors(var, groundSolver)) {  // only add it if there are no predecessors
					nw.addVariable(var);
				}
			}
		}
		
		if (nw.getVariables().length > 0) {
				ret.add(nw);
		}
//		logger.finest("MetaVariables: " + ret);

		logger.info("getMetaVariables-Invocation: " + ++getVarsCNT);
		if (DEBUG) {
			logger.info("  Meta-Variables: " + ret);
			//			if (applicationCNT == 300) {
			//				ret.clear();
			//			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	
	/**
	 * Checks if a Variable has a Before with an UNPLANNED task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkPredecessors(Variable var, FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : 
			groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
			if (!checkApplied((Fluent)flc.getScope()[0])) {
				return false;
			}	 
		}
		return true;
	}

	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariable}s.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		long startTime = System.nanoTime();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
			
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		
		for (Variable var : problematicNetwork.getVariables()) {
			Fluent taskFluent = (Fluent) var;
			String taskName = taskFluent.getCompoundSymbolicVariable().getPossiblePredicateNames()[0];
			if (operatorNames.contains(taskName)) {
				if (tryUnification) {
					ret.addAll(unifyTasks(taskFluent, groundSolver));
				}
				ret.addAll(applyPlanrepoirtroryItems(taskFluent, operators, groundSolver));
			} else {
				if (tryUnification) {
				ret.addAll(unifyTasks(taskFluent, groundSolver));
				}
				ret.addAll(applyPlanrepoirtroryItems(taskFluent, methods, groundSolver));
			}
		}
		
		long endTime = System.nanoTime();
		logger.finest("Computed metaValues for " + problematicNetwork.getVariables().length + " tasks");
		logger.finest("Found " + ret.size() + " metaValues");
		if (DEBUG) {
			logger.info("  Found " + ret.size() + " metaValues");
		}
		logger.finest("HTN GetMetaValues Took: " + ((endTime - startTime) / 1000000) + " ms");
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	private List<ConstraintNetwork> unifyTasks(Fluent task, FluentNetworkSolver groundSolver) {
		List<ConstraintNetwork> ret = new ArrayList<ConstraintNetwork>();
		
		String taskPredicate = task.getCompoundSymbolicVariable().getPredicateName();
		
		// get planned tasks with the same name
		List<Fluent> possibleMatchingTasks = new ArrayList<Fluent>();
		for (Variable var : groundSolver.getVariables(task.getComponent())) {
			if (checkApplied((Fluent)var)) { // TODO CHECK start times
				if (taskPredicate.equals(((Fluent)var).getCompoundSymbolicVariable().getPredicateName())) {
					
					if (checkTime(task.getAllenInterval(), ((Fluent) var).getAllenInterval())) {

						// possibleMatchingTasks.add((Fluent) var);

						// compare full name
						if (compareWithGetName(task, (Fluent) var)) {
							possibleMatchingTasks.add((Fluent) var);
						}
					}
				}

			}
		}
		
		for (Fluent fl : possibleMatchingTasks) {
			ConstraintNetwork cn = new ConstraintNetwork(null);
			// add unification
			FluentConstraint mc = new FluentConstraint(FluentConstraint.Type.MATCHES);
			mc.setFrom(task);
			mc.setTo(fl);
			cn.addConstraint(mc);
			// Add unaryApplied
			FluentConstraint applicationCon = 
					new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED);
			applicationCon.setFrom(task);
			applicationCon.setTo(task);
//			applicationCon.setDepth(depth);
			cn.addConstraint(applicationCon);
			ret.add(cn);
		}
		if (ret.size() > 0 ) {
			logger.info("Created " + ret.size() + " TaskUnification-Meta-Values.");
		}
		
		return ret;
	}
	
	private boolean checkTime(AllenInterval taskInterval, AllenInterval varInterval) {

		return varInterval.getEST() <= taskInterval.getLST()
				&& taskInterval.getEST() <= varInterval.getLST()
				&& varInterval.getEET() <= taskInterval.getLET()
				&& taskInterval.getEET() <= varInterval.getLET();
	}

	private boolean compareWithGetName(Fluent task, Fluent var) {
		return task.getCompoundSymbolicVariable().getName().equals(var.getCompoundSymbolicVariable().getName());
	}
	
	
	private boolean parametersAreMatching(Fluent task, Fluent var) {
		
		try {
			String[] taskArgs = task.getCompoundSymbolicVariable().getGroundArgs();
			String[] varArgs = var.getCompoundSymbolicVariable().getGroundArgs();
			for (int i = 0; i < taskArgs.length; i++) {
				if (!taskArgs[i].equals(varArgs[i])) {
					return false;
				}
			}
			
		} catch (IllegalStateException e) {
			
		}
		return true;
	}

	private Vector<ConstraintNetwork> applyPlanrepoirtroryItems(Fluent fl,
			List<PlanReportroryItem> items, FluentNetworkSolver groundSolver) {
		
		/// Extract depth:
		int depth = 0;
		List<FluentConstraint> inDC = groundSolver.getFluentConstraintsOfTypeTo(fl, FluentConstraint.Type.DC);
		if (inDC.size() > 0) {
			Fluent from = (Fluent) inDC.get(0).getFrom();
			List<FluentConstraint> inUA = groundSolver.getFluentConstraintsOfTypeFrom(from, FluentConstraint.Type.UNARYAPPLIED);
			if (inUA.size() > 0) {
				try {
					depth = inUA.get(0).getDepth() + 1;
				} catch (IllegalAccessException e) {
					logger.severe("Depth has not been set for UNARYAPPLIED");
				}
			}
		}

		Fluent[] openFluents = groundSolver.getOpenFluents(fl.getAllenInterval());

		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (PlanReportroryItem item : items) {
			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
                List<ConstraintNetwork> newResolvers = item.expand(fl, depth, groundSolver, openFluents);
                ret.addAll(newResolvers);
			}
		}
		
		// Add FluentResourceConstraints
		for (ConstraintNetwork cn :ret) {
			for (Variable v : cn.getVariables()) {
				if (v instanceof VariablePrototype &&
						!(((VariablePrototype) v).getParameters()[0] instanceof VariablePrototype)) {
					String symbol = (String)((VariablePrototype) v).getParameters()[1];
					List<ResourceUsageTemplate> rtList = resourcesTemplatesMap.get(symbol);
					if (rtList != null) {
						for (ResourceUsageTemplate rt : rtList) {
							FluentConstraint resourceCon = 
									new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
							resourceCon.setFrom(v);
							resourceCon.setTo(v);
							cn.addConstraint(resourceCon);
						}
					}
				}
			}
		}
		
		return ret;
	}

	@Override
	public void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue) { }

	@Override
	public void draw(ConstraintNetwork network) {
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return this.metaCS.getConstraintSolvers()[0];
	}

	@Override
	public String toString() {
		return "PFD0MetaConstraint";
	}

	@Override
	public String getEdgeLabel() {
		return null;
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		return false;
	}

	public void addOperators(List<PlanReportroryItem> operators) {
		this.operators.addAll(operators);
		for (PlanReportroryItem op : operators) {
			operatorNames.add(op.getName());
		}
	}
	
	public void addMethods(List<PlanReportroryItem> methods) {
		this.methods.addAll(methods);
	}
	
	public void addOperator(PlanReportroryItem o) {
		operators.add(o);
		operatorNames.add(o.getName());
	}
	
	public void addMethod(PlanReportroryItem m) {
		methods.add(m);
	}
	
	/**
	 * Adds resources usages of general fluents.
	 * When creating effects these will be used to add resource constraints to those fluents.
	 * @param resourceTemplates Indicate the resource usages.
	 */
	public void setResourceUsages(List<ResourceUsageTemplate> resourceTemplates) {
		resourcesTemplatesMap = createResourceUsagesMap(resourceTemplates);
	}
	
	/**
	 * Generate a map from resource types to lists of ResourceUsageTemplates.
	 * @param resourceTemplates Indicate the resource usages.
	 * @return Map from resource types to a list of ResourceUsageTemplates.
	 */
	public static Map<String, List<ResourceUsageTemplate>> createResourceUsagesMap(List<ResourceUsageTemplate> resourceTemplates) {
		Map<String, List<ResourceUsageTemplate>> ret = 
				new HashMap<String, List<ResourceUsageTemplate>>();  //resource type -> templates
		for (ResourceUsageTemplate rt : resourceTemplates){
			List<ResourceUsageTemplate> l = ret.get(rt.getFluentType());
			if (l == null) {
				l = new ArrayList<ResourceUsageTemplate>();
				ret.put(rt.getFluentType(), l);
			}
			l.add(rt);
		}
		return ret;
	}

	/**
	 * With this option set it tries to unify tasks to already planned tasks. (Turned off by default.)
	 */
	public void enableUnification() {
		this.tryUnification = true;	
	}

	public List<PlanReportroryItem> getOperators() {
		return operators;
	}

	public List<PlanReportroryItem> getMethods() {
		return methods;
	}
}
