package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import com.google.common.primitives.Ints;

import fluentSolver.FluentNetworkSolver;
import htn.AdditionalConstraintTemplate;
import htn.EffectTemplate;
import htn.HTNPrecondition;
import htn.PlanReportroryItem;
import resourceFluent.ResourceUsageTemplate;

public abstract class PlanReportroiryItemParser {

	protected final String textualSpecification;
	protected final Map<String, String[]> typesInstancesMap;
	protected final int maxArgs;

	protected final String head;
	protected final String[] argStrings;
	protected final List<ResourceUsageTemplate> rtList = new ArrayList<ResourceUsageTemplate>();
	
	protected final Vector<AdditionalConstraintTemplate> additionalAIConstraints = 
			new Vector<AdditionalConstraintTemplate>(); 
	
	protected final Map<String, Map<String, Integer>> variableOccurrencesMap = 
			new HashMap<String, Map<String, Integer>>();

	protected final Map<String,String[]> variablesPossibleValuesMap = new HashMap<String, String[]>();

	public PlanReportroiryItemParser(String textualSpecification, Map<String, String[]> typesInstancesMap, int maxArs) {
		this.textualSpecification = textualSpecification;
		this.typesInstancesMap = typesInstancesMap;
		this.maxArgs = maxArs;

		this.head = HybridDomain.parseKeyword(HybridDomain.HEAD_KEYWORD, textualSpecification)[0].trim();
		argStrings = HybridDomain.extractArgs(head);
		addVariableOccurrences(argStrings, PlanReportroryItem.HEAD_KEYWORD_STRING);
		
		// Parse type definitions
		//		String[] typeElements = parseKeyword(TYPE_KEYWORD, textualSpecification);
		//		for (String typeElement : typeElements) {
		//			String varName = typeElement.substring(0,typeElement.indexOf(" ")).trim();
		//			String type = typeElement.substring(typeElement.indexOf(" ")).trim();
		//	     // TODO
		//		}
		
		parseAllenIntervalConstraints();
		
		// Parse Resources
		String[] resourceElements = HybridDomain.parseKeyword(HybridDomain.ACTION_RESOURCE_KEYWORD, 
				textualSpecification);
		for (String resElement : resourceElements) {
			ResourceUsageTemplate rt = HybridDomain.parseResourceUsage(resElement, false);
			rtList.add(rt);
		}
	}
	
	public abstract PlanReportroryItem create() throws DomainParsingException;
	
	protected void addVariableOccurrences(String[] argStrings, String key) {
		for (int i = 0; i < argStrings.length;  i++) {
			if (argStrings[i].startsWith(HybridDomain.VARIABLE_INDICATOR)) {
				Map<String, Integer> occ = variableOccurrencesMap.get(argStrings[i]);
				if (occ == null) {
					occ = new HashMap<String, Integer>();
					variableOccurrencesMap.put(argStrings[i], occ);
				}
				occ.put(key, new Integer(i));
			} else {
				// It is a constant -> create dummy variable name to insert it into variableOccurrencesMap and add it to possible values
				String dummyVarName = key + "_v" + i;
				Map<String, Integer> occ = new HashMap<>();
				occ.put(key, new Integer(i));
				variableOccurrencesMap.put(dummyVarName, occ);
				variablesPossibleValuesMap.put(dummyVarName, new String[] {argStrings[i]});
			}
		}
	}
	
	protected HTNPrecondition[] createPreconditions(boolean negativeEffects) throws DomainParsingException {
		Map<String,String> preconditionStringsMap = parsePreconditionStrings();
		List<String> negativeEffectsKeyList = parseNegativeEffects();
		
		HTNPrecondition[] preconditions = new HTNPrecondition[preconditionStringsMap.size()];
		int i = 0;
		for (Entry<String, String> e : preconditionStringsMap.entrySet()) {
			String preKey = e.getKey();
			HTNPrecondition pre = createPrecondition(preKey, e.getValue());
			
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
	
	protected HTNPrecondition createPrecondition(String preKey, String preString) throws DomainParsingException {
		String name = HybridDomain.extractName(preString);
		String[] args = HybridDomain.extractArgs(preString);

		// find bindings to head
		ArrayList<Integer> connectionsList = new ArrayList<Integer>();
		addVariableOccurrences(args, preKey);
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(HybridDomain.VARIABLE_INDICATOR)) {
				Integer headId = variableOccurrencesMap.get(args[i]).get(PlanReportroryItem.HEAD_KEYWORD_STRING);
				if (headId != null) {
					connectionsList.add(i);
					connectionsList.add(headId);
				}
			}
		}

		HTNPrecondition ret = new HTNPrecondition(name, args, Ints.toArray(connectionsList), maxArgs, 
				HybridDomain.EMPTYSTRING, preKey);
		for (AdditionalConstraintTemplate additionalCon : additionalAIConstraints) {
			if (additionalCon.involvesHeadAndKey(preKey)) {
				ret.addAdditionalConstraint(additionalCon);
			}
		}
		
		return ret;
	}
	
	protected SubDifferentDefinition[] parseSubDifferentDefinitions() {
		String[] varElements = HybridDomain.parseKeyword(HybridDomain.VARIABLES_DIFFERENT_KEYWORD, textualSpecification);
		SubDifferentDefinition[] ret = new SubDifferentDefinition[varElements.length];
		for (int i = 0; i < varElements.length; i++) {
			String varElement = varElements[i];
			String fromKey = varElement.substring(0, varElement.indexOf(" ")).trim();
			String toKey = varElement.substring(varElement.indexOf(" ")).trim();
			ret[i] = new SubDifferentDefinition(fromKey, toKey);
		}
		return ret;
	}
	
	protected Map<String,String[]> parseValueRestrictions(String valueKeyword, String typeKeyword)
			throws DomainParsingException {
		Map<String,String[]> ret = new HashMap<String, String[]>();
		// Parse variable definitions
		String[] varElements = HybridDomain.parseKeyword(valueKeyword, textualSpecification);
		for (String varElement : varElements) {
			String varName = varElement.substring(0, varElement.indexOf(" ")).trim();
			String[] values = varElement.substring(varElement.indexOf(" ")).trim().split(" ");
			ret.put(varName, values);
		}
		
		String[] typeElements = HybridDomain.parseKeyword(typeKeyword, textualSpecification);
		if (typesInstancesMap == null && typeElements.length > 0) {
			throw new DomainParsingException("Specified types in the domain but the planner does not know the instances.");
		}
		for (String typeElement : typeElements) {
			String varName = typeElement.substring(0, typeElement.indexOf(" ")).trim();
			if (!ret.containsKey(varName)) {        // only add type restriction if ValueRestriction not used.
				
				String[] types = typeElement.substring(typeElement.indexOf(" ")).trim().split(" ");
				List<String> values = new ArrayList<String>();
				for (String type : types) {
					String[] instances = typesInstancesMap.get(type);
					if (instances != null) {
						for (String instance : instances) {
							values.add(instance);
						}
					} else {
						throw new DomainParsingException("Type " + type + " specified but not in typesInstancesMap");
					}
				}
				ret.put(varName, values.toArray(new String[values.size()]));
			}
			
		}
		
		return ret;
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
	
	protected EffectTemplate[] createEffectTemplates (String component, String keyword) throws DomainParsingException {
		Map<String, String> effectStringsMap = parseEffects(keyword);
		EffectTemplate[] ret = new EffectTemplate[effectStringsMap.size()];
		
		int i = 0;
		for (Entry<String, String> e : effectStringsMap.entrySet()) {
			ret[i++] = createEffectTemplate(component, e.getKey(), e.getValue());
		}
		return ret;
	}
	
	protected int parsePreferenceWeight() {
		
		String[] splitStr = textualSpecification.split("\\s+");
		try {
			return Integer.parseInt(splitStr[0]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	private void parseAllenIntervalConstraints() {
		// Parse AllenIntervalConstraints:
		String[] constraintElements = HybridDomain.parseKeyword(HybridDomain.CONSTRAINT_KEYWORD, textualSpecification);
		for (String conElement : constraintElements) {
			try {
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
					try {
						constraintName = conElement.substring(0,conElement.indexOf("(")).trim();
					} catch (Exception e) {
						System.err.println(e);
						System.out.println(conElement);
						throw new IllegalArgumentException();
					}
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
				additionalAIConstraints.add(new AdditionalConstraintTemplate(con, from, to));
			} catch (Exception e) {
				System.err.println(e);
				System.out.println(conElement);
				throw new IllegalArgumentException();
			}
		}
	}
	
//	private void handleAdditionalConstraints() {
//		
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
//	}
	
	protected EffectTemplate createEffectTemplate(String component, String subKey, String subString) throws DomainParsingException {
		String name = HybridDomain.extractName(subString);
		String[] args = HybridDomain.extractArgs(subString);

		addVariableOccurrences(args, subKey);
		
		EffectTemplate et = new EffectTemplate(subKey, name, args, maxArgs, component);
		// Add additional AllenIntervalConstraints
		for (AdditionalConstraintTemplate additionalCon : additionalAIConstraints) {
			if (additionalCon.involvesHeadAndKey(subKey)) {
				et.addAdditionalConstraint(additionalCon);
			}
		}

		return et;
	}

	protected AdditionalConstraintTemplate[] filterAdditionalConstraints() {
		ArrayList<AdditionalConstraintTemplate> ret = new ArrayList<AdditionalConstraintTemplate>();
		for (AdditionalConstraintTemplate act : additionalAIConstraints) {
			if (act.headToHead() || act.withoutHead()) {
				ret.add(act);
			}
		}
		return ret.toArray(new AdditionalConstraintTemplate[ret.size()]);
	}
	
}
