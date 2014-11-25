package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Operator;
import pfd0Symbolic.PFD0Precondition;
import pfd0Symbolic.PlanReportroryItem;

import com.google.common.primitives.Ints;

public abstract class PlanReportroiryItemParser {

	protected final String textualSpecification;
	protected final FluentNetworkSolver groundSolver;
	protected final int maxArgs;

	protected final String head;
	protected final String[] argStrings;
	
	protected final Vector<AllenIntervalConstraint> constraints = new Vector<AllenIntervalConstraint>();
	protected final Vector<String> froms = new Vector<String>();  // TODO Are these necessary???
	protected final Vector<String> tos = new Vector<String>();
	
	protected final Map<String, Map<String, Integer>> variableOccurrencesMap = 
			new HashMap<String, Map<String, Integer>>();

	public PlanReportroiryItemParser(String textualSpecification, FluentNetworkSolver groundSolver, 
			int maxArs) {
		this.textualSpecification = textualSpecification;
		this.groundSolver = groundSolver;
		this.maxArgs = maxArs;

		this.head = HybridDomain.parseKeyword(HybridDomain.HEAD_KEYWORD, textualSpecification)[0].trim();
		argStrings = HybridDomain.extractArgs(head);
		addVariableOccurrences(argStrings, "head");
		
		// Parse type definitions
		//		String[] typeElements = parseKeyword(TYPE_KEYWORD, textualSpecification);
		//		for (String typeElement : typeElements) {
		//			String varName = typeElement.substring(0,typeElement.indexOf(" ")).trim();
		//			String type = typeElement.substring(typeElement.indexOf(" ")).trim();
		//	     // TODO
		//		}
		
		parseVariableDefinitions();
		parseAllenIntervalConstraints();
	}
	
	public abstract PlanReportroryItem create();
	
	protected void addVariableOccurrences(String[] argStrings, String key) {
		for (int i = 0; i < argStrings.length;  i++) {
			if (argStrings[i].startsWith(HybridDomain.VARIABLE_INDICATOR)) {
				Map<String, Integer> occ = variableOccurrencesMap.get(argStrings[i]);
				if (occ == null) {
					occ = new HashMap<String, Integer>();
					variableOccurrencesMap.put(argStrings[i], occ);
				}
				occ.put(key, new Integer(i));
			}
		}
	}
	
	protected PFD0Precondition[] createPreconditions(boolean negativeEffects) {
		Map<String,String> preconditionStringsMap = parsePreconditionStrings();
		List<String> negativeEffectsKeyList = parseNegativeEffects();
		
		PFD0Precondition[] preconditions = new PFD0Precondition[preconditionStringsMap.size()];
		int i = 0;
		for (Entry<String, String> e : preconditionStringsMap.entrySet()) {
			String preKey = e.getKey();
			PFD0Precondition pre = createPrecondition(preKey, e.getValue());
			
			if (negativeEffects) {
				// Set negative effects
				if (negativeEffectsKeyList.contains(preKey)) {
					pre.setNegativeEffect(true);
				}
			}
			preconditions[i++] = pre;
		}
		return preconditions;
	}
	
	protected PFD0Precondition createPrecondition(String preKey, String preString) {
		String name = HybridDomain.extractName(preString);
		String[] args = HybridDomain.extractArgs(preString);

		// find bindings to head
		ArrayList<Integer> connectionsList = new ArrayList<Integer>();
		addVariableOccurrences(args, preKey);
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(HybridDomain.VARIABLE_INDICATOR)) {
				Integer headId = variableOccurrencesMap.get(args[i]).get("head");
				if (headId != null) {
					connectionsList.add(i);
					connectionsList.add(headId);
				}
			}
		}

		PFD0Precondition ret = new PFD0Precondition(name, args, 
				Ints.toArray(connectionsList),
				maxArgs,
				HybridDomain.EMPTYSTRING,
				preKey);
		System.out.println("PFD0Precondition " + ret);
		return ret;
	}
	
	protected Map<String,String[]> parseVariableDefinitions() {
		Map<String,String[]> variablesPossibleValuesMap = new HashMap<String, String[]>();
		// Parse variable definitions
		String[] varElements = HybridDomain.parseKeyword(HybridDomain.VARIABLE_RESTRICTION_KEYWORD, textualSpecification);
		for (String varElement : varElements) {
			String varName = varElement.substring(0, varElement.indexOf(" ")).trim();
			String[] values = varElement.substring(varElement.indexOf(" ")).trim().split(" ");
			variablesPossibleValuesMap.put(varName, values);
		}
		return variablesPossibleValuesMap;
	}
	
	protected Map<String,String> parsePreconditionStrings() {
		Map<String,String> preconditionStringsMap = new HashMap<String, String>();
		// Parse Preconditions
		String[] preElements = HybridDomain.parseKeyword(HybridDomain.PRECONDITION_KEYWORD, textualSpecification);
		for (String preElement : preElements) {
			String preKey = preElement.substring(0,preElement.indexOf(" ")).trim();
			String preFluent = preElement.substring(preElement.indexOf(" ")).trim();
			preconditionStringsMap.put(preKey, preFluent);
		}
		return preconditionStringsMap;
	}
	
	private List<String> parseNegativeEffects() {
		List<String> negativeEffectsKeyList = new ArrayList<String>();
		// Parse negative effects
		String[] negEffElements = HybridDomain.parseKeyword(HybridDomain.NEGATIVE_EFFECT_KEYWORD, textualSpecification);
		for (String negEff : negEffElements) {
			negativeEffectsKeyList.add(negEff);
		}
		return negativeEffectsKeyList;
	}
	
	protected Map<String,String> parseEffects(String keyword) {
		Map<String,String> effectsStringsMap = new HashMap<String, String>();
		String[] effElements = HybridDomain.parseKeyword(keyword, textualSpecification);
		for (String effElement : effElements) {
			String effKey = effElement.substring(0,effElement.indexOf(" ")).trim();
			String effFluent = effElement.substring(effElement.indexOf(" ")).trim();
			effectsStringsMap.put(effKey, effFluent);
		}
		return effectsStringsMap;
	}
	
	private void parseAllenIntervalConstraints() {
				// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.CONSTRAINT_KEYWORD, textualSpecification);
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
	}
	
	private void handleAdditionalConstraints() {
		
//		class AdditionalConstraint {
//			AllenIntervalConstraint con;
//			int from, to;
//			public AdditionalConstraint(AllenIntervalConstraint con, int from, int to) {
//				this.con = con;
//				this.from = from;
//				this.to = to;
//			}
//			public void addAdditionalConstraint(PlanReportroryItem item) {
//				//				item.addConstraint(con, from, to); // TODO
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
	}

}
