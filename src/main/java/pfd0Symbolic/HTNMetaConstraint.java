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

import com.sun.xml.internal.bind.v2.TODO;

import pfd0Symbolic.TaskApplicationMetaConstraint.markings;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicVariableConstraintSolver;
import cern.colt.Arrays;


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
	
	public HTNMetaConstraint(boolean oneShot) {
		super(null, null);
		this.oneShot = oneShot;
//		operators = new Vector<PlanReportroryItem>();
//		methods = new Vector<PlanReportroryItem>();
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
	private boolean checkPredecessors(Variable var, FluentNetworkSolver groundSolver) {
		for (FluentConstraint flc : 
			groundSolver.getFluentConstraintsOfTypeTo(var, FluentConstraint.Type.BEFORE)) {
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
		// TODO propagation probably not needed here
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
	
	private Vector<ConstraintNetwork> applyPlanrepoirtroryItems(Fluent fl,
			Vector<PlanReportroryItem> items, FluentNetworkSolver groundSolver) {
		Fluent[] openFluents = groundSolver.getOpenFluents(fl.getAllenInterval());
		logger.fine("OPEN FLUENTS: " + Arrays.toString(openFluents));
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (PlanReportroryItem item : items) {
			if (item.checkApplicability(fl) && item.checkPreconditions(openFluents)) {
				
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
