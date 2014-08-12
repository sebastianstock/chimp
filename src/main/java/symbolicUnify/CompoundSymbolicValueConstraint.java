package symbolicUnify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;

public class CompoundSymbolicValueConstraint extends MultiBinaryConstraint {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3192732063748750660L;

	public static enum Type {MATCHES, SUBMATCHES};
	
	private Type type;
	private int[] connections;
	
	
	public CompoundSymbolicValueConstraint(Type type) {
		this.type = type;
	}
	
	public CompoundSymbolicValueConstraint(Type type, int[] connections) {
		this.type = type;
		this.connections = connections;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
// TODO UPDATE + UNCOMMENT
//		if (!( f instanceof CompoundSymbolicVariable) || !(t instanceof CompoundSymbolicVariable)) {
//			return null;
//		}
//		
//		Variable[] finternals = ((CompoundSymbolicVariable) f).getInternalVariables();
//		Variable[] tinternals = ((CompoundSymbolicVariable) t).getInternalVariables();
//		Vector<NameMatchingConstraint> constraints = 
//				new Vector<NameMatchingConstraint>(finternals.length);
//		if (this.type.equals(Type.MATCHES)) {
//			for(int i = 0; i < finternals.length; i++) {
//				NameMatchingConstraint con = new NameMatchingConstraint();
//				if(((NameVariable) finternals[i]).getName().length() > 0 
//						&& ((NameVariable) tinternals[i]).getName().length() > 0) {
//					con.setFrom(finternals[i]);
//					con.setTo(tinternals[i]);
//					constraints.add(con);
//				}
//			} 
//			return constraints.toArray(new Constraint[constraints.size()]);
//			
//		}	else if (this.type.equals(Type.SUBMATCHES)) {
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
//		
		return null;
	}

	@Override
	public Object clone() {
		return new CompoundSymbolicValueConstraint(this.type);
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
