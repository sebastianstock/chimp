package old_dev.axioms;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;

import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;
import unify.CompoundSymbolicVariable;

public class AxiomMetaConstraint extends MetaConstraint {

	public AxiomMetaConstraint() {
		super(null, null);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6126436488895919835L;

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		
		for (FluentConstraint axiomConstraint : getAxiomConstraints()) {
			ConstraintNetwork cn = checkAxiom(axiomConstraint);
			if (cn != null) {
				ret.add(cn);
			}
		}
		logger.finest("MetaVariables: " + ret);
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	private ConstraintNetwork checkAxiom(FluentConstraint con) {
		boolean foundViolation = false;
		ConstraintNetwork nw = new ConstraintNetwork(null);
		String axiomName = con.getAxiom();
		Fluent taskFluent = (Fluent) con.getFrom();
		Fluent[] openFluents = 
				((FluentNetworkSolver) getGroundSolver()).getOpenFluents(taskFluent.getAllenInterval());
		
		for (Fluent fl : openFluents) {
			if (checkViolation(fl)) {
				foundViolation = true;
				nw.addVariable(fl);
			}
		}
		
		if (foundViolation) {
			return nw;
		} else {
			return null;
		}
	}
	
	private boolean checkViolation(Fluent fl) {
		CompoundSymbolicVariable compVar = fl.getCompoundSymbolicVariable();
		if (compVar.getPredicateName().equals("On")) {
            return compVar.getGroundSymbolAt(2).equals("placingAreaWestRightTable1");
		}
		return false;
	}
	
	private List<FluentConstraint> getAxiomConstraints() {
		List<FluentConstraint> ret = new ArrayList<FluentConstraint>();
		for (Constraint con : this.getGroundSolver().getConstraints()) {
			if (con instanceof FluentConstraint) {
				if(((FluentConstraint) con).getType() == FluentConstraint.Type.AXIOM) {
					ret.add((FluentConstraint) con);
				}
			}
		}
		return ret;
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		// Cannot be resolved -> backtrack
		return null;
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
		return "AxiomMetaConstraint";
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
