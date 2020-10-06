package htn;

import java.util.Vector;

import fluentSolver.Fluent;
import org.metacsp.framework.VariablePrototype;

import fluentSolver.FluentNetworkSolver;
import hybridDomainParsing.HybridDomain;

public class EffectTemplate {
	
	private final String key;
	private final String name;
	private final String[] args;
	private final IntArg[] intArgs;
	private String comp;
	private int maxArgs;
	
	private VariablePrototype prototype = null;
	private final Vector<AdditionalConstraintTemplate> additionalConstraints = 
			new Vector<AdditionalConstraintTemplate>();
	
	public EffectTemplate(String key, String name, String[] args, IntArg[] intArgs, int maxArgs, String component) {
		this.key = key;
		this.name = name;
		this.args = args;
		this.intArgs = intArgs;
		this.maxArgs = maxArgs;
		if (component.equals("Task") && name.startsWith("!")) { // this is updated later in the parser when all operator names are known
			setComponentToActivity();
		} else {
			comp = component;
		}
	}

	public EffectTemplate(String key, String name, String[] args, int maxArgs, String component) {
		this(key, name, args, new IntArg[0], maxArgs, component);
	}
	
	private void createPrototype(FluentNetworkSolver groundSolver) {
		// fill arguments array up to maxargs
		String[] filledArgs = new String[maxArgs];
		for (int i = 0; i < args.length; i++) {
			filledArgs[i] = args[i];
		}
		for (int i = args.length; i < maxArgs; i++) {
			filledArgs[i] = HybridDomain.EMPTYSTRING;
		}
		this.prototype = new VariablePrototype(groundSolver, comp, name, filledArgs);
	}

	public VariablePrototype getPrototype(FluentNetworkSolver groundSolver) {
		if (this.prototype == null) {
			createPrototype(groundSolver);
		}
		return prototype;
	}

	public void addAdditionalConstraint(AdditionalConstraintTemplate con) {
		this.additionalConstraints.add(con);
	}

	public Vector<AdditionalConstraintTemplate> getAdditionalConstraints() {
		return additionalConstraints;
	}
	
	public boolean hasAdditionalConstraints() {
		return additionalConstraints.size() > 0;
	}
	
	public String getKey() {
		return key;
	}

	public String[] getInputArgs() {
		return args;
	}

	public IntArg[] getIntArgs() {
		return intArgs;
	}

	public String getName() {
		return name;
	}

	public void setComponentToActivity() {
		comp = Fluent.ACTIVITY_TYPE_STR;
	}
}
