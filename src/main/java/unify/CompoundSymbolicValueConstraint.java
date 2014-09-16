package unify;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;


public class CompoundNameMatchingConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1697594290767658650L;

	public static enum Type {MATCHES, SUBMATCHES};
	
	private Type type;
	private int[] connections;
	
	
	public CompoundNameMatchingConstraint(Type type) {
		this.type = type;
	}
	
	public CompoundNameMatchingConstraint(Type type, int[] connections) {
		this.type = type;
		this.connections = connections;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof CompoundNameVariable) || 
				!(t instanceof CompoundNameVariable)) {
			return null;
		}
	
		Variable[] finternals = ((CompoundNameVariable) f).getInternalVariables();
		Variable[] tinternals = ((CompoundNameVariable) t).getInternalVariables();
		
		int[] varIndex2solverIndex = 
				((CompoundNameMatchingConstraintSolver) f.getConstraintSolver()).getVarIndex2solverIndex();
		
		if (this.type.equals(Type.MATCHES)) {
			Vector<NameMatchingConstraint> constraints = 
					new Vector<NameMatchingConstraint>(finternals.length);
			for(int i = 0; i < finternals.length; i++) {
				NameMatchingConstraint con = 
						new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
				con.setFrom(finternals[i]);
				con.setTo(tinternals[i]);
				for (int j = 0; j < ((MultiVariable)f).getInternalConstraintSolvers().length; j++) {
					if (j != varIndex2solverIndex[i]) {
						con.skipSolver(((MultiVariable)f).getInternalConstraintSolvers()[j]);
					}
				}
				constraints.add(con);
			} 
			return constraints.toArray(new Constraint[constraints.size()]);	
		}	
		else if (this.type.equals(Type.SUBMATCHES)) {
			if(connections != null && ((connections.length % 2) == 0)) {
				Vector<NameMatchingConstraint> constraints = 
						new Vector<NameMatchingConstraint>(connections.length / 2);
				int i = 0;
				while (i < connections.length) {
					NameMatchingConstraint con = new NameMatchingConstraint(NameMatchingConstraint.Type.EQUALS);
					if(finternals.length > connections[i] + 1 && tinternals.length > connections[i+1] + 1) {
						con.setFrom(finternals[connections[i] + 1]);
						con.setTo(tinternals[connections[i+1] + 1]);
						
						for (int j = 0; j < ((MultiVariable)f).getInternalConstraintSolvers().length; j++) {
							if (j != varIndex2solverIndex[connections[i] + 1]) {
								con.skipSolver(((MultiVariable)f).getInternalConstraintSolvers()[j]);
							}
						}
						
						constraints.add(con);
					}
					i += 2;
				}
				return constraints.toArray(new Constraint[constraints.size()]);
			}
		}
		
		return null;
	}

	@Override
	public Object clone() {
		return new CompoundNameMatchingConstraint(this.type, connections);
		
	}

	@Override
	public String getEdgeLabel() {
		return this.type.toString();
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
