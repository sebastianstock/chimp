package hybridDomainParsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.meta.simplePlanner.SimplePlanner;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Method;
import pfd0Symbolic.PFD0Operator;
import pfd0Symbolic.PlanReportroryItem;
import resourceFluent.FluentResourceUsageScheduler;
import resourceFluent.FluentScheduler;
import resourceFluent.ResourceUsageTemplate;


public class HybridDomain{

	private final Vector<PlanReportroryItem> operators = new Vector<PlanReportroryItem>();
	private final Vector<PlanReportroryItem> methods = new Vector<PlanReportroryItem>();
	private final Vector<FluentScheduler> fluentSchedulers = new Vector<FluentScheduler>();
	private String name;
	private final Vector<FluentResourceUsageScheduler> resourceSchedulers = 
			new Vector<FluentResourceUsageScheduler>();
	private final Vector<ResourceUsageTemplate> fluentResourceUsages = 
			new Vector<ResourceUsageTemplate>();
	
	private final MetaConstraintSolver solver;
	private final FluentNetworkSolver groundSolver;
	private final String fileName;
	private int maxArgs; // Maximum number of arguments of a fluent.
	
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
	public static final String VARIABLE_RESTRICTION_KEYWORD = "Values";
	public static final String STATE_VARIBALE_KEYWORD = "StateVariable";
	public static final String FLUENT_RESOURCE_KEYWORD = "FluentResourceUsage";
	public static final String PARAM_KEYWORD = "Param";
	public static final String ACTION_RESOURCE_KEYWORD = "ResourceUsage";
	public static final String USAGE_KEYWORD = "Usage";
	public static final String RESOURCE_KEYWORD ="Resource";
	public static final String FLUENT_KEYWORD = "Fluent";
	
	public static final String EMPTYSTRING = "n";
	public static final String VARIABLE_INDICATOR = "?";
	
	private static final String[] NO_STRINGS = {};
	
	public HybridDomain(MetaConstraintSolver solver, String filename) throws DomainParsingException {
		this.solver = solver;
		this.groundSolver = (FluentNetworkSolver) solver.getConstraintSolvers()[0];
		this.fileName = filename;
		parseDomain();
	}
	
	public Vector<PlanReportroryItem> getOperators() {
		return operators;
	}

	public Vector<PlanReportroryItem> getMethods() {
		return methods;
	}
	
	public Vector<FluentScheduler> getFluentSchedulers() {
		return fluentSchedulers;
	}
	
	public Vector<FluentResourceUsageScheduler> getResourceSchedulers() {
		return resourceSchedulers;
	}
	
	public Vector<ResourceUsageTemplate> getFluentResourceUsages() {
		return fluentResourceUsages;
	}
	
	public static String extractName(String str) {
		return str.substring(str.indexOf("::")+1,str.indexOf("(")).trim();
	}
	
	public static String[] extractArgs(String str) {
		String argStr =  str.substring(str.indexOf("(")+1,str.indexOf(")")).trim();
		if (argStr.length() > 0) {
			return argStr.split(" ");
		} else {
			return NO_STRINGS;
		}
	}
	

	private void createFluentSchedulers(String stateStr) {
		// Parse Head	
		String[] elements = stateStr.split(" ");
		String head = elements[0];
		int field = Integer.parseInt(elements[1]);
		String[] fieldValues = Arrays.copyOfRange(elements, 2, elements.length-1);
		for (String fieldValue : fieldValues) {
			this.fluentSchedulers.add(new FluentScheduler(null, null, head, field, fieldValue));
		}
	}

	/**
	 * Parses a domain file (see domains/testDomain.ddl for an example), instantiates
	 * the necessary {@link MetaConstraint}s and adds them to the provided {@link SimplePlanner}.
	 * @param sp The {@link SimplePlanner} that will use this domain.
	 * @param fileName Text file containing the domain definition. 
	 * @throws DomainParsingException 
	 */
	private void parseDomain() throws DomainParsingException {
		String everything = null;
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
				everything = sb.toString();
				name = parseKeyword(DOMAIN_KEYWORD, everything)[0];
				
				maxArgs = Integer.parseInt(parseKeyword(MAXARGS_KEYWORD, everything)[0]);
				System.out.println("MaxArgs " + maxArgs);
				
				// Parse Resources and create ResourceSchedulers
				String[] resourceElements = parseKeyword(RESOURCE_KEYWORD, everything);
				Map<String,Integer> resources = processResources(resourceElements);
				resourceSchedulers.clear();
				for (Entry<String, Integer> entry : resources.entrySet()) {
					FluentResourceUsageScheduler rs = new FluentResourceUsageScheduler(null, null, entry.getKey(), 
							entry.getValue().intValue());
					
//					resourceSchedulers.add(new FluentResourceUsageScheduler(null, null, entry.getKey(), 
//							entry.getValue().intValue()));
					resourceSchedulers.add(rs);
				}
				
				// Parse FluentResourceUsages
				String[] usageElements = parseKeyword(FLUENT_RESOURCE_KEYWORD, everything);
				processFluentResourceUsages(usageElements);
				// test if all resources are listed:
				for (ResourceUsageTemplate rt : fluentResourceUsages) {
					if (!resources.containsKey(rt.getResourceName())) {
						throw new DomainParsingException("Resource " + rt.getResourceName() + " is not defined");
					}
				}
				
				System.out.println(everything);
				String[] planningOperators = parseKeyword(OPERATOR_KEYWORD, everything);
				for (String operatorstr : planningOperators) {
					OperatorParser oParser = new OperatorParser(operatorstr, groundSolver, maxArgs);
					PFD0Operator op = oParser.create();
					System.out.println("Created Operator: " + op);
					this.operators.addElement(op);
				}
				
				String[] planningMethods = parseKeyword(METHOD_KEYWORD, everything);
				for (String methodStr : planningMethods) {
					MethodParser mParser = new MethodParser(methodStr, groundSolver, maxArgs);
					PFD0Method m = mParser.create();
					System.out.println("Created Method: " + m);
					this.methods.addElement(m);
				}
				
				// Parse state variables:
				String[] stateVariableElements = parseKeyword(STATE_VARIBALE_KEYWORD, everything);
				for (String stateStr : stateVariableElements) {
					System.out.println("STATESTRING: {");
					System.out.println(stateStr);
					System.out.println("}\n");
					createFluentSchedulers(stateStr);
				}
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
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

	protected static HashMap<String,Integer> processResources (String[] resources) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String resourceElement : resources) {
			String resourceName = resourceElement.substring(0,resourceElement.indexOf(" ")).trim();
			int resourceCap = Integer.parseInt(resourceElement.substring(resourceElement.indexOf(" ")).trim());
			ret.put(resourceName, resourceCap);
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
	static ResourceUsageTemplate parseResourceUsage(String usageElement, 
			boolean stateUsage) {
		// parse Resource
			String resourceStr = parseKeyword(USAGE_KEYWORD, usageElement)[0];
			String resourceName = resourceStr.substring(0,resourceStr.indexOf(" ")).trim();
			int usageLevel = Integer.parseInt(resourceStr.substring(resourceStr.indexOf(" ")).trim());
			
			String fluentType = "";
			// parse Fluent type
			if (stateUsage) {
				fluentType = parseKeyword(FLUENT_KEYWORD, usageElement)[0];
			}
			
			// parse Params
			String[] paramElements = parseKeyword(PARAM_KEYWORD, usageElement);
			int[] resourceRequirementPositions = new int[paramElements.length];
			String[] resourceRequirements = new String[paramElements.length];
			for (int j = 0; j < paramElements.length; j++) {
				String paramElement = paramElements[j];
				resourceRequirementPositions[j] = 
						Integer.parseInt(paramElement.substring(0, paramElement.indexOf(" ")).trim());
				resourceRequirements[j] = paramElement.substring(paramElement.indexOf(" ")).trim();
			}
			
			return new ResourceUsageTemplate(resourceName, fluentType, resourceRequirementPositions, 
					resourceRequirements, usageLevel);
	}
	
}