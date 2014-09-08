package pfd0Symbolic;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import resourceFluent.SchedulableFluent;
import resourceFluent.SimpleReusableResourceFluent;
import symbolicUnifyTyped.TypedCompoundSymbolicVariableConstraintSolver;


public class TaskSelectionMetaConstraint extends MetaConstraint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4546697317217126280L;
	private Vector<PlanReportroryItem> operators;
	private Vector<PlanReportroryItem> methods;
	protected String[] resourceNames;
	protected HashMap<String,SimpleReusableResourceFluent> resourcesMap;
	protected HashMap<SimpleReusableResourceFluent,HashMap<Variable,Integer>> currentResourceUtilizers;

	private String name = "";

	public TaskSelectionMetaConstraint() {
		this(new int[0], new String[0], "");
//		super(null, null);
//		operators = new Vector<PlanReportroryItem>();
//		methods = new Vector<PlanReportroryItem>();
	}
	
	public TaskSelectionMetaConstraint(int[] capacities, String[] resourceNames, String domainName) {
		super(null, null);
		operators = new Vector<PlanReportroryItem>();
		methods = new Vector<PlanReportroryItem>();
		
		//added by Iran
		this.name = domainName;
		this.resourceNames = resourceNames;
		currentResourceUtilizers = new HashMap<SimpleReusableResourceFluent,HashMap<Variable,Integer>>();
		resourcesMap = new HashMap<String, SimpleReusableResourceFluent>();
//
//		for (int i = 0; i < capacities.length; i++) {
//			//Most critical conflict is the one with most activities 
//			VariableOrderingH varOH = new VariableOrderingH() {
//				@Override
//				public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
//					return arg1.getVariables().length - arg0.getVariables().length;
//				}
//				@Override
//				public void collectData(ConstraintNetwork[] allMetaVariables) { }
//			};
//			// no value ordering
//			ValueOrderingH valOH = new ValueOrderingH() {
//				@Override
//				public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
//			};
//			resourcesMap.put(resourceNames[i], new SimpleReusableResourceFluent(varOH, valOH, capacities[i], this, resourceNames[i]));
//		}
//
//		// for every SRR just created, couple it with a vector of variables
//		for (SimpleReusableResourceFluent rr : resourcesMap.values()) currentResourceUtilizers.put(rr,new HashMap<Variable, Integer>());

	}

	public SchedulableFluent[] getSchedulingMetaConstraints() {
		return currentResourceUtilizers.keySet().toArray(new SchedulableFluent[currentResourceUtilizers.keySet().size()]);
	}
	
	public void addResrouceUtilizers(SimpleReusableResourceFluent rr, HashMap<Variable, Integer> hm) {
		currentResourceUtilizers.put(rr,hm);
	}

	public void addResrouceUtilizer(SimpleReusableResourceFluent rr, Variable var, Integer amount) {
		currentResourceUtilizers.get(rr).put(var,amount);
	}
	
	public void addReourceMap(String resourcename, SimpleReusableResourceFluent simpleReusableResource){		
		resourcesMap.put(resourcename, simpleReusableResource);
	}
	
	
	public HashMap<String, SimpleReusableResourceFluent> getResources() {
		return resourcesMap;
	}

	// Given a variable act, it returns all the resources that are currently exploited by the variable
	public SimpleReusableResourceFluent[] getCurrentReusableResourcesUsedByActivity(Variable act) {
		Vector<SimpleReusableResourceFluent> ret = new Vector<SimpleReusableResourceFluent>();
		for (SimpleReusableResourceFluent rr : currentResourceUtilizers.keySet()) {
			if (currentResourceUtilizers.get(rr).containsKey(act)) 
				ret.add(rr);
		}
		return ret.toArray(new SimpleReusableResourceFluent[ret.size()]);
	}

	public int getResourceUsageLevel(SimpleReusableResourceFluent rr, Variable act) {
		return currentResourceUtilizers.get(rr).get(act);
	}
	
	public HashMap<SimpleReusableResourceFluent,HashMap<Variable,Integer>> getAllResourceUsageLevel(){
		return currentResourceUtilizers;
	}

	public void resetAllResourceAllocation(){
		currentResourceUtilizers = new HashMap<SimpleReusableResourceFluent, HashMap<Variable,Integer>>();
		for (SimpleReusableResourceFluent rr : resourcesMap.values()) currentResourceUtilizers.put(rr,new HashMap<Variable, Integer>());

	}

	
	
	public void addOperator(PlanReportroryItem o) {
		operators.add(o);
	}
	
	public void addMethod(PlanReportroryItem m) {
		methods.add(m);
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
		// a ConstraintNetwork is built.
		// this becomes a task.
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking() != null && var.getMarking().equals(markings.UNPLANNED)) {
				if (checkPredecessors(var, groundSolver)) {  // only add it if there are no predecessors
					ConstraintNetwork nw = new ConstraintNetwork(null);
					nw.addVariable(var);
					ret.add(nw);
				}
			}
		}
		System.out.println("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	
	}
	
	
	/**
	 * Checks if a Variable has a Before with an UNPLANNED task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkPredecessors(Variable var, 
			FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
			Object marking = flc.getScope()[0].getMarking();
			if ( (marking == markings.UNPLANNED) || (marking == markings.SELECTED) ){
				return false;
			}	 
		}
		return true;
	}

	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariables}.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> ret;
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent taskFluent = (Fluent)problematicNetwork.getVariables()[0];
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		
		logger.info("getMetaValues for: " + taskFluent);
		// TODO: following line can probably be removed for speedup:
		((TypedCompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
		if (taskFluent.getCompoundSymbolicVariable().getPossiblePredicateNames()[0].charAt(0) == '!') {
			ret = applyPlanrepoirtroryItems(taskFluent, operators, groundSolver);
		} else {
			ret = applyPlanrepoirtroryItems(taskFluent, methods, groundSolver);
		}
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	private Vector<ConstraintNetwork> applyPlanrepoirtroryItems(Fluent fl, Vector<PlanReportroryItem> items, 
			FluentNetworkSolver groundSolver) {
		Fluent[] openFluents = groundSolver.getOpenFluents();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (PlanReportroryItem item : items) {
			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
				
				
//				HashMap<String, Integer> usages = ((PFD0Operator)item).getResourceUsage();
//				for (String resname : usages.keySet()) {
//					HashMap<Variable, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(resname));
//					utilizers.put(fl, usages.get(resname));
//				}
				
				logger.fine("Applying preconditions of PlanReportroryItem " + item);
				List<ConstraintNetwork> newResolvers = item.expandPreconditions(fl,  groundSolver);
				for (ConstraintNetwork newResolver : newResolvers) {
					ret.add(newResolver);
				}
			}
		}
		return ret;
	}
	
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		return getMetaValues(metaVariable);
	}

	/**
	 * Sets the marking of the task to SELECTED
	 */
	@Override
	public void markResolvedSub(MetaVariable metaVariable,
			ConstraintNetwork metaValue) {
		// TODO if it is a primitive task, set the marking to PLANNED
		metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.SELECTED);
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


	public int getResourceUsageLevel(
			SimpleReusableResourceFluent simpleReusableResourceFluent,
			Fluent act) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setOperators(Vector<PlanReportroryItem> operators) {
		this.operators = operators;
	}
	
	public void setMethods(Vector<PlanReportroryItem> methods) {
		this.methods = methods;
	}

}
