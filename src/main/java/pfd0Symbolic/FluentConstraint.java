package pfd0Symbolic;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;

import simpleBooleanValueCons.SimpleBooleanValueConstraint;
import simpleBooleanValueCons.SimpleBooleanValueVariable;
import symbolicUnifyTyped.TypedCompoundSymbolicValueConstraint;
import symbolicUnifyTyped.TypedCompoundSymbolicVariable;

public class FluentConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 137380711080409334L;

	public static enum Type {MATCHES, DC, PRE, OPENS, CLOSES, BEFORE};

	private Type type;
	private int[] connections;

	public FluentConstraint(Type type) {
		this.type = type;
	}
	
	public FluentConstraint(Type type, int[] connections) {
		this.type = type;
		this.connections = connections;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof Fluent) || !(t instanceof Fluent)) return null;

		if (this.type.equals(Type.MATCHES)) {
			SimpleBooleanValueConstraint scon = 
					new SimpleBooleanValueConstraint(SimpleBooleanValueConstraint.Type.EQUALS);
			SimpleBooleanValueVariable bf = ((Fluent) f).getSimpleBooleanValueVariable();
			scon.setFrom(bf);
			SimpleBooleanValueVariable bt = ((Fluent) t).getSimpleBooleanValueVariable();
			scon.setTo(bt);

			TypedCompoundSymbolicValueConstraint ncon = 
					new TypedCompoundSymbolicValueConstraint(TypedCompoundSymbolicValueConstraint.Type.MATCHES);
			TypedCompoundSymbolicVariable nf = ((Fluent) f).getCompoundSymbolicVariable();
			ncon.setFrom(nf);
			TypedCompoundSymbolicVariable nt = ((Fluent) t).getCompoundSymbolicVariable();
			ncon.setTo(nt);
			return new Constraint[]{scon, ncon};
			
		} else if (this.type.equals(Type.DC)) {
			// TODO nothing to add here?
		} else if (this.type.equals(Type.PRE)) {
			if (connections != null && connections.length > 0) {
				TypedCompoundSymbolicValueConstraint ncon = 
						new TypedCompoundSymbolicValueConstraint(TypedCompoundSymbolicValueConstraint.Type.SUBMATCHES, 
								connections);
				TypedCompoundSymbolicVariable nf = ((Fluent) f).getCompoundSymbolicVariable();
				ncon.setFrom(nf);
				TypedCompoundSymbolicVariable nt = ((Fluent) t).getCompoundSymbolicVariable();
				ncon.setTo(nt);
				return new Constraint[]{ncon};
			} else {
				return null;
			}
		} else if (this.type.equals(Type.OPENS)) {
			if(connections != null) {
				return new Constraint[]{createSubmatches(f, t)};
			} else {
				return null;
			}
		}

		return null;
	}
	
	private TypedCompoundSymbolicValueConstraint createSubmatches(Variable f, Variable t) {
		TypedCompoundSymbolicValueConstraint ncon = new TypedCompoundSymbolicValueConstraint(
				TypedCompoundSymbolicValueConstraint.Type.SUBMATCHES, 
				connections);
		TypedCompoundSymbolicVariable nf = ((Fluent) f).getCompoundSymbolicVariable();
		ncon.setFrom(nf);
		TypedCompoundSymbolicVariable nt = ((Fluent) t).getCompoundSymbolicVariable();
		ncon.setTo(nt);
		return ncon;
	}

	@Override
	public Object clone() {
		return new FluentConstraint(type, connections);
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
