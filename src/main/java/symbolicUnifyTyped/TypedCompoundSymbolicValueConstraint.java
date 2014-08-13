package symbolicUnifyTyped;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicVariable;

import unify.NameVariable;

public class TypedCompoundSymbolicValueConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1697594290767658650L;

	public static enum Type {MATCHES, SUBMATCHES};
	
	private Type type;
	private int[] connections;
	
	
	public TypedCompoundSymbolicValueConstraint(Type type) {
		this.type = type;
	}
	
	public TypedCompoundSymbolicValueConstraint(Type type, int[] connections) {
		this.type = type;
		this.connections = connections;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof TypedCompoundSymbolicVariable) || 
				!(t instanceof TypedCompoundSymbolicVariable)) {
			return null;
		}
		
		Variable[] finternals = ((TypedCompoundSymbolicVariable) f).getInternalVariables();
		Variable[] tinternals = ((TypedCompoundSymbolicVariable) t).getInternalVariables();
		Vector<SymbolicValueConstraint> constraints = 
				new Vector<SymbolicValueConstraint>(finternals.length);
		if (this.type.equals(Type.MATCHES)) {
			for(int i = 0; i < finternals.length; i++) {
				SymbolicValueConstraint con = 
						new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
				con.setFrom(finternals[i]);
				con.setTo(tinternals[i]);
				for (int j = 0; j < finternals.length; j++) {
					if (j != i) {
						con.skipSolver(((MultiVariable)f).getInternalConstraintSolvers()[j]);
					}
				}
				constraints.add(con);
			} 
//			SymbolicValueConstraint con = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
//			con.setFrom(finternals[0]);
//			con.setTo(tinternals[0]);
//			return new Constraint[] {con};
			return constraints.toArray(new Constraint[constraints.size()]);
			
		}	
//		else if (this.type.equals(Type.SUBMATCHES)) {
//			if(connections != null && ((connections.length % 2) == 0)) {
//				int i = 0;
//				while (i < connections.length) {
//					NameMatchingConstraint con = new NameMatchingConstraint();
//					if(finternals.length > connections[i] + 1 && finternals.length > connections[i+1] + 1) {
//						if(((NameVariable) finternals[connections[i] + 1]).getName().length() > 0 
//								&& ((NameVariable) tinternals[connections[i+1] + 1]).getName().length() > 0) {
//							con.setFrom(finternals[connections[i] + 1]);
//							con.setTo(tinternals[connections[i+1] + 1]);
//							constraints.add(con);
//						}
//					}
//					i += 2;
//				}
//				return constraints.toArray(new Constraint[constraints.size()]);
//			}
//		}
		
		return null;
	}

	@Override
	public Object clone() {
		return new TypedCompoundSymbolicValueConstraint(this.type);   // TODO: ADD CONNECTIONS ????
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
