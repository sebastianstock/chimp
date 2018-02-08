package htn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenInterval;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import resourceFluent.ResourceUsageTemplate;


public class HTNMetaConstraint extends MetaConstraint {
	
	private static final boolean DEBUG = true;
	public int getVarsCNT = 0;
	
	private static final long serialVersionUID = 4546697317217126280L;
	private final Vector<PlanReportroryItem> operators = new Vector<PlanReportroryItem>();
	private final Vector<PlanReportroryItem> methods =new Vector<PlanReportroryItem>();

	private Map<String, List<ResourceUsageTemplate>> resourcesTemplatesMap;  //resource type -> templates

	//false if we split up HTN algorithm into 3 meta constraints, 
	// true if we apply preconditions and effects at the same time.
	private final boolean oneShot;
	
	// try to unify tasks with planned tasks?
	private boolean tryUnification = false;

    public HTNMetaConstraint() {
		this(true);
	}
	
	public HTNMetaConstraint(ValueOrderingH valOH) {
		super(null, valOH);
		this.oneShot = true;
	}
	
	public HTNMetaConstraint(boolean oneShot) {
		super(null, null);
		this.oneShot = oneShot;
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
		ArrayList<Variable> tasks = new ArrayList<Variable>();
		for (Variable var : groundSolver.getVariables("Activity")) {
			tasks.add(var);
		}
		for (Variable var : groundSolver.getVariables("Task")) {
			tasks.add(var);
		}
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
		// TODO propagation probably not needed here
//		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
		//		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagatePredicateNames();
			
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		
		for (Variable var : problematicNetwork.getVariables()) {
			Fluent taskFluent = (Fluent) var;
			if (taskFluent.getCompoundSymbolicVariable().getPossiblePredicateNames()[0].charAt(0) == '!') {
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
			Vector<PlanReportroryItem> items, FluentNetworkSolver groundSolver) {
		
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
				
				logger.finest("Applying preconditions of PlanReportroryItem " + item);
				if (this.oneShot) {
					List<ConstraintNetwork> newResolvers = item.expandOneShot(fl, depth, groundSolver, openFluents);
					ret.addAll(newResolvers);
				} else {
					ret.add(item.expandPreconditions(fl, groundSolver));
				}
			}
		}
		
		// Add FluentResourceConstraints
		for (ConstraintNetwork cn :ret) {
			for (Variable v : cn.getVariables()) {
				if (v instanceof VariablePrototype) {
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
	

	/**
	 * Sets the marking of the task to SELECTED
	 */
	@Override
	public void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue) {
		// not needed anymore as we only use the unaryapplied
//		for (Constraint con : metaValue.getConstraints()) {
//			if (con instanceof FluentConstraint
//					&& ((FluentConstraint) con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) {
//				Variable task = ((FluentConstraint) con).getFrom();
//				if(this.oneShot) {
//					task.setMarking(markings.PLANNED);
//				} else {
//					task.setMarking(markings.SELECTED);
//				}
//			}
//		}

	}

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

	public void addOperators(Vector<PlanReportroryItem> operators) {
		this.operators.addAll(operators);
	}
	
	public void addMethods(Vector<PlanReportroryItem> methods) {
		this.methods.addAll(methods);
	}
	
	public void addOperator(PlanReportroryItem o) {
		operators.add(o);
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

}
