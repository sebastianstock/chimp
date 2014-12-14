package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.VariablePrototype;

import pfd0Symbolic.EffectTemplate;
import pfd0Symbolic.FluentConstraint;
import pfd0Symbolic.PFD0Method;
import pfd0Symbolic.PFD0Planner;
import pfd0Symbolic.PFD0Precondition;
import sun.security.pkcs.ParsingException;

public class MethodParser extends PlanReportroiryItemParser {

	public MethodParser(String textualSpecification, PFD0Planner planner, int maxArgs) {
		super(textualSpecification, planner, maxArgs);
		
		// TODO Should we parse resources here / Should methods consume resources?
	}
	
	public PFD0Method create() throws ParsingException {
		PFD0Precondition[] preconditions = createPreconditions(false);

		String headname = HybridDomain.extractName(head);
		EffectTemplate[] subtasks = createEffectTemplates(HybridDomain.SUBTASK_KEYWORD);
		Constraint[] orderingCons = parseFluentBeforeConstraints(subtasks);
		PFD0Method ret = new PFD0Method(headname, argStrings, preconditions, subtasks, orderingCons);
		ret.setVariableOccurrencesMap(variableOccurrencesMap);
		
		// add additional constraints from head to head or between preconditions or effects
		ret.setAdditionalConstraints(filterAdditionalConstraints());
		
		Map<String,String[]> variablesPossibleValuesMap = parseValueRestrictions(HybridDomain.VALUE_RESTRICTION_KEYWORD, HybridDomain.TYPE_KEYWORD);
		ret.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		
		Map<String,String[]> variablesImpossibleValuesMap = parseValueRestrictions(HybridDomain.NEGATED_VALUE_RESTRICTION_KEYWORD, HybridDomain.NOT_TYPE_KEYWORD);
		ret.setVariablesImpossibleValuesMap(variablesImpossibleValuesMap);
		
		SubDifferentDefinition[] subDiffs = parseSubDifferentDefinitions();
		ret.setSubDifferentDefinitions(subDiffs);
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
