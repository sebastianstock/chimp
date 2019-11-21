package hybridDomainParsing;

import java.util.Map;

import fluentSolver.FluentNetworkSolver;
import htn.EffectTemplate;
import htn.HTNOperator;
import htn.HTNPrecondition;

public class OperatorParser extends PlanReportroiryItemParser {
	

	public OperatorParser(String textualSpecification, Map<String, String[]> typesInstancesMap, int maxArgs){
		super(textualSpecification, typesInstancesMap, maxArgs);
	}
	
	@Override
	public HTNOperator create() throws DomainParsingException {
		HTNPrecondition[] preconditions = createPreconditions(true);
		
		String headname = HybridDomain.extractName(head);
		EffectTemplate[] effects = createEffectTemplates("PlannedState", HybridDomain.EFFECT_KEYWORD);
		int preferenceWeight = parsePreferenceWeight();
		HTNOperator op =  new HTNOperator(headname, argStrings, preconditions, effects, preferenceWeight);
		op.setVariableOccurrencesMap(variableOccurrencesMap);
		
		// add additional constraints from head to head or between preconditions or effects
		op.setAdditionalConstraints(filterAdditionalConstraints());
		
		variablesPossibleValuesMap.putAll(
				parseValueRestrictions(HybridDomain.VALUE_RESTRICTION_KEYWORD, HybridDomain.TYPE_KEYWORD));
		op.setVariablesPossibleValuesMap(variablesPossibleValuesMap); // TODO this is not necessary, anymore, as it is set in the constructor
		
		Map<String,String[]> variablesImpossibleValuesMap = parseValueRestrictions(HybridDomain.NEGATED_VALUE_RESTRICTION_KEYWORD, HybridDomain.NOT_TYPE_KEYWORD);
		op.setVariablesImpossibleValuesMap(variablesImpossibleValuesMap);
		
		SubDifferentDefinition[] subDiffs = parseSubDifferentDefinitions();
		op.setSubDifferentDefinitions(subDiffs);
		
		op.addResourceUsageTemplates(rtList);
		return op;
	}

}
