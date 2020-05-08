package unify;

import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.utility.logging.MetaCSPLogging;

public class CompoundSymbolicValueConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1697594290767658650L;

	public enum Type {MATCHES, SUBMATCHES, POSITIVEVALUERESTRICTION, NEGATIVEVALUERESTRICTION, SUBDIFFERENT}

    private Type type;
	private int[] connections;
	private int[] restrictionIndices;
	private String[][] restrictions;
	
	private static final Logger logger = MetaCSPLogging.getLogger(CompoundSymbolicValueConstraint.class);
	
	
	public CompoundSymbolicValueConstraint(Type type) {
		this.type = type;
	}
	
	public CompoundSymbolicValueConstraint(Type type, int[] connections) {
		this.type = type;
		this.connections = connections;
	}
	
	public CompoundSymbolicValueConstraint(Type type, int[] indices, String[][] restrictions) {
		this.type = type;
		this.restrictionIndices = indices;
		this.restrictions =  restrictions;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof CompoundSymbolicVariable) || 
				!(t instanceof CompoundSymbolicVariable)) {
			return null;
		}
	
		Variable[] finternals = ((CompoundSymbolicVariable) f).getInternalVariables();
		Variable[] tinternals = ((CompoundSymbolicVariable) t).getInternalVariables();
		ConstraintSolver[] finternalSolvers = ((MultiVariable)f).getInternalConstraintSolvers();
		
		int[] varIndex2solverIndex = 
				((CompoundSymbolicVariableConstraintSolver) f.getConstraintSolver()).getVarIndex2solverIndex();
		
		if (this.type.equals(Type.MATCHES)) {
			Vector<NameMatchingConstraint> constraints = 
					new Vector<NameMatchingConstraint>(finternals.length);
			for(int i = 0; i < finternals.length; i++) {
				NameMatchingConstraint con = 
						new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
				con.setFrom(finternals[i]);
				con.setTo(tinternals[i]);
				skipIrrelevantSolvers(con, finternalSolvers, varIndex2solverIndex[i]);
				constraints.add(con);
			} 
			return constraints.toArray(new Constraint[constraints.size()]);	
		}	else if (this.type.equals(Type.SUBMATCHES)) {
			logger.fine("CREATING SUBMATCHES INTERNAL CONSTRAINTS");
			if(connections != null && ((connections.length % 2) == 0)) {
				Vector<NameMatchingConstraint> constraints = 
						new Vector<NameMatchingConstraint>(connections.length / 2);
				int i = 0;
				while (i < connections.length) {
					NameMatchingConstraint con = new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
					if(finternals.length > connections[i] + 1 && tinternals.length > connections[i+1] + 1) {
						con.setFrom(finternals[connections[i] + 1]);
						con.setTo(tinternals[connections[i+1] + 1]);
						skipIrrelevantSolvers(con, finternalSolvers, varIndex2solverIndex[connections[i] + 1]);
						constraints.add(con);
					}
					i += 2;
				}
				return constraints.toArray(new Constraint[constraints.size()]);
			}
		} else if (this.type.equals(Type.SUBDIFFERENT)) {
			logger.fine("CREATING SUBDIFFERENT INTERNAL CONSTRAINTS");
			if(connections != null && ((connections.length % 2) == 0)) {
				Vector<NameMatchingConstraint> constraints = 
						new Vector<NameMatchingConstraint>(connections.length / 2);
				int i = 0;
				while (i < connections.length) {
					NameMatchingConstraint con = new NameMatchingConstraint(NameMatchingConstraint.Type.DIFFERENT);
					if(finternals.length > connections[i] + 1 && tinternals.length > connections[i+1] + 1) {
						con.setFrom(finternals[connections[i] + 1]);
						con.setTo(tinternals[connections[i+1] + 1]);
						skipIrrelevantSolvers(con, finternalSolvers, varIndex2solverIndex[connections[i] + 1]);
						constraints.add(con);
					}
					i += 2;
				}
				return constraints.toArray(new Constraint[constraints.size()]);
			}
		} else if (this.type.equals(Type.POSITIVEVALUERESTRICTION)) {
			logger.fine("CREATING POSITIVEVALUERESTRICTION INTERNAL CONSTRAINTS");
			if (restrictionIndices != null 
					&& restrictions != null 
					&& restrictionIndices.length == restrictions.length) {
				Constraint[] constraints = new Constraint[restrictionIndices.length];
				for (int i = 0; i < restrictionIndices.length; i++) {
					NameMatchingConstraint con = 
							new NameMatchingConstraint(NameMatchingConstraint.Type.UNARYEQUALS);
					con.setFrom(finternals[restrictionIndices[i] + 1]);
					con.setTo(finternals[restrictionIndices[i] + 1]);
					int solverIndex = varIndex2solverIndex[restrictionIndices[i] + 1];
					int[] unaryValues = 
							((NameMatchingConstraintSolver) finternalSolvers[solverIndex]).getIndexArrayOfSymbols(restrictions[i]);
					con.setUnaryValues(unaryValues);
					
					skipIrrelevantSolvers(con, finternalSolvers, solverIndex);
					constraints[i] = con;
				}
				return constraints;
			}
		} else if (this.type.equals(Type.NEGATIVEVALUERESTRICTION)) {
			logger.fine("CREATING NEGATIVEVALUERESTRICTION INTERNAL CONSTRAINTS");
			if (restrictionIndices != null 
					&& restrictions != null 
					&& restrictionIndices.length == restrictions.length) {
				Constraint[] constraints = new Constraint[restrictionIndices.length];
				for (int i = 0; i < restrictionIndices.length; i++) {
					NameMatchingConstraint con = 
							new NameMatchingConstraint(NameMatchingConstraint.Type.UNARYDIFFERENT);
					con.setFrom(finternals[restrictionIndices[i] + 1]);
					con.setTo(finternals[restrictionIndices[i] + 1]);
					int solverIndex = varIndex2solverIndex[restrictionIndices[i] + 1];
					int[] unaryValues = 
							((NameMatchingConstraintSolver) finternalSolvers[solverIndex]).getIndexArrayOfSymbols(restrictions[i]);
					con.setUnaryValues(unaryValues);
					
					skipIrrelevantSolvers(con, finternalSolvers, solverIndex);
					constraints[i] = con;
				}
				return constraints;
			}
		}
		
		return null;
	}
	
	private static void skipIrrelevantSolvers(Constraint con, ConstraintSolver[] internalSolvers, int solverIndex) {
		for (int j = 0; j < internalSolvers.length; j++) {
			if (j != solverIndex) {
				con.skipSolver(internalSolvers[j]);
			}
		}
	}

	@Override
	public Object clone() {
		if (this.connections != null) {
			return new CompoundSymbolicValueConstraint(this.type, connections);
		} else {
			return new CompoundSymbolicValueConstraint(this.type, 
					this.restrictionIndices, 
					this.restrictions);
		}
	}

	@Override
	public String getEdgeLabel() {
		if (this.type.equals(Type.SUBMATCHES)) {
			return this.type.toString();
		} else {	
			return this.type.toString();
		}
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Type getType() {
		return type;
	}

}
