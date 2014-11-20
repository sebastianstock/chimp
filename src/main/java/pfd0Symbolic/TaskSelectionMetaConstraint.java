package pfd0Symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import cern.colt.Arrays;


public class TaskSelectionMetaConstraint extends MetaConstraint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4546697317217126280L;
	private Vector<PlanReportroryItem> operators;
	private Vector<PlanReportroryItem> methods;
//	protected String[] resourceNames;
//	protected HashMap<String, FluentResourceUsageScheduler> resourcesMap;
//	protected HashMap<FluentResourceUsageScheduler,HashMap<Variable,Integer>> currentResourceUtilizers;
	private final Map<String, List<ResourceUsageTemplate>> resourcesTemplatesMap = 
			new HashMap<String, List<ResourceUsageTemplate>>();  //resource name -> templates

	private String name = "";
	//false if we split up HTN algorithm into 3 meta constraints, 
	// true if we apply preonditions and effects at the same time.
	private boolean oneShot; 

	public TaskSelectionMetaConstraint() {
		this(true);
	}
	
	public TaskSelectionMetaConstraint(boolean oneShot) {
//		this(new int[0], new String[0], "", oneShot);
		super(null, null);
		this.oneShot = oneShot;
//		operators = new Vector<PlanReportroryItem>();
//		methods = new Vector<PlanReportroryItem>();
	}
	
	public void setResourceUsages(List<ResourceUsageTemplate> resourceTemplates) {
		for (ResourceUsageTemplate rt : resourceTemplates){
			List<ResourceUsageTemplate> l = resourcesTemplatesMap.get(rt.getResourceName());
			if (l == null) {
				l = new ArrayList<ResourceUsageTemplate>();
				resourcesTemplatesMap.put(rt.getResourceName(), l);
			}
			l.add(rt);
		}
	}
	
	
	// TODO add again for resources
//	public TaskSelectionMetaConstraint(int[] capacities, String[] resourceNames, String domainName, boolean oneShot) {
//		super(null, null);
//		this.oneShot = oneShot;
//		operators = new Vector<PlanReportroryItem>();
//		methods = new Vector<PlanReportroryItem>();
//		
//		//added by Iran
//		this.name = domainName;
//		this.resourceNames = resourceNames;
//		currentResourceUtilizers = new HashMap<FluentResourceUsageScheduler,HashMap<Variable,Integer>>();
//		resourcesMap = new HashMap<String, FluentResourceUsageScheduler>();
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
//
//	}
//
//	public SchedulableFluent[] getSchedulingMetaConstraints() {
//		return currentResourceUtilizers.keySet().toArray(new SchedulableFluent[currentResourceUtilizers.keySet().size()]);
//	}
//	
//	public void addResrouceUtilizers(SimpleReusableResourceFluent rr, HashMap<Variable, Integer> hm) {
//		currentResourceUtilizers.put(rr,hm);
//	}
//
//	public void addResrouceUtilizer(SimpleReusableResourceFluent rr, Variable var, Integer amount) {
//		currentResourceUtilizers.get(rr).put(var,amount);
//	}
//	
//	public void addReourceMap(String resourcename, SimpleReusableResourceFluent simpleReusableResource){		
//		resourcesMap.put(resourcename, simpleReusableResource);
//	}
//	
//	
//	public HashMap<String, SimpleReusableResourceFluent> getResources() {
//		return resourcesMap;
//	}
//
//	// Given a variable act, it returns all the resources that are currently exploited by the variable
//	public SimpleReusableResourceFluent[] getCurrentReusableResourcesUsedByActivity(Variable act) {
//		Vector<SimpleReusableResourceFluent> ret = new Vector<SimpleReusableResourceFluent>();
//		for (SimpleReusableResourceFluent rr : currentResourceUtilizers.keySet()) {
//			if (currentResourceUtilizers.get(rr).containsKey(act)) 
//				ret.add(rr);
//		}
//		return ret.toArray(new SimpleReusableResourceFluent[ret.size()]);
//	}
//
//	public int getResourceUsageLevel(SimpleReusableResourceFluent rr, Variable act) {
//		return currentResourceUtilizers.get(rr).get(act);
//	}
//	
//	public HashMap<SimpleReusableResourceFluent,HashMap<Variable,Integer>> getAllResourceUsageLevel(){
//		return currentResourceUtilizers;
//	}
//
//	public void resetAllResourceAllocation(){
//		currentResourceUtilizers = new HashMap<SimpleReusableResourceFluent, HashMap<Variable,Integer>>();
//		for (SimpleReusableResourceFluent rr : resourcesMap.values()) currentResourceUtilizers.put(rr,new HashMap<Variable, Integer>());
//
//	}

	
	
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
		logger.finest("MetaVariables: " + ret);
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
		
		logger.fine("getMetaValues for: " + taskFluent);
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
//		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagatePredicateNames();
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
		Fluent[] openFluents = groundSolver.getOpenFluents(fl.getAllenInterval());
		logger.fine("OPEN FLUENTS: " + Arrays.toString(openFluents));
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (PlanReportroryItem item : items) {
			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
				
				// IRAN
//				HashMap<String, Integer> usages = ((PFD0Operator)item).getResourceUsage();
//				for (String resname : usages.keySet()) {
//					HashMap<Variable, Integer> utilizers = currentResourceUtilizers.get(resourcesMap.get(resname));
//					utilizers.put(fl, usages.get(resname));
//				}
				
				logger.fine("Applying preconditions of PlanReportroryItem " + item);
				if (this.oneShot) {
					List<ConstraintNetwork> newResolvers = item.expandOneShot(fl, groundSolver, openFluents);
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
					for (ResourceUsageTemplate rt : resourcesTemplatesMap.get(symbol)) {
						FluentConstraint resourceCon = 
								new FluentConstraint(FluentConstraint.Type.RESOURCEUSAGE, rt);
						resourceCon.setFrom(v);
						resourceCon.setTo(v);
						cn.addConstraint(resourceCon);
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
		if(this.oneShot) {
			metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.PLANNED);
		} else {
			metaVariable.getConstraintNetwork().getVariables()[0].setMarking(markings.SELECTED);
		}
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


//	public int getResourceUsageLevel(
//			SimpleReusableResourceFluent simpleReusableResourceFluent,
//			Fluent act) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	public void setOperators(Vector<PlanReportroryItem> operators) {
		this.operators = operators;
	}
	
	public void setMethods(Vector<PlanReportroryItem> methods) {
		this.methods = methods;
	}

}
