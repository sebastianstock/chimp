/**
 * 
 */
package pfd0;

import java.io.Serializable;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;

/**
 * @author Sebastian Stock
 *
 */
public class TaskNetworkSolver extends MultiConstraintSolver {


	private static final long serialVersionUID = -7690871419246278011L;
	protected int IDs = 0;


/*	protected TaskNetworkSolver(Class<?>[] constraintTypes, Class<?> variableType,
			ConstraintSolver[] internalSolvers, int[] ingredients, long origin, long horizon) {
		super(constraintTypes, variableType, internalSolvers, ingredients);
	}
*/

	public TaskNetworkSolver(long origin, long horizon) {
		super(new Class[] {AllenIntervalConstraint.class, SymbolicValueConstraint.class}, //TODO DecompositionConstraint.class}
				Activity.class,
				createConstraintSolvers(origin, horizon, -1),
				new int[] {1, 1});
		
				
	}
	
	
	protected static ConstraintSolver[] createConstraintSolvers(long origin,
			long horizon, int numActivities) {
		ConstraintSolver[] ret = numActivities != -1 ? 
//				new ConstraintSolver[] {new ActivityNetworkSolver(origin, horizon, numActivities)} :new ConstraintSolver[] {new ActivityNetworkSolver(origin, horizon)};
				new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon, numActivities), new SymbolicVariableConstraintSolver()}
				:new ConstraintSolver[] {new AllenIntervalNetworkSolver(origin, horizon), new SymbolicVariableConstraintSolver()};
		return ret;
	}
	

	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

}
