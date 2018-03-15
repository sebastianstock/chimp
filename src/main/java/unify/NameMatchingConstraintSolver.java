package unify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
	private String[] symbols;
	private Map<String, Integer> symbols2Index;
	private final Map<Variable, List<NameMatchingConstraint>> var2Constraints = new HashMap<>();
	
	private long propagationTime = 0;
	private int invocationCNT = 0;
	private long invocGetConstraintsOfVarCNT = 0;

	// TODO check if really needed:
	private HashMap<Integer, String> getVaribaleById = new HashMap<Integer, String>();
	
	public NameMatchingConstraintSolver(String[] symbols) {
		super(new Class[] {NameMatchingConstraint.class}, NameVariable.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
		this.symbols = symbols;
		symbols2Index = new HashMap<String, Integer>();
		for (int i = 0; i < symbols.length; i++) {
			symbols2Index.put(symbols[i], new Integer(i));
		}
	}
	
	// Progressively increasing IDs for NameVariables
	protected int NVIDs = 0;
//	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();


	@Override
	protected boolean addConstraintsSub(Constraint[] arg0) {
		// TODO Should we check the constraints here?
		return true;
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		NameVariable[] ret = new NameVariable[num];
		for(int i = 0; i < num; i++) ret[i] = new NameVariable(this, IDs++, symbols.length);
		return ret;
	}

	@Override
	public boolean propagate() {
//		long startTime = System.nanoTime();
		buildVar2Constraints();

		// create backup of old domains
		Variable[] vars = this.getVariables();
		NameDomain[] backUpDomains = new NameDomain[this.getVariables().length];
		for (int i = 0; i < vars.length; i++) {
			NameDomain domain = (NameDomain) vars[i].getDomain();
			backUpDomains[i] = new NameDomain(domain);
			
			// re-initialize the domains
			domain.reset();
		}
		
		boolean success = ac3();
		// restore backup
		if (!success) { 
			for (NameDomain d : backUpDomains) {
				d.getVariable().setDomain(d);
			}
			logger.info("NameFailure");
		}
//		long endTime = System.nanoTime();
//		propagationTime += endTime - startTime;
//		System.out.println(invocationCNT++ + "Name Propagation took "+((endTime - startTime) / 1000000.) + " ms");
//		System.out.println("Name Propagation took in sum "+((propagationTime) / 1000000) + " ms");
		return success;
	}
	
	private boolean ac3() {
		Queue<NameMatchingConstraint> worklist = new LinkedList<NameMatchingConstraint>();
		for (Constraint con : this.getConstraints()) {
			worklist.add((NameMatchingConstraint) con);
		}
		
		while (!worklist.isEmpty()) {
			NameMatchingConstraint con = worklist.remove();
			int changed = revise(con);
			if (changed > 0) {
				if (checkDomains(con)) {
					if (changed == 1) {
						worklist.addAll(getConstraintsOfVarExcept(con.getScope()[0], con));
					} else if (changed == 2) {
						worklist.addAll(getConstraintsOfVarExcept(con.getScope()[1], con));
					} else if (changed == 3) {
						worklist.addAll(getConstraintsOfVarExcept(con.getScope()[0], con));
						worklist.addAll(getConstraintsOfVarExcept(con.getScope()[1], con));
					}
				} else {
					return false; // one domain has no model
				}
			}
		}
		return true;
	}
	
	private boolean checkDomains(NameMatchingConstraint con) {
		if (! ((NameVariable) con.getScope()[0]).hasModel())
			return false;
		if (! (con.getType().equals(NameMatchingConstraint.Type.UNARYEQUALS) ||
					 con.getType().equals(NameMatchingConstraint.Type.UNARYDIFFERENT))) {
            return ((NameVariable) con.getScope()[1]).hasModel();
		}
		return true;
	}
	
	/**
	 * @param con
	 * @return 0 if nothing changed, 1 if only domain of x1 changed, 2 if only domain of x2 changed, 3 if both changed
	 */
	private int revise(NameMatchingConstraint con) {
		int ret = 0;
		if (con.getType().equals(NameMatchingConstraint.Type.UNARYEQUALS)) {
			NameDomain dom = (NameDomain) con.getScope()[0].getDomain();
			if(dom.setUnaryEquals(con.getUnaryValues())) {
				ret = 1;
			}
		} else if (con.getType().equals(NameMatchingConstraint.Type.UNARYDIFFERENT)) {
			NameDomain dom = (NameDomain) con.getScope()[0].getDomain();
			if(dom.setUnaryDifferent(con.getUnaryValues())) {
				ret =  1;
			}
		} else if (con.getType().equals(NameMatchingConstraint.Type.EQUALS)) {
			NameDomain dFrom = (NameDomain) con.getScope()[0].getDomain();
			NameDomain dTo = (NameDomain) con.getScope()[1].getDomain();
			ret = dFrom.applyEqualsConstr(dTo);
		} else if (con.getType().equals(NameMatchingConstraint.Type.DIFFERENT)) {
			NameDomain dFrom = (NameDomain) con.getScope()[0].getDomain();
			NameDomain dTo = (NameDomain) con.getScope()[1].getDomain();
			ret = dFrom.applyDifferentConstr(dTo);
		}
		return ret;
	}

	private void buildVar2Constraints() {
		var2Constraints.clear();
		for (Constraint con : this.getConstraints()) {
			if (con instanceof NameMatchingConstraint) {
				Variable[] scope = con.getScope();
				addToVar2Constraints(scope[0], (NameMatchingConstraint) con);
				addToVar2Constraints(scope[1], (NameMatchingConstraint) con);
			}
		}
	}

	private void addToVar2Constraints(Variable var, NameMatchingConstraint con) {
		try {
			var2Constraints.get(var).add(con);
		} catch (NullPointerException e) {
			List<NameMatchingConstraint> l = new LinkedList<>();
			l.add(con);
			var2Constraints.put(var, l);
		}
	}

	private List<NameMatchingConstraint> getConstraintsOfVarExcept(Variable var, Constraint except) {
		List<NameMatchingConstraint> ret = new LinkedList<>();
		List<NameMatchingConstraint> allConsOfVar = this.var2Constraints.get(var);
		if (allConsOfVar != null) {
			for (NameMatchingConstraint con : allConsOfVar) {
				if (con != except) {
					ret.add(con);
				}
			}
		}
		return ret;
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
	
	public String[] getSymbols() {
		return symbols;
	}
	
	public int getIndexOfSymbol(String symbol) {
		Integer index = symbols2Index.get(symbol);
		if (index != null) {
			return index.intValue();
		} else {
			throw new IllegalArgumentException("Symbol " + symbol + " does not exist");
		}
	}
	
	public int[] getIndexArrayOfSymbols(String[] symbols) {
		int[] ret = new int[symbols.length];
		for (int i = 0; i < symbols.length; i++) {
			ret[i] = getIndexOfSymbol(symbols[i]);
		}
		return ret;
	}
}
