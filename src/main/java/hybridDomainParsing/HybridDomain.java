package hybridDomainParsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import fluentSolver.FluentNetworkSolver;
import chimp.search.FlawProvider;
import chimp.logging.ChimpLogging;

import htn.HTNMethod;
import htn.HTNOperator;
import htn.PlanReportroryItem;
import chimp.model.ResourceParameterRequirement;
import chimp.model.ResourceRequirement;
import chimp.model.ResourceCapacity;
import chimp.model.StateVariableDefinition;


public class HybridDomain implements ClassicHybridDomain {

	private final Vector<PlanReportroryItem> operators = new Vector<PlanReportroryItem>();
	private final Vector<PlanReportroryItem> methods = new Vector<PlanReportroryItem>();
	private final Vector<StateVariableDefinition> stateVariableDefinitions = new Vector<>();
	private String name;
	private final Vector<ResourceCapacity> resourceCapacities = new Vector<ResourceCapacity>();
	private final Vector<ResourceRequirement> fluentResourceUsages =
			new Vector<ResourceRequirement>();
	
	private int maxArgs; // Maximum number of arguments of a fluent.
	private final String domainStr;
	private final String[] predicateSymbols;
	
	public static final String DOMAIN_KEYWORD = "HybridHTNDomain";
	public static final String MAXARGS_KEYWORD = "MaxArgs";
	public static final String OPERATOR_KEYWORD = ":operator";
	public static final String METHOD_KEYWORD = ":method";
	public static final String HEAD_KEYWORD = "Head";
	public static final String PRECONDITION_KEYWORD = "Pre";
	public static final String EFFECT_KEYWORD = "Add";
	public static final String NEGATIVE_EFFECT_KEYWORD = "Del";
	public static final String SUBTASK_KEYWORD = "Sub";
	public static final String CONSTRAINT_KEYWORD = "Constraint";
	public static final String ORDERING_CONSTRAINT_KEYWORD = "Ordering";
	public static final String TYPE_KEYWORD = "Type";
	public static final String NOT_TYPE_KEYWORD = "NotType";
	public static final String VALUE_RESTRICTION_KEYWORD = "Values";
	public static final String NEGATED_VALUE_RESTRICTION_KEYWORD = "NotValues";
	public static final String STATE_VARIBALE_KEYWORD = "StateVariable";
	public static final String FLUENT_RESOURCE_KEYWORD = "FluentResourceUsage";
	public static final String PARAM_KEYWORD = "Param";
	public static final String ACTION_RESOURCE_KEYWORD = "ResourceUsage";
	public static final String USAGE_KEYWORD = "Usage";
	public static final String RESOURCE_KEYWORD ="Resource";
	public static final String FLUENT_KEYWORD = "Fluent";
	public static final String VARIABLES_DIFFERENT_KEYWORD = "VarDifferent";
	public static final String PREDICATE_SYMBOLS_KEYWORD = "PredicateSymbols";
	
	public static final String EMPTYSTRING = "n";
	public static final String VARIABLE_INDICATOR = "?";
	
	private static final String[] NO_STRINGS = {};
	
	public HybridDomain(String filename) throws DomainParsingException {
		this.domainStr = readDomain(filename);
		maxArgs = Integer.parseInt(parseKeyword(MAXARGS_KEYWORD, domainStr)[0]);
		predicateSymbols = parsePredicateSymbols(domainStr);
	}
	
	public Vector<PlanReportroryItem> getOperators() {
		return operators;
	}

	public Vector<PlanReportroryItem> getMethods() {
		return methods;
	}
	
	public Vector<StateVariableDefinition> getStateVariableDefinitions() {
		return stateVariableDefinitions;
	}
	
	public Vector<ResourceCapacity> getResourceCapacities() {
		return resourceCapacities;
	}
	
	public Vector<ResourceRequirement> getFluentResourceUsages() {
		return fluentResourceUsages;
	}
	
	public static String extractName(String str) throws DomainParsingException {
		try {
			return str.substring(str.indexOf("::")+1,str.indexOf("(")).trim();
		} catch (IndexOutOfBoundsException ie) {
			throw new DomainParsingException("Error while parsing predicate name from string " + str 
					+ "\n Should be of format 'name(arg1 ... argn)'");
		}
	}
	
	public static String[] extractArgs(String str) {
		String argStr =  str.substring(str.indexOf("(")+1,str.indexOf(")")).trim();
		if (argStr.length() > 0) {
			return argStr.split(" ");
		} else {
			return NO_STRINGS;
		}
	}

	static StateVariableDefinition parseStateVariableDefinition(String stateStr) {
		String[] elements = stateStr.split(" ");
		String head = elements[0];
		int field = Integer.parseInt(elements[1]);
		List<String> fieldValues = Arrays.asList(Arrays.copyOfRange(elements, 2, elements.length));
		return new StateVariableDefinition(head, field, fieldValues);
	}
	
	private static String readDomain(String fileName) throws DomainParsingException{
		String domainStr = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.trim().startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				domainStr = sb.toString();
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		if (domainStr == null) {
			throw new DomainParsingException("Could not read domain");
		}
		return domainStr;
	}

	/**
	 * Parses a domain file (see domains/testDomain.ddl for an example) and instantiates
	 * the necessary {@link FlawProvider}s.
	 * @throws DomainParsingException Throws this exception if the domain cannot be parsed.
	 */
	public void parseDomain(Map<String, String[]> typesInstancesMap) throws DomainParsingException {
		name = parseKeyword(DOMAIN_KEYWORD, domainStr)[0];
		
		// Parse resource capacities.
		String[] resourceElements = parseKeyword(RESOURCE_KEYWORD, domainStr);
		List<ResourceCapacity> resources = processResources(resourceElements);
		resourceCapacities.clear();
		resourceCapacities.addAll(resources);
		
		// Parse FluentResourceUsages
		String[] usageElements = parseKeyword(FLUENT_RESOURCE_KEYWORD, domainStr);
		processFluentResourceUsages(usageElements);
		// test if all resources are listed:
		for (ResourceRequirement rt : fluentResourceUsages) {
			if (resources.stream().noneMatch(capacity -> capacity.resourceName().equals(rt.resourceName()))) {
				throw new DomainParsingException("Resource " + rt.resourceName() + " is not defined");
			}
		}
		
		String[] planningOperators = parseKeyword(OPERATOR_KEYWORD, domainStr);
		for (String operatorstr : planningOperators) {
			OperatorParser oParser = new OperatorParser(operatorstr, typesInstancesMap, maxArgs);
			HTNOperator op = oParser.create();
//			System.out.println("Created Operator: " + op);
			this.operators.addElement(op);
		}
		
		String[] planningMethods = parseKeyword(METHOD_KEYWORD, domainStr);
		for (String methodStr : planningMethods) {
			MethodParser mParser = new MethodParser(methodStr, typesInstancesMap, maxArgs);
			HTNMethod m = mParser.create();
//			System.out.println("Created Method: " + m);
			this.methods.addElement(m);
		}
		
		// Parse state variables:
		String[] stateVariableElements = parseKeyword(STATE_VARIBALE_KEYWORD, domainStr);
		for (String stateStr : stateVariableElements) {
			stateVariableDefinitions.add(parseStateVariableDefinition(stateStr));
		}
	}


	protected static String[] parseKeyword(String keyword, String everything) {
		Vector<String> elements = new Vector<String>();
		int lastElement = everything.lastIndexOf(keyword);
		while (lastElement != -1) {
			int bw = lastElement;
			int fw = lastElement;
			boolean skip = false;
			
			// test if keyword fully matches (ends)
			if (!Character.isLetterOrDigit(everything.charAt(lastElement + keyword.length()))) {
				while (everything.charAt(--bw) != '(') { 
					if (everything.charAt(bw) != ' ' && everything.charAt(bw) != '(') {
						skip = true;
						break;
					}
				}
				if (!skip) {
					int parcounter = 1;
					while (parcounter != 0) {  // run to closing ')'
						if (everything.charAt(fw) == '(') parcounter++;
						else if (everything.charAt(fw) == ')') parcounter--;
						fw++;
					}
					String element = everything.substring(bw,fw).trim();
					element = element.substring(element.indexOf(keyword)+keyword.length(),element.lastIndexOf(")")).trim();
					if (!element.startsWith(",") && !element.trim().equals("")) elements.add(element);
				}
			}
			everything = everything.substring(0,bw);
			lastElement = everything.lastIndexOf(keyword);
			
		}
		return elements.toArray(new String[elements.size()]);		
	}

	protected static List<ResourceCapacity> processResources (String[] resources) {
		List<ResourceCapacity> ret = new ArrayList<ResourceCapacity>();
		for (String resourceElement : resources) {
			String resourceName = resourceElement.substring(0,resourceElement.indexOf(" ")).trim();
			int resourceCap = Integer.parseInt(resourceElement.substring(resourceElement.indexOf(" ")).trim());
			ret.add(new ResourceCapacity(resourceName, resourceCap));
		}
		return ret;
	}

	private void processFluentResourceUsages (String[] usages) {
		fluentResourceUsages.clear();
		for (String usageElement : usages) {			
			fluentResourceUsages.add(parseResourceUsage(usageElement, true));
		}
	}
	
	/**
	 * Parses a resource usage.
	 * @param usageElement The String representing the resource usage.
	 * @param stateUsage True if it is a state fluent that uses the resource, false if it is an action.
	 * @return
	 */
	static ResourceRequirement parseResourceUsage(String usageElement,
			boolean stateUsage) {
		// parse Resource
		
		String resourceStr;
		if (usageElement.startsWith("(")) { // Old domain format: (ResourceUsage (Usage objManCapacity 1))
			resourceStr = parseKeyword(USAGE_KEYWORD, usageElement)[0];
		} else {         				// new domain format: (ResourceUsage objMancapacity 1)
			resourceStr = usageElement;
		}
		String resourceName = resourceStr.substring(0,resourceStr.indexOf(" ")).trim();
		int usageLevel = Integer.parseInt(resourceStr.substring(resourceStr.indexOf(" ")).trim());

		String fluentType = "";
		// parse Fluent type
		if (stateUsage) {
			fluentType = parseKeyword(FLUENT_KEYWORD, usageElement)[0];
		}

		// parse Params
		String[] paramElements = parseKeyword(PARAM_KEYWORD, usageElement);
		List<ResourceParameterRequirement> parameterRequirements = new ArrayList<>();
		for (int j = 0; j < paramElements.length; j++) {
			String paramElement = paramElements[j];
			int position = Integer.parseInt(paramElement.substring(0, paramElement.indexOf(" ")).trim());
			String value = paramElement.substring(paramElement.indexOf(" ")).trim();
			parameterRequirements.add(new ResourceParameterRequirement(position, value));
		}

		return stateUsage
				? new ResourceRequirement(resourceName, fluentType, parameterRequirements, usageLevel)
				: new ResourceRequirement(resourceName, parameterRequirements, usageLevel);
	}

	private static String[] parsePredicateSymbols(String everything) {
		String[] parsed = parseKeyword(PREDICATE_SYMBOLS_KEYWORD, everything);
		if (parsed.length > 0 ) {
			String[] parsedSymbols = parsed[0].split("\\s+");
			return parsedSymbols;
		} else  {
			ChimpLogging.getLogger(HybridDomain.class).warning("Warning: No predicate symbols specified in domain!");
			return new String[]{};
		}
	}

	public String[] getPredicateSymbols() {
		return predicateSymbols;
	}

	public String getName() {
		return name;
	}

	public int getMaxArgs() {
		return maxArgs;
	}

	@Override
	public int getMaxIntArgs() {
		return 0; // Not implemented for the old domain reader (use ChimpClassicReader)
	}

	@Override
	public int getMinIntValue() {
		return 0; // Not implemented for the old domain reader (use ChimpClassicReader)
	}

	@Override
	public int getMaxIntValue() {
		return 0; // Not implemented for the old domain reader (use ChimpClassicReader)
	}

	@Override
	public Set<String> getOperatorNames() {
		Set<String> operatorNames = new HashSet<>();
		for (PlanReportroryItem operator : operators) {
			operatorNames.add(operator.getName());
		}
		return operatorNames;
	}
}
