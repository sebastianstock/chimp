package unify;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;

/**
 * 
 * @author Sebastian Stock
 */
public class NameMatchingConstraintSolver extends ConstraintSolver {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3449358967295197155L;
	
	public static final int MAX_VARS = 1000000;
	
	public NameMatchingConstraintSolver() {
		super(new Class[] {NameMatchingConstraint.class}, NameVariable.class);
		this.maxVars = MAX_VARS;
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}
	
	// Progressively increasing IDs for NameVariables
	protected int NVIDs = 0;
//	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	
	private int maxVars;

	@Override
	protected boolean addConstraintsSub(Constraint[] arg0) {
		// TODO Should we check the constraints here?
		return true;
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		NameVariable[] ret = new NameVariable[num];
		for(int i = 0; i < num; i++) ret[i] = new NameVariable(this, IDs++);
		return ret;
	}

	@Override
	public boolean propagate() {
//		for (int i = 0; i < this.getVariables().length; i++) {
//			getVaribaleById.put(this.getVariables()[i].getID(), this.getVariables()[i]);	
//		}
		Constraint[] cons = this.getConstraints();
		for(int i = 0; i < cons.length; i++) {
			if (!((NameMatchingConstraint) cons[i]).getResult())
				return false;
		}
		return true;
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeConstraintsSub(Constraint[] arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void removeVariablesSub(Variable[] arg0) {
		// TODO Auto-generated method stub

	}

}
