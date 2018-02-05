package simpleBooleanValueCons;

import java.util.logging.Logger;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;

public class SimpleBooleanValueConstraint extends MultiBinaryConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1779189485787417098L;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public enum Type {EQUALS, UNARYTRUE, UNARYFALSE}

    private Type type;
	
	public SimpleBooleanValueConstraint(Type type) {
		this.type = type;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable f, Variable t) {
		if (!(f instanceof SimpleBooleanValueVariable) || !(t instanceof SimpleBooleanValueVariable)) return null;
		
		SimpleBooleanValueVariable fFrom = ((SimpleBooleanValueVariable) f);
		BooleanVariable bFrom = ((BooleanVariable) fFrom.getInternalVariables()[0]);
		SimpleBooleanValueVariable fTo = ((SimpleBooleanValueVariable) t);
		BooleanVariable bTo = ((BooleanVariable) fTo.getInternalVariables()[0]);
		
		String wff = "";
		if (this.type.equals(Type.EQUALS)) {
			wff = "(w1 <-> w2)";
			logger.finest("Generated WFF for EQUALS constraint: " + wff);
		} else if (this.type.equals(Type.UNARYTRUE)) {
			wff = "(w1)";
			logger.finest("Generated WFF for UNARYTRUE constraint: " + wff);
		} else if (this.type.equals(Type.UNARYFALSE)) {
			wff = "(~w1)";
			logger.finest("Generated WFF for UNARYFALSE constraint: " + wff);
		}
		logger.finest("Generated WFF for EQUALS constraint: " + wff);
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(new BooleanVariable[] {bFrom, bTo}, wff);
		return cons;
	}

	@Override
	public Object clone() {
		return new SimpleBooleanValueConstraint(this.type);
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

}
