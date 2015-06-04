package htn;

import htn.TaskApplicationMetaConstraint.markings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import unify.CompoundSymbolicValueConstraint;
import fluentSolver.Fluent;
import fluentSolver.FluentConstraint;
import fluentSolver.FluentNetworkSolver;


public class HTNPlanner extends MetaConstraintSolver {

	private static final long serialVersionUID = 8031573555691611305L;
	public static final String FUTURE_STR = "Eternity";
	
	private Map<String, String[]> typesInstancesMap;
	
	private Map<Fluent, Constraint> meetsFutureMap;
	private final Fluent future;
	private final FluentNetworkSolver groundSolver;
	
	private Logger logger = MetaCSPLogging.getLogger(HTNPlanner.class);

	public HTNPlanner(long origin, long horizon, long animationTime, String[][] symbols, 
			int[] symbolicingredients) {
		// Currently only FluentConstraints. Other constraint should be added later.
		super(new Class[] {FluentConstraint.class}, 
				animationTime, 
				new FluentNetworkSolver(origin, horizon, symbols, symbolicingredients));
		
		logger = MetaCSPLogging.getLogger(HTNPlanner.class);
		groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
		// create future fluent
		future = (Fluent)groundSolver.createVariable("Future");
		future.setMarking(markings.JUSTIFIED);
		future.setName(FUTURE_STR + "()");
		AllenIntervalConstraint futureRelease = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(horizon,horizon));
		futureRelease.setFrom(future);
		futureRelease.setTo(future);
		groundSolver.addConstraint(futureRelease);
	}

	@Override
	public void preBacktrack() { }

	@Override
	public void postBacktrack(MetaVariable mv) {
//		if (mv.getMetaConstraint() instanceof TaskApplicationMetaConstraint) {
//			for (Variable v : mv.getConstraintNetwork().getVariables()) {
//				v.setMarking(markings.SELECTED);
//			}
//		}
//		else if (mv.getMetaConstraint() instanceof PreconditionMetaConstraint) {
//			for (Variable v : mv.getConstraintNetwork().getVariables()) {
//				v.setMarking(markings.UNJUSTIFIED);
//			}
//		} 
//		else if (mv.getMetaConstraint() instanceof HTNMetaConstraint) {
//			for (Variable v : mv.getConstraintNetwork().getVariables()) {
//				v.setMarking(markings.UNPLANNED);
//				System.out.println("SET TO UNPLANNED: " + v);
//			}
//		} 
	}

	/**
	 * Defines extra operations that should happen
	 * after retracting a meta-value in the meta-CSP search (e.g., when backtracking).
	 * @param metaVariable The {@link MetaVariable} over which backtracking occurs.
	 * @param metaValue The meta-value that has been retracted.
	 */
	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		
		long startTime = System.nanoTime();
		
		// remove added fluents
		Vector<Variable> varsToRemove = new Vector<Variable>();
		for (Variable v : metaValue.getVariables()) {
			if (!metaVariable.containsVariable(v)) {
				if (v instanceof VariablePrototype) {
					Variable vReal = metaValue.getSubstitution((VariablePrototype)v);
					if (vReal != null) {
							varsToRemove.add(vReal);
//							System.out.println("GONNA REMOVE  " + vReal);
					}
				}
			}
		}
		groundSolver.removeVariables(varsToRemove.toArray(new Variable[varsToRemove.size()]));
		
		// change CLOSED fluents back to OPEN and re-add meets future constraints
		List<AllenIntervalConstraint> additionalMeetsFuture = new ArrayList<AllenIntervalConstraint>();
		for(Constraint c : metaValue.getConstraints()) {
			if ((c instanceof FluentConstraint) && 
					(((FluentConstraint) c).getType() == FluentConstraint.Type.CLOSES)) {
				((FluentConstraint) c).getTo().setMarking(markings.OPEN);
				
				AllenIntervalConstraint meetsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meetsFuture.setFrom(((FluentConstraint) c).getTo());
				meetsFuture.setTo(future);
				additionalMeetsFuture.add(meetsFuture);
			}
		}
		if(additionalMeetsFuture.size() > 0) {
			if(! groundSolver.addConstraints(additionalMeetsFuture.toArray(new Constraint[additionalMeetsFuture.size()]))) {
				logger.info("Could not re-add meet future constraints");
			} else {
				for (AllenIntervalConstraint con : additionalMeetsFuture) {
					meetsFutureMap.put((Fluent) con.getFrom(), con);
				}
			}
		}
		
		long endTime = System.nanoTime();
		logger.finest("RECTRACT_RESOLVER_SUB Took: " + ((endTime - startTime) / 1000000) + " ms");
	}

	/**
	 * Defines additional operations that should happen before
	 * adding a meta-value in the meta-CSP search (e.g., when branching).
	 * 
	 * Make real {@link Variable}s from {@link VariablePrototype}s.
	 * Involve real {@link Variable}s in the Constraints.
	 * @param metaVariable The {@link MetaVariable} over which the search is branching.
	 * @param metaValue The meta-value that has been selected (the branch). 
	 */
	@Override
	protected boolean addResolverSub(ConstraintNetwork metaVariable,
			ConstraintNetwork metaValue) {
		
		long startTime = System.nanoTime();
		
		FluentNetworkSolver groundSolver = (FluentNetworkSolver)this.getConstraintSolvers()[0];
		
		List<AllenIntervalConstraint> additionalMeetsFutureCons = new ArrayList<AllenIntervalConstraint>();
		
		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component 
				//  and the second is the type, the third are the arguments of the fluent
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Fluent fluent = (Fluent)groundSolver.createVariable(component);
				
				String[] arguments = (String[])((VariablePrototype) v).getParameters()[2];
				fluent.setName(symbol, arguments);

				fluent.setMarking(v.getMarking());
				metaValue.addSubstitution((VariablePrototype)v, fluent);
				
				// add meets future constraints for positive effects
				if(v.getMarking().equals(markings.OPEN)) {
					AllenIntervalConstraint meetsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
					meetsFuture.setFrom(fluent);
					meetsFuture.setTo(future);
//					metaValue.addConstraint(meetsFuture);  // this is done below
					additionalMeetsFutureCons.add(meetsFuture);
					meetsFutureMap.put(fluent, meetsFuture);
				}
			}
		}

		//Involve real variables in the constraints
		for (Constraint con : metaValue.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();  
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			for (int i = 0; i < oldScope.length; i++) {
				if (oldScope[i] instanceof VariablePrototype) {
					if (con instanceof CompoundSymbolicValueConstraint){
						newScope[i] = ((Fluent) metaValue.getSubstitution((VariablePrototype)oldScope[i])).getCompoundSymbolicVariable();
					} else if (con instanceof AllenIntervalConstraint){ 
						newScope[i] = ((Fluent) metaValue.getSubstitution((VariablePrototype)oldScope[i])).getAllenInterval();
					}else {	
						newScope[i] = metaValue.getSubstitution((VariablePrototype)oldScope[i]);
					}
				}
				else {
					newScope[i] = oldScope[i];
				}
			}
			clonedConstraint.setScope(newScope);
			metaValue.removeConstraint(con);
			metaValue.addConstraint(clonedConstraint);
		}
		
		metaValue.addConstraints(additionalMeetsFutureCons.toArray(new AllenIntervalConstraint[additionalMeetsFutureCons.size()]));
		
		
		// set marking of closed fluents to CLOSED and remove connection to the future
		List<Constraint> constraintsToRemove = new ArrayList<Constraint>();
		for (Constraint con : metaValue.getConstraints()) {
			if (con instanceof FluentConstraint) {
				if (((FluentConstraint) con).getType() == FluentConstraint.Type.CLOSES) {
					((FluentConstraint) con).getTo().setMarking(markings.CLOSED);
					Fluent to = (Fluent) ((FluentConstraint) con).getTo();
					Constraint meetsConstraints = meetsFutureMap.remove(to);
					constraintsToRemove.add(meetsConstraints);
				}
			}
		}
		
		if (constraintsToRemove.size() > 0 ) {
			logger.fine("Removing future constraints: " + constraintsToRemove);
			this.getConstraintSolvers()[0].removeConstraints(constraintsToRemove.toArray(new Constraint[constraintsToRemove.size()]));
		}

		
		long endTime = System.nanoTime();
		logger.finest("ADD_RESOLVER_SUB Took: " + ((endTime - startTime) / 1000000) + " ms");
				
		return true;
	}

	@Override
	protected double getUpperBound() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// Auto-generated method stub

	}

	@Override
	protected double getLowerBound() {
		// Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// Auto-generated method stub

	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// Auto-generated method stub

	}

	public Map<String, String[]> getTypesInstancesMap() {
		return typesInstancesMap;
	}

	public void setTypesInstancesMap(Map<String, String[]> typesInstancesMap) {
		this.typesInstancesMap = typesInstancesMap;
	}

	public Map<Fluent, Constraint> getMeetsFutureMap() {
		return meetsFutureMap;
	}

	public Fluent getFutureFluent() {
		return future;
	}

	/**
	 * Adds meets constraint between open fluents and the future fluent.
	 */
	public void createInitialMeetsFutureConstraints() {
		// set meets future constraints
		meetsFutureMap = new HashMap<Fluent, Constraint>();
		for (Variable var : groundSolver.getVariables()) {
			if (var.getMarking().equals(markings.OPEN)) {
				AllenIntervalConstraint meetsFuture = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
				meetsFuture.setFrom(var);
				meetsFuture.setTo(future);
				if(groundSolver.addConstraint(meetsFuture)) {
					meetsFutureMap.put((Fluent) var, meetsFuture);
				}
			}
		}
	}
	

}
