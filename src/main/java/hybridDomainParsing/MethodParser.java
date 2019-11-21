package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import htn.OrderingConstraintTemplate;
import htn.EffectTemplate;
import htn.HTNMethod;
import htn.HTNPrecondition;

public class MethodParser extends PlanReportroiryItemParser {

	public MethodParser(String textualSpecification, Map<String, String[]> typesInstancesMap, int maxArgs) {
		super(textualSpecification, typesInstancesMap, maxArgs);
	}
	
	@Override
	public HTNMethod create() throws DomainParsingException {
		HTNPrecondition[] preconditions = createPreconditions(false);

		String headname = HybridDomain.extractName(head);
		EffectTemplate[] subtasks = createEffectTemplates("Task", HybridDomain.SUBTASK_KEYWORD);
		OrderingConstraintTemplate[] orderingCons = parseOrderingConstraints(subtasks);
		int preferenceWeight = parsePreferenceWeight();
		HTNMethod ret = new HTNMethod(headname, argStrings, preconditions, subtasks, orderingCons, preferenceWeight);
		ret.setVariableOccurrencesMap(variableOccurrencesMap);
		
		// add additional constraints from head to head or between preconditions or effects
		ret.setAdditionalConstraints(filterAdditionalConstraints());
		
		variablesPossibleValuesMap.putAll(parseValueRestrictions(HybridDomain.VALUE_RESTRICTION_KEYWORD, HybridDomain.TYPE_KEYWORD));
		ret.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		
		Map<String,String[]> variablesImpossibleValuesMap = parseValueRestrictions(HybridDomain.NEGATED_VALUE_RESTRICTION_KEYWORD, HybridDomain.NOT_TYPE_KEYWORD);
		ret.setVariablesImpossibleValuesMap(variablesImpossibleValuesMap);
		
		SubDifferentDefinition[] subDiffs = parseSubDifferentDefinitions();
		ret.setSubDifferentDefinitions(subDiffs);
		
		ret.addResourceUsageTemplates(rtList);
		return ret;
	}
	
	private OrderingConstraintTemplate[] parseOrderingConstraints(EffectTemplate[] subtasks) {
		Map<String, EffectTemplate> keyToEffectTemplatesMap = new HashMap<>();
		for (EffectTemplate et : subtasks) {
			keyToEffectTemplatesMap.put(et.getKey(), et);
		}
		
		String[] orderingElements = 
				HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, textualSpecification);
		List<OrderingConstraintTemplate> ret = new ArrayList<OrderingConstraintTemplate>();
		for (String orderingElement : orderingElements) {
			String fromKey = orderingElement.substring(0,orderingElement.indexOf(" ")).trim();
			String toKey = orderingElement.substring(orderingElement.indexOf(" ")).trim();
			ret.add(new OrderingConstraintTemplate(keyToEffectTemplatesMap.get(fromKey),
					keyToEffectTemplatesMap.get(toKey)));
		}
		return ret.toArray(new OrderingConstraintTemplate[ret.size()]);
	}
	
}
