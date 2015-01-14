package htn;

import htn.TaskApplicationMetaConstraint.markings;

import java.util.ArrayList;
import java.util.Arrays;
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

import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;


public class HTNMetaConstraint extends MetaConstraint {
	
	private static final long serialVersionUID = 4546697317217126280L;
	private final Vector<PlanReportroryItem> operators = new Vector<PlanReportroryItem>();
	private final Vector<PlanReportroryItem> methods =new Vector<PlanReportroryItem>();

	private final Map<String, List<ResourceUsageTemplate>> resourcesTemplatesMap = 
			new HashMap<String, List<ResourceUsageTemplate>>();  //resource name -> templates

	//false if we split up HTN algorithm into 3 meta constraints, 
	// true if we apply preonditions and effects at the same time.
	private boolean oneShot; 

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
//		operators = new Vector<PlanReportroryItem>();
//		methods = new Vector<PlanReportroryItem>();
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
		logger.finest("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	
	/**
	 * Checks if a Variable has a Before with an UNPLANNED task.
	 * @return False if the Variable has an unplanned predecessor, otherwise true.
	 */
	private boolean checkPredecessors(Variable var, FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : 
			groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
			Object marking = flc.getScope()[0].getMarking();
			if (!checkApplied((Fluent)flc.getScope()[0])) {
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
		long startTime = System.nanoTime();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		// TODO propagation probably not needed here
		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagateAllSub();
		//		((CompoundSymbolicVariableConstraintSolver) groundSolver.getConstraintSolvers()[0]).propagatePredicateNames();
			
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		
		for (Variable var : problematicNetwork.getVariables()) {
			Fluent taskFluent = (Fluent) var;
			if (taskFluent.getCompoundSymbolicVariable().getPossiblePredicateNames()[0].charAt(0) == '!') {
				ret.addAll(applyPlanrepoirtroryItems(taskFluent, operators, groundSolver));
			} else {
				ret.addAll(applyPlanrepoirtroryItems(taskFluent, methods, groundSolver));
			}
		}
		
		long endTime = System.nanoTime();
		logger.finest("Computed metaValues for " + problematicNetwork.getVariables().length + " tasks");
		logger.finest("Found " + ret.size() + " metaValues");
		logger.finest("HTN GetMetaValues Took: " + ((endTime - startTime) / 1000000) + " ms");
		
		if (!ret.isEmpty()) 
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		return null;
	}
	
	private Vector<ConstraintNetwork> applyPlanrepoirtroryItems(Fluent fl,
			Vector<PlanReportroryItem> items, FluentNetworkSolver groundSolver) {
		Fluent[] openFluents = groundSolver.getOpenFluents(fl.getAllenInterval());
//		Fluent[] openFluents = groundSolver.getOpenFluents();
		logger.fine("OPEN FLUENTS: " + Arrays.toString(openFluents));
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (PlanReportroryItem item : items) {
			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
				
				logger.finest("Applying preconditions of PlanReportroryItem " + item);
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
		for (Constraint con : metaValue.getConstraints()) {
			if (con instanceof FluentConstraint 
					&& ((FluentConstraint) con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) {
				Variable task = ((FluentConstraint) con).getFrom();
				if(this.oneShot) {
					task.setMarking(markings.PLANNED);
				} else {
					task.setMarking(markings.SELECTED);
				}
			}
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
	 * @param resourceTemplates
	 */
	public void setResourceUsages(List<ResourceUsageTemplate> resourceTemplates) {
		for (ResourceUsageTemplate rt : resourceTemplates){
			List<ResourceUsageTemplate> l = resourcesTemplatesMap.get(rt.getResourceName());
			if (l == null) {
				l = new ArrayList<ResourceUsageTemplate>();
				resourcesTemplatesMap.put(rt.getFluentType(), l);
			}
			l.add(rt);
		}
	}

}
