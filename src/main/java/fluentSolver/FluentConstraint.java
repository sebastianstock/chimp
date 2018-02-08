package fluentSolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import htn.AdditionalConstraintTemplate;
import htn.PlanReportroryItem;
import resourceFluent.ResourceUsageTemplate;
import unify.CompoundSymbolicValueConstraint;
import unify.CompoundSymbolicVariable;

public class FluentConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 137380711080409334L;

	public enum Type {MATCHES, DC, PRE, OPENS, CLOSES, BEFORE, UNARYAPPLIED, MOVEDURATION,
		AXIOM, RESOURCEUSAGE}

    private Type type;
	private int[] connections;
	private PlanReportroryItem plannedWith; // The operator or method that has been used for planning the task.
	private boolean isNegativeEffect;
	private Bounds bounds;
	private String axiom;
	private ResourceUsageTemplate resourceIndicator;
	private Vector<AdditionalConstraintTemplate> additionalConstraints;
	private int depth; // depth of the task in the decomposition tree (only set for UNARYAPPLIED
	private boolean depthIsSet = false;

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
	
	public FluentConstraint(Type type, Bounds bounds) {
		this(type);
		this.bounds = bounds;
	}
	
	public FluentConstraint(Type type, String axiom) {
		this(type);
		this.axiom = axiom;
	}
	
	public FluentConstraint(Type type, ResourceUsageTemplate resourceIndicator) {
		this(type);
		this.resourceIndicator = resourceIndicator;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!( f instanceof Fluent) || !(t instanceof Fluent)) return null;
		
		List<Constraint> retList = new ArrayList<Constraint>();
		
		if (this.type.equals(Type.MATCHES)) {
			AllenIntervalConstraint eq = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.Equals);
			eq.setFrom(((Fluent) f).getAllenInterval());
			eq.setTo(((Fluent) t).getAllenInterval());
			retList.add(eq);

			CompoundSymbolicValueConstraint ncon = 
					new CompoundSymbolicValueConstraint(CompoundSymbolicValueConstraint.Type.MATCHES);
			CompoundSymbolicVariable nf = ((Fluent) f).getCompoundSymbolicVariable();
			ncon.setFrom(nf);
			CompoundSymbolicVariable nt = ((Fluent) t).getCompoundSymbolicVariable();
			ncon.setTo(nt);
			retList.add(ncon);
			
		} else if (this.type.equals(Type.DC)) {
			if (additionalConstraints != null && additionalConstraints.size() > 0) { 
				for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
					AllenIntervalConstraint con = (AllenIntervalConstraint) aConTemplate.getConstraint().clone();
					if (aConTemplate.startsFromHead()) {
						con.setFrom(((Fluent) f).getAllenInterval());
						con.setTo(((Fluent) t).getAllenInterval());
					} else {
						con.setFrom(((Fluent) t).getAllenInterval());
						con.setTo(((Fluent) f).getAllenInterval());
					}
					retList.add(con);
				}
			} else {
				AllenIntervalConstraint desf = new AllenIntervalConstraint(
						AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes, 
						AllenIntervalConstraint.Type.DuringOrEqualsOrStartsOrFinishes.getDefaultBounds());
				desf.setFrom(((Fluent) t).getAllenInterval());
				desf.setTo(((Fluent) f).getAllenInterval());
				retList.add(desf);
			}
			
		} else if (this.type.equals(Type.PRE)) {
			if (additionalConstraints != null && additionalConstraints.size() > 0) { 
				for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
					AllenIntervalConstraint con = (AllenIntervalConstraint) aConTemplate.getConstraint().clone();
					if (aConTemplate.startsFromHead()) {
						con.setFrom(((Fluent) t).getAllenInterval());
						con.setTo(((Fluent) f).getAllenInterval());
					} else {
						con.setFrom(((Fluent) f).getAllenInterval());
						con.setTo(((Fluent) t).getAllenInterval());
					}
					retList.add(con);
				}
			} else {  // use default temporal constraints
//				AllenIntervalConstraint preAIC = new AllenIntervalConstraint(
//						AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring, 
//						AllenIntervalConstraint.Type.MetByOrOverlappedByOrIsFinishedByOrDuring.getDefaultBounds());
//				preAIC.setFrom(((Fluent) t).getAllenInterval());
//				preAIC.setTo(((Fluent) f).getAllenInterval());
				AllenIntervalConstraint preAIC = new AllenIntervalConstraint(
						AllenIntervalConstraint.Type.MeetsOrOverlapsOrFinishedByOrContains, 
						AllenIntervalConstraint.Type.MeetsOrOverlapsOrFinishedByOrContains.getDefaultBounds());
				preAIC.setFrom(((Fluent) f).getAllenInterval());
				preAIC.setTo(((Fluent) t).getAllenInterval());
				retList.add(preAIC);
			}
			
		} else if (this.type.equals(Type.OPENS)) { 
			if (additionalConstraints != null && additionalConstraints.size() > 0) { 
				for (AdditionalConstraintTemplate aConTemplate : additionalConstraints) {
					AllenIntervalConstraint con = (AllenIntervalConstraint) aConTemplate.getConstraint().clone();
					if (aConTemplate.startsFromHead()) {
						con.setFrom(((Fluent) f).getAllenInterval());
						con.setTo(((Fluent) t).getAllenInterval());
					} else {
						con.setFrom(((Fluent) t).getAllenInterval());
						con.setTo(((Fluent) f).getAllenInterval());
					}
					retList.add(con);
				}
			} else {
				// TODO probably need other relations, too
				//			AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				//			AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, new Bounds(2L, 6L));
				AllenIntervalConstraint befCon = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.Overlaps.getDefaultBounds());
				befCon.setFrom(((Fluent) f).getAllenInterval());
				befCon.setTo(((Fluent) t).getAllenInterval());
				retList.add(befCon);
			}
			
		} else if (this.type.equals(Type.BEFORE)) {
			// Only used for ordering. No temporal constraints to set

		} else if (this.type.equals(Type.CLOSES)) { 
			if (additionalConstraints != null && additionalConstraints.size() > 0) { 
				// Additional constraints are added in pre at the moment.

			} else {
				// TODO probably need other relations, too
				AllenIntervalConstraint oiCon = new AllenIntervalConstraint(
						AllenIntervalConstraint.Type.OverlappedBy,
						AllenIntervalConstraint.Type.OverlappedBy.getDefaultBounds());
				oiCon.setFrom(((Fluent) f).getAllenInterval());
				oiCon.setTo(((Fluent) t).getAllenInterval());
				retList.add(oiCon);
			}

		} else if (this.type.equals(Type.MOVEDURATION)) {
			AllenIntervalConstraint durationCon = new AllenIntervalConstraint(
					AllenIntervalConstraint.Type.Duration, this.bounds
					//					new Bounds(10, 100)
					);
			AllenInterval ai = ((Fluent) f).getAllenInterval();
			durationCon.setFrom(ai);
			durationCon.setTo(ai);
			retList.add(durationCon);	
		}
		
		if (connections != null && connections.length > 0) {
			retList.add(createSubmatches(f, t));
		}
		return retList.toArray(new Constraint[retList.size()]);
	}
	
	private CompoundSymbolicValueConstraint createSubmatches(Variable f, Variable t) {
		CompoundSymbolicValueConstraint ncon = new CompoundSymbolicValueConstraint(
				CompoundSymbolicValueConstraint.Type.SUBMATCHES, 
				connections);
		CompoundSymbolicVariable nf = ((Fluent) f).getCompoundSymbolicVariable();
		ncon.setFrom(nf);
		CompoundSymbolicVariable nt = ((Fluent) t).getCompoundSymbolicVariable();
		ncon.setTo(nt);
		return ncon;
	}

	@Override
	public Object clone() {
		FluentConstraint ret = new FluentConstraint(type);
		ret.connections = this.connections;
		ret.bounds = this.bounds;
		ret.plannedWith = this.plannedWith;
		ret.setNegativeEffect(isNegativeEffect);
		ret.axiom = this.axiom;
		ret.resourceIndicator = this.resourceIndicator;
		ret.additionalConstraints = this.additionalConstraints;
		ret.depth = this.depth;
		ret.depthIsSet = this.depthIsSet;
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
	
	public String getAxiom() {
		return axiom;
	}
	
	public boolean isUsingResource(String resource) {
		if (resourceIndicator.getResourceName().equals(resource)) {
			CompoundSymbolicVariable cv = ((Fluent) this.getFrom()).getCompoundSymbolicVariable();
			int[] resourceRequirementPositions = resourceIndicator.getResourceRequirementPositions();
			for (int i = 0; i < resourceRequirementPositions.length; i++) {
				String[] symbols = cv.getSymbolsAt(resourceRequirementPositions[i]);
				if (symbols.length != 1 || 
						!symbols[0].equals(resourceIndicator.getResourceRequirements()[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public String getResourceName() {
		return resourceIndicator.getResourceName();
	}
	
	public int getResourceUsageLevel() {
		return resourceIndicator.getResourceUsageLevel();
	}

	public void setAdditionalConstraints(Vector<AdditionalConstraintTemplate> additionalConstraints) {
		this.additionalConstraints = additionalConstraints;
	}
	
	public Vector<AdditionalConstraintTemplate> getAdditionalConstraints() {
		return additionalConstraints;
	}
	
	public boolean hasAdditionalConstraints() {
		return additionalConstraints.size() > 0;
	}

	public int getDepth() throws IllegalAccessException {
		if (this.depthIsSet) {
			return depth;
		} else {
			throw new IllegalAccessException("Depth has not been set");
		}
	}

	public void setDepth(int depth) {
		this.depth = depth;
		this.depthIsSet = true;
	}

}
