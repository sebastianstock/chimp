package unify;

import java.util.HashMap;
import java.util.Map.Entry;

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
	
	private HashMap<Integer, String> getVaribaleById = new HashMap<Integer, String>();
	
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
		return calcPropagation();
		
//		Constraint[] cons = this.getConstraints();
//		for(int i = 0; i < cons.length; i++) {
//			if (!((NameMatchingConstraint) cons[i]).getResult())
//				return false;
//		}
//		return true;
	}
	
	private boolean calcPropagation() {
		if(this.getConstraints().length == 0) return true;
		for (int i = 0; i < this.getVariables().length; i++) {
			getVaribaleById.put(this.getVariables()[i].getID(), 
					((NameVariable) this.getVariables()[i]).getName());
		}
		
		Constraint[] cons = this.getConstraints();
		boolean updated= true;
		boolean changedvars = false;
		while (updated == true) {
			updated = false;
			for (Constraint c : cons) {
				int fromId = ((NameMatchingConstraint) c).getFrom().getID();
				int toId = ((NameMatchingConstraint) c).getTo().getID();
				String fromStr = getVaribaleById.get(fromId);
				String toStr = getVaribaleById.get(toId);
				if (fromStr == null || toStr == null) {
					return false;
				}
				if (fromStr.charAt(0) == '?') {
					// fromStr is unground
					if(toStr.charAt(0) == '?') {
						// both unground
						// nothing to do
					} else {
						// change from to toStr
						getVaribaleById.put(fromId, toStr);
						updated = true;
					}
				} else {
					// fromStr is ground
					if(toStr.charAt(0) == '?') {
						// change to to fromStr
						getVaribaleById.put(toId, fromStr);
						updated = true;
					} else {
						if(fromStr.equals(toStr)) {
							// nothing to do
							// TODO constraint is satisfied and does not need to be checked next time.
						} else {
							return false;  // ground names do not match
						}
					}
				}
			}
			if (updated == true) {
				changedvars = true;
			}
		}
		if (changedvars == true) {
			for (Entry<Integer, String> e : getVaribaleById.entrySet()) {
				((NameVariable) getVariable(e.getKey())).setName(e.getValue());
			}
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
