package pfd0Symbolic;

import java.util.ArrayList;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

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
	private boolean isNegativeEffect;


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
		
		// TODO: Refactor:
		
		if (this.type.equals(Type.MATCHES)) {
			AllenIntervalConstraint eq = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.Equals);
			eq.setFrom(((Fluent) f).getAllenInterval());
			eq.setTo(((Fluent) t).getAllenInterval());
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
			return new Constraint[]{scon, ncon, eq};
			
		} else if (this.type.equals(Type.DC)) {
			AllenIntervalConstraint desf = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes, 
					AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes.getDefaultBounds());
			desf.setFrom(((Fluent) t).getAllenInterval());
			desf.setTo(((Fluent) f).getAllenInterval());
			if (connections != null && connections.length > 0) {
				return new Constraint[]{createSubmatches(f, t), desf};
			} else {
				return new Constraint[]{desf};
			}
		} else if (this.type.equals(Type.PRE)) {
			AllenIntervalConstraint preAIC = 
					new AllenIntervalConstraint(
							AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring, 
							AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring.getDefaultBounds());
			preAIC.setFrom(((Fluent) t).getAllenInterval());
			preAIC.setTo(((Fluent) f).getAllenInterval());
			if (connections != null && connections.length > 0) {
				return new Constraint[]{createSubmatches(f, t), preAIC};
			} else {
				return new Constraint[]{preAIC};
			}
		} else if (this.type.equals(Type.OPENS)) { // TODO probably need other relations, too
//			AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
			AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, new Bounds(2L, 6L));
			befCon.setFrom(((Fluent) f).getAllenInterval());
			befCon.setTo(((Fluent) t).getAllenInterval());
			if(connections != null) {
				return new Constraint[]{createSubmatches(f, t), befCon};
			} else {
				return new Constraint[]{befCon};
			}
		} else if (this.type.equals(Type.BEFORE)) {
			AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before);
			befCon.setFrom(((Fluent) f).getAllenInterval());
			befCon.setTo(((Fluent) t).getAllenInterval());
			if(connections != null) {
				return new Constraint[]{createSubmatches(f, t), befCon};
			} else {
				return new Constraint[]{befCon};
			}
		}
		else if (this.type.equals(Type.CLOSES)) { // TODO probably need other relations, too
			AllenIntervalConstraint oiCon = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.OverlappedBy,
					AllenIntervalConstraint.Type.OverlappedBy.getDefaultBounds());
			oiCon.setFrom(((Fluent) f).getAllenInterval());
			oiCon.setTo(((Fluent) t).getAllenInterval());
			if(connections != null) {
				return new Constraint[]{createSubmatches(f, t), oiCon};
			} else {
				return new Constraint[]{oiCon};
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
		ret.setNegativeEffect(isNegativeEffect);
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
	
	public boolean isNegativeEffect() {
		return isNegativeEffect;
	}

	public void setNegativeEffect(boolean isNegativeEffect) {
		this.isNegativeEffect = isNegativeEffect;
	}

}
