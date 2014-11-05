package pfd0Symbolic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.meta.simplePlanner.PlanningOperator;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimpleReusableResource;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import com.google.common.primitives.Ints;


public class HybridDomain{

	private Vector<PlanReportroryItem> operators;
	private Vector<PlanReportroryItem> methods;
	private String name;
	
	private static final String DOMAIN_KEYWORD = "HybridHTNDomain";
	private static final String MAXARGS_KEYWORD = "MaxArgs";
	private static final String OPERATOR_KEYWORD = ":operator";
	private static final String HEAD_KEYWORD = "Head";
	private static final String PRECONDITION_KEYWORD = "Pre";
	private static final String EFFECT_KEYWORD = "Add";
	private static final String NEGATIVE_EFFECT_KEYWORD = "Del";
	private static final String CONSTRAINT_KEYWORD = "Constraint";
	
	private static final String EMPTYSTRING = "n";
	private static final String VARIABLE_INDICATOR = "?";
	
	public HybridDomain(String name) {
		this.name = name;
		this.operators = new Vector<PlanReportroryItem>();
	}
	
	public void addOperator(PFD0Operator op) {
		operators.add(op);
	}
	
	public Vector<PlanReportroryItem> getOperators() {
		return operators;
	}

	public Vector<PlanReportroryItem> getMethods() {
		return methods;
	}

	/**
	 * Creates a {@link SimpleOperator} from a textual specification (used by the
	 * domain parser).
	 * @param textualSpecification A textual specification of an operator
	 * @param resources The resources (identifiers of {@link SimpleReusableResource}s) used in this operator.
	 * @param planningOp Whether this is a {@link PlanningOperator} or a {@link SimpleOperator}.
	 * @return A {@link SimpleOperator} build according to the textual specification.
	 */
	private static PFD0Operator parseOperator(String textualSpecification, int maxargs, 
			FluentNetworkSolver groundSolver) {
		HashMap<String,String> preconditionsMap = new HashMap<String, String>();
		ArrayList<String> negativeEffectsKeyList = new ArrayList<String>();
		HashMap<String,String> effectsMap = new HashMap<String, String>();
		Vector<AllenIntervalConstraint> constraints = new Vector<AllenIntervalConstraint>();
		Vector<String> froms = new Vector<String>();  // TODO Are these necessary???
		Vector<String> tos = new Vector<String>();
//		int[] resourceRequirements = new int[resources.length];

		// Parse Head
		String head = parseKeyword(HEAD_KEYWORD, textualSpecification)[0].trim();

		// Parse Preconditions
		String[] preElements = parseKeyword(PRECONDITION_KEYWORD, textualSpecification);
		for (String preElement : preElements) {
			String preKey = preElement.substring(0,preElement.indexOf(" ")).trim();
			String preFluent = preElement.substring(preElement.indexOf(" ")).trim();
			preconditionsMap.put(preKey, preFluent);
		}

		// Parse effects
		String[] effElements = parseKeyword(EFFECT_KEYWORD, textualSpecification);
		for (String effElement : effElements) {
			String effKey = effElement.substring(0,effElement.indexOf(" ")).trim();
			String effFluent = effElement.substring(effElement.indexOf(" ")).trim();
			effectsMap.put(effKey, effFluent);
		}
		
		// Parse negative effects
		String[] negEffElements = parseKeyword(NEGATIVE_EFFECT_KEYWORD, textualSpecification);
		for (String negEff : negEffElements) {
			negativeEffectsKeyList.add(negEff);
		}

		// Parse AllenIntervalConstraints:
		String[] constraintElements = parseKeyword(CONSTRAINT_KEYWORD, textualSpecification);
		for (String conElement : constraintElements) {
			String constraintName = null;
			Vector<Bounds> bounds = null;
			if (conElement.contains("[")) {
				constraintName = conElement.substring(0,conElement.indexOf("[")).trim();
				String boundsString = conElement.substring(conElement.indexOf("["),conElement.lastIndexOf("]")+1);
				String[] splitBounds = boundsString.split("\\[");
				bounds = new Vector<Bounds>();
				for (String oneBound : splitBounds) {
					if (!oneBound.trim().equals("")) {
						String lbString = oneBound.substring(oneBound.indexOf("[")+1,oneBound.indexOf(",")).trim();
						String ubString = oneBound.substring(oneBound.indexOf(",")+1,oneBound.indexOf("]")).trim();
						long lb, ub;
						if (lbString.equals("INF")) lb = org.metacsp.time.APSPSolver.INF;
						else lb = Long.parseLong(lbString);
						if (ubString.equals("INF")) ub = org.metacsp.time.APSPSolver.INF;
						else ub = Long.parseLong(ubString);
						bounds.add(new Bounds(lb,ub));
					}
				}
			}
			else {
				constraintName = conElement.substring(0,conElement.indexOf("(")).trim();
			}
			String from = null;
			String to = null;
			String fromSeg = null;
			if (constraintName.equals("Duration")) {
				from = conElement.substring(conElement.indexOf("(")+1, conElement.indexOf(")")).trim();
				to = from;
			}
			else {
				fromSeg = conElement.substring(conElement.indexOf("("));
				from = fromSeg.substring(fromSeg.indexOf("(")+1, fromSeg.indexOf(",")).trim();
				to = fromSeg.substring(fromSeg.indexOf(",")+1, fromSeg.indexOf(")")).trim();
			}

			AllenIntervalConstraint con = null;
			if (bounds != null) {
				con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),bounds.toArray(new Bounds[bounds.size()]));
			}
			else con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));
			constraints.add(con);
			froms.add(from);
			tos.add(to);
		}

		// Parse Resources // TODO LEFT OUT FOR NOW
//		String[] resourceElements = parseKeyword("RequiredResource", textualSpecification);
//		for (String resElement : resourceElements) {
//			String requiredResource = resElement.substring(0,resElement.indexOf("(")).trim();
//			int requiredAmount = Integer.parseInt(resElement.substring(resElement.indexOf("(")+1,resElement.indexOf(")")).trim());
//			for (int k = 0; k < resources.length; k++) {
//				if (resources[k].equals(requiredResource)) {
//					resourceRequirements[k] = requiredAmount;
//				}
//			}
//		}

//		class AdditionalConstraint {
//			AllenIntervalConstraint con;
//			int from, to;
//			public AdditionalConstraint(AllenIntervalConstraint con, int from, int to) {
//				this.con = con;
//				this.from = from;
//				this.to = to;
//			}
//			public void addAdditionalConstraint(PFD0Operator op) {
////				op.addConstraint(con, from, to); // TODO
//			}
//		}
//
//		//pass this to constructor
//		String[] preconditionStrings = new String[preconditionsMap.keySet().size()];
//		boolean[] effectBools = new boolean[preconditionsMap.keySet().size()];
//		AllenIntervalConstraint[] consFromHeadtoPre = new AllenIntervalConstraint[preconditionsMap.keySet().size()];
//		//Vector<AllenIntervalConstraint> consFromHeadToReq = new Vector<AllenIntervalConstraint>();
//		Vector<AdditionalConstraint> acs = new Vector<AdditionalConstraint>();
//		HashMap<String,Integer> preKeysToIndices = new HashMap<String, Integer>();
//
//		int preCounter = 0;
//		for (String preKey : preconditionsMap.keySet()) {
//			String preString = preconditionsMap.get(preKey);
//			preconditionStrings[preCounter] = preString;
//			preKeysToIndices.put(preKey,preCounter);
//			preCounter++;
//		}
//
//		for (int i = 0; i < froms.size(); i++) {
//			//Head -> Head
//			if (froms.elementAt(i).equals("Head") && tos.elementAt(i).equals("Head")) {
//				AdditionalConstraint ac = new AdditionalConstraint(constraints.elementAt(i), 0, 0);
//				acs.add(ac);
//			}
//			//req -> req
//			else if (!froms.elementAt(i).equals("Head") && !tos.elementAt(i).equals("Head")) {
//				String preFromKey = froms.elementAt(i);
//				String reqToKey = tos.elementAt(i);
//				int preFromIndex = preKeysToIndices.get(preFromKey);
//				int preToIndex = preKeysToIndices.get(reqToKey);
//				AllenIntervalConstraint con = constraints.elementAt(i);
//				AdditionalConstraint ac = new AdditionalConstraint(con, preFromIndex+1, preToIndex+1);
//				acs.add(ac);
//			}
//			//req -> Head
//			else if (!froms.elementAt(i).equals("Head") && tos.elementAt(i).equals("Head")) {
//				String reqFromKey = froms.elementAt(i);
//				int reqFromIndex = preKeysToIndices.get(reqFromKey);
//				AllenIntervalConstraint con = constraints.elementAt(i);
//				AdditionalConstraint ac = new AdditionalConstraint(con, reqFromIndex+1, 0);
//				acs.add(ac);
//			}
//			//Head -> req
//			else if (froms.elementAt(i).equals("Head") && !tos.elementAt(i).equals("Head")) {
//				AllenIntervalConstraint con = constraints.elementAt(i);
//				String preToKey = tos.elementAt(i);
//				consFromHeadtoPre[preKeysToIndices.get(preToKey)] = con;
//			}
//		}
//
//		//Call constructor
//		PFD0Operator ret =  new PFD0Operator(head,consFromHeadtoReq,requirementStrings,resourceRequirements);
//		for (AdditionalConstraint ac : acs) ac.addAdditionalConstraint(ret);
//		return ret;

		System.out.println("HEAD: \n" + head);
		System.out.println("Preconditions: \n" + preconditionsMap);

		System.out.println("Effects: \n" + effectsMap);

		System.out.println("Constraints: \n" + constraints);

		System.out.println("Froms: \n" + froms);

		System.out.println("Tos: \n" + tos);
		
		String headname = extractName(head);
		System.out.println("Headname: " + headname);
		String[] argStrings = extractArgs(head);
		System.out.println("argStrings" + Arrays.toString(argStrings));
		
		HashMap<String, HashMap<String, Integer>> variableOccurrencesMap = new HashMap<String, HashMap<String, Integer>>();
		addVariableOccurrences(variableOccurrencesMap, argStrings, "head");
		
		// Create preconditions
		PFD0Precondition[] preconditions = createPreconditions(preconditionsMap, maxargs, 
				variableOccurrencesMap, negativeEffectsKeyList);
		
		// Create positive effects
		VariablePrototype[] effects = new VariablePrototype[effectsMap.size()];
		int i = 0;
		for (Entry<String, String> e : effectsMap.entrySet()) {
			String effKey = e.getKey();
			effects[i++] = createEffect(effKey, e.getValue(), maxargs, variableOccurrencesMap, groundSolver);
		}
		
		System.out.println("VarMap after parsing effects " + variableOccurrencesMap);
		
		
		PFD0Operator ret =  new PFD0Operator(headname, argStrings, preconditions, effects);
		ret.setVariableOccurrencesMap(variableOccurrencesMap);
		System.out.println("Created Operator: " + ret);
		return ret;
	}
	
	private static PFD0Precondition[] createPreconditions(HashMap<String,String> preconditionsMap, 
			int maxargs, 
			HashMap<String, HashMap<String, Integer>> variableOccurrencesMap,
			ArrayList<String> negativeEffectsKeyList) {
		//Create preconditions
		PFD0Precondition[] preconditions = new PFD0Precondition[preconditionsMap.size()];
		int i = 0;
		for (Entry<String, String> e : preconditionsMap.entrySet()) {
			String preKey = e.getKey();
			PFD0Precondition pre = createPrecondition(preKey, e.getValue(), maxargs, variableOccurrencesMap);
			// Set negative effects
			System.out.println("NEG KEY LIST " + negativeEffectsKeyList.toString());
			if (negativeEffectsKeyList.contains(preKey)) {
				pre.setNegativeEffect(true);
			}
			preconditions[i++] = pre;
		}
		return preconditions;
	}
	
	private static void addVariableOccurrences(
			HashMap<String, HashMap<String, Integer>> variableOccurrencesMap,
			String[] argStrings, String key) {
		for (int i = 0; i < argStrings.length;  i++) {
			HashMap<String, Integer> occ = variableOccurrencesMap.get(argStrings[i]);
			if (occ == null) {
				occ = new HashMap<String, Integer>();
				variableOccurrencesMap.put(argStrings[i], occ);
			}
			occ.put(key, new Integer(i));
		}

	}
	
//	private static ArrayList<String> filterVariables(String[] argStrings) {
//		ArrayList<String> ret = new ArrayList<String>();
//		for (String str : argStrings) {
//			if (str.startsWith(VARIABLE_INDICATOR)) {
//				ret.add(str);
//			}
//		}
//		return ret;
//	}
	
	private static String extractName(String str) {
		return str.substring(str.indexOf("::")+1,str.indexOf("(")).trim();
	}
	
	private static String[] extractArgs(String str) {
		return str.substring(str.indexOf("(")+1,str.indexOf(")")).trim().split(" ");
	}
	
	private static VariablePrototype createEffect(
			String effKey, 
			String effString, 
			int maxargs, 
			HashMap<String, HashMap<String, Integer>> variableOccurrencesMap,
			FluentNetworkSolver groundSolver) {
		String name = extractName(effString);
		System.out.println("EffName " + name);
		String[] args = extractArgs(effString);
		System.out.println("EffArgs: " + Arrays.toString(args));
		
		// find bindings to head
//		ArrayList<Integer> connectionsList = new ArrayList<Integer>();
/*		addVariableOccurrences(variableOccurrencesMap, args, effKey); */
//		for (int i = 0; i < args.length; i++) {
//			if (args[i].startsWith(VARIABLE_INDICATOR)) {
//				Integer headId = variableOccurrencesMap.get(args[i]).get("head");
//				if (headId != null) {
//					connectionsList.add(i);
//					connectionsList.add(headId);
//				}
//			}
//		}
//		System.out.println("Connections: " + connectionsList.toString());

		// fill arguments array up to maxargs
		String[] filledArgs = new String[maxargs];
		for (int i = 0; i < args.length; i++) {
			filledArgs[i] = args[i];
		}
		for (int i = args.length; i < maxargs; i++) {
			filledArgs[i] = EMPTYSTRING;
		}
		VariablePrototype ret = new VariablePrototype(groundSolver, "S", name, filledArgs);
		System.out.println("Effect " + ret);
		return ret;
	}
	
	private static PFD0Precondition createPrecondition(
			String preKey, 
			String preString, 
			int maxargs, 
			HashMap<String, HashMap<String, Integer>> variableOccurrencesMap) {
		String name = extractName(preString);
		System.out.println("PreName " + name);
		String[] args = extractArgs(preString);
		System.out.println("PreArgs: " + Arrays.toString(args));
		
		// find bindings to head
		ArrayList<Integer> connectionsList = new ArrayList<Integer>();
		addVariableOccurrences(variableOccurrencesMap, args, preKey);
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(VARIABLE_INDICATOR)) {
				Integer headId = variableOccurrencesMap.get(args[i]).get("head");
				if (headId != null) {
					connectionsList.add(i);
					connectionsList.add(headId);
				}
			}
		}
		System.out.println("Connections: " + connectionsList.toString());
		
		PFD0Precondition ret = new PFD0Precondition(name, args, 
				Ints.toArray(connectionsList),
				maxargs,
				EMPTYSTRING,
				preKey);
		System.out.println("PFD0Precondition " + ret);
		return ret;
	}

	/**
	 * Parses a domain file (see domains/testDomain.ddl for an example), instantiates
	 * the necessary {@link MetaConstraint}s and adds them to the provided {@link SimplePlanner}.
	 * @param sp The {@link SimplePlanner} that will use this domain.
	 * @param filename Text file containing the domain definition. 
	 */
	public static HybridDomain parseDomain(MetaConstraintSolver sp, String filename) {
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
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
				String name = parseKeyword(DOMAIN_KEYWORD, everything)[0];
				
				int maxargs = Integer.parseInt(parseKeyword(MAXARGS_KEYWORD, everything)[0]);
				System.out.println("MaxArgs " + maxargs);
				
				// RESOURCES: skipped for the moment	
//				String[] resourceElements = parseKeyword("Resource", everything);
//				HashMap<String,Integer> resources = processResources(resourceElements);
//				String[] simpleOperators = parseKeyword("SimpleOperator", everything);
				String[] planningOperators = parseKeyword(OPERATOR_KEYWORD, everything);
//				String[] sensors = parseKeyword("Sensor", everything);
//				String[] actuators = parseKeyword("Actuator", everything);
//				//String[] controllable = parseKeyword("Controllable", everything);
//
//				String[] contextVars = parseKeyword("ContextVariable", everything);
//
//				int[] resourceCaps = new int[resources.keySet().size()];
//				String[] resourceNames = new String[resources.keySet().size()];
//				int resourceCounter = 0;
//				for (String rname : resources.keySet()) {
//					resourceNames[resourceCounter] = rname;
//					resourceCaps[resourceCounter] = resources.get(rname);
//					resourceCounter++;
//				}

				HybridDomain dom = new HybridDomain(name);

				ArrayList<PFD0Operator> operators = new ArrayList<PFD0Operator>();
				for (String operatorstr : planningOperators) {
					System.out.println("OPERATORSTRING: {");
					System.out.println(operatorstr);
					System.out.println("}\n");
					dom.addOperator(HybridDomain.parseOperator(operatorstr, maxargs, 
							(FluentNetworkSolver) sp.getConstraintSolvers()[0]));
				}

				return dom;
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return null;
	}
	
	protected static String[] parseKeyword(String keyword, String everything) {
		Vector<String> elements = new Vector<String>();
		int lastElement = everything.lastIndexOf(keyword);
		while (lastElement != -1) {
			int bw = lastElement;
			int fw = lastElement;
			boolean skip = false;
			while (everything.charAt(--bw) != '(') { 
				if (everything.charAt(bw) != ' ' && everything.charAt(bw) != '(') {
					everything = everything.substring(0,bw);
					lastElement = everything.lastIndexOf(keyword);
					skip = true;
					break;
				}
			}
			if (!skip) {
				int parcounter = 1;
				while (parcounter != 0) {
					if (everything.charAt(fw) == '(') parcounter++;
					else if (everything.charAt(fw) == ')') parcounter--;
					fw++;
				}
				String element = everything.substring(bw,fw).trim();
				element = element.substring(element.indexOf(keyword)+keyword.length(),element.lastIndexOf(")")).trim();
				if (!element.startsWith(",") && !element.trim().equals("")) elements.add(element);
				everything = everything.substring(0,bw);
				lastElement = everything.lastIndexOf(keyword);
			}
		}
		return elements.toArray(new String[elements.size()]);		
	}

//	protected static HashMap<String,Integer> processResources (String[] resources) {
//		HashMap<String, Integer> ret = new HashMap<String, Integer>();
//		for (String resourceElement : resources) {
//			String resourceName = resourceElement.substring(0,resourceElement.indexOf(" ")).trim();
//			int resourceCap = Integer.parseInt(resourceElement.substring(resourceElement.indexOf(" ")).trim());
//			ret.put(resourceName, resourceCap);
//		}
//		return ret;
//	}

	
	
}
