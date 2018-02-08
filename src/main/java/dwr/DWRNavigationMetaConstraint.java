package dwr;

import java.util.List;
import java.util.Vector;

import htn.HTNMetaConstraint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomain;
import unify.CompoundSymbolicVariable;



public class DWRNavigationMetaConstraint extends MetaConstraint {

	private static final long serialVersionUID = 8908231006136514834L;
	private static final String NAVIGATE_TASK_NAME = "navigate";
	private static final String CONNECTED_NAME = "connected";

	public DWRNavigationMetaConstraint() {
		super(null, null);
	}
	/** 
	 * @return All {@link MetaVariable}s with the marking UNPLANNED and which have no unplanned 
	 * predecessors.
	 */
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (Variable var : groundSolver.getVariables()) {
			if (((Fluent) var).getCompoundSymbolicVariable().getPredicateName().equals(NAVIGATE_TASK_NAME)) {
				//			if (var.getMarking() != null && var.getMarking().equals(markings.UNPLANNED)) {
				if ( !checkApplied((Fluent) var)) {
					if (checkPredecessors(var, groundSolver)) {  // only add it if there are no predecessors
						ConstraintNetwork nw = new ConstraintNetwork(null);
						nw.addVariable(var);
						ret.add(nw);
					}
				}
			}
			//			}
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
			if (!checkApplied((Fluent)flc.getScope()[0])) {
				return false;
			}	 
		}
		return true;
	}
	
	private boolean checkApplied(Fluent task) {
		for (Constraint con : this.getGroundSolver().getConstraintNetwork().getConstraints(task, task)) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint)con).getType().equals(FluentConstraint.Type.UNARYAPPLIED)) return true;
			}
		}
		return false;
	}
	
	private Graph<String, String> createGraph() {
		Graph<String,String> g = new DirectedSparseGraph<String, String>();
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getGroundSolver();
		for (Variable var : groundSolver.getVariables()) {
			CompoundSymbolicVariable csv = ((Fluent) var).getCompoundSymbolicVariable();
			if (csv.getPredicateName().equals(CONNECTED_NAME)) {
				String from = csv.getGroundSymbolAt(1);
				String to = csv.getGroundSymbolAt(2);
				g.addVertex(from);
				g.addVertex(to);
				g.addEdge(csv.getName(), from, to);
			}
		}
		return g;
	}
	
	/**
	 * Get all values for a given {@link MetaVariable}.
	 * @param metaVariable The {@link MetaVariable} for which we seek meta values.
	 * @return All meta values for the given{@link MetaVariable}s.
	 */
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Fluent taskFluent = (Fluent)problematicNetwork.getVariables()[0];
		
		logger.fine("getMetaValues for: " + taskFluent);
		
//		return estimateDuration(taskFluent);
		Graph<String, String> g = createGraph();
		DijkstraShortestPath<String,String> dijk = new DijkstraShortestPath<String,String>(g);
		
		Variable[] conflicts = metaVariable.getConstraintNetwork().getVariables();
		ConstraintNetwork cn = new ConstraintNetwork(null);
		for (int i = 0; i < conflicts.length; i++) {
			Fluent nav = (Fluent) conflicts[i];
			if (! nav.getCompoundSymbolicVariable().getPredicateName().equals(NAVIGATE_TASK_NAME)) {
				throw new IllegalStateException("Expected only 'navigate'-Fluents");
			}
			String robot = nav.getCompoundSymbolicVariable().getGroundSymbolAt(1);
			String from = nav.getCompoundSymbolicVariable().getGroundSymbolAt(2);
			String to = nav.getCompoundSymbolicVariable().getGroundSymbolAt(3);
			List<String> path = dijk.getPath(from, to);
			VariablePrototype[] prototypes = new VariablePrototype[path.size()];
			VariablePrototype prev = null;
			
			for (int j = 0; j < path.size(); ++j) {
				String e = path.get(j);
				String[] s_items = e.substring("connected(".length()).split(" ");
				String[] args = new String[5];
				args[0] = robot;
				args[1] = s_items[0];
				args[2] = s_items[1].substring(0, s_items[1].length()-1);
				args[3] = HybridDomain.EMPTYSTRING;
				args[4] = HybridDomain.EMPTYSTRING;
				VariablePrototype vp = new VariablePrototype(this.getGroundSolver(), "Activity", "!move", args);
				vp.setMarking(HTNMetaConstraint.markings.UNPLANNED);
				prototypes[j] = vp;
				FluentConstraint dc = new FluentConstraint(FluentConstraint.Type.DC);
				dc.setFrom(nav);
				dc.setTo(vp);
				cn.addConstraint(dc);
				if (j > 0) {
					FluentConstraint bc = new FluentConstraint(FluentConstraint.Type.BEFORE);
					bc.setFrom(prev);
					bc.setTo(vp);
					cn.addConstraint(bc);
					AllenIntervalConstraint aiCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, 
							AllenIntervalConstraint.Type.Before.getDefaultBounds());
					aiCon.setFrom(prev);
					aiCon.setTo(vp);
					cn.addConstraint(aiCon);
				}
				if (j == path.size()-1) {
					List<FluentConstraint> tasksBeforeConstrs = 
							((FluentNetworkSolver) getGroundSolver()).getFluentConstraintsOfTypeFrom(nav, FluentConstraint.Type.BEFORE);
					for (FluentConstraint c : tasksBeforeConstrs) {
						FluentConstraint oc = 
								new FluentConstraint(FluentConstraint.Type.BEFORE);
						oc.setFrom(vp);
						oc.setTo(c.getScope()[1]);
						cn.addConstraint(oc);
					}
				}
				prev = vp;
			}
			
			FluentConstraint applicationCon = 
					new FluentConstraint(FluentConstraint.Type.UNARYAPPLIED);
			applicationCon.setFrom(nav);
			applicationCon.setTo(nav);
			applicationCon.setDepth(100);
			cn.addConstraint(applicationCon);	
		}
		
		return new ConstraintNetwork[] {cn};

	}
	
	
	@Override
	public void markResolvedSub(MetaVariable metaVariable, ConstraintNetwork metaValue) {
		// nothing to do here
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
		return "DWRNavigationMetaConstraint";
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

}
