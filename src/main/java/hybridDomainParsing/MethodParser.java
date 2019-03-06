package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.VariablePrototype;

import fluentSolver.FluentConstraint;
import htn.EffectTemplate;
import htn.HTNMethod;
import htn.HTNPrecondition;

public class MethodParser extends PlanReportroiryItemParser {

	public MethodParser(String textualSpecification, HybridDomainPlanner planner, int maxArgs) {
		super(textualSpecification, planner, maxArgs);
	}
	
	@Override
	public HTNMethod create() throws DomainParsingException {
		HTNPrecondition[] preconditions = createPreconditions(false);

		String headname = HybridDomain.extractName(head);
		EffectTemplate[] subtasks = createEffectTemplates("Task", HybridDomain.SUBTASK_KEYWORD);
		Constraint[] orderingCons = parseFluentBeforeConstraints(subtasks);
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
	
	private Constraint[] parseFluentBeforeConstraints(EffectTemplate[] subtasks) {
		Map<String, VariablePrototype> keyToPrototypesMap = new HashMap<String, VariablePrototype>();
		for (EffectTemplate et : subtasks) {
			keyToPrototypesMap.put(et.getKey(), et.getPrototype());
		}
		
		String[] orderingElements = 
				HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, textualSpecification);
		List<Constraint> cons = new ArrayList<Constraint>();
		for (String orderingElement : orderingElements) {
			String fromKey = orderingElement.substring(0,orderingElement.indexOf(" ")).trim();
			String toKey = orderingElement.substring(orderingElement.indexOf(" ")).trim();
			FluentConstraint con = new FluentConstraint(FluentConstraint.Type.BEFORE);
			con.setFrom(keyToPrototypesMap.get(fromKey));
			con.setTo(keyToPrototypesMap.get(toKey));
			cons.add(con);
		}
		return cons.toArray(new Constraint[cons.size()]);
	}
	
}
