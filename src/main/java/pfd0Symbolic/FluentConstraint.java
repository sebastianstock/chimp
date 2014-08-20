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

	public static enum Type {MATCHES, DC, PRE, OPENS, CLOSES, BEFORE, UNARYAPPLIED};

	private Type type;
	private int[] connections;
	private PlanReportroryItem plannedWith; // The operator or method that has been used for planning the task.

	public FluentConstraint(Type type) {
		this.type = type;
	}
	
	public FluentConstraint(Type type, int[] connections) {
		this(type);
		this.connections = connections;
	}
	
	public FluentConstraint(Type type, PlanReportroryItem plannedWith) {
		this(type);
		this.plannedWith = plannedWith;
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
			System.out.println("Connections: ");
			System.out.println(this.toString());
			if (connections != null && connections.length > 0) {
				for (int i = 0; i < connections.length; i++)
					System.out.print(connections[i] + " ");
				System.out.println("++++++++++++++++++++");
				return new Constraint[]{createSubmatches(f, t)};
			} else {
				System.out.println("NO CONNECTIONS");
				return null;
			}
		} else if (this.type.equals(Type.PRE)) {
			if (connections != null && connections.length > 0) {
				return new Constraint[]{createSubmatches(f, t)};
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
		FluentConstraint ret = new FluentConstraint(type, connections);
		ret.plannedWith = this.plannedWith;
		return ret;
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
	
	public PlanReportroryItem getPlannedWith() {
		return plannedWith;
	}

}
