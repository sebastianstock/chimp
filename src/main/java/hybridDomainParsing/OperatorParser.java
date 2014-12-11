package hybridDomainParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pfd0Symbolic.EffectTemplate;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Operator;
import pfd0Symbolic.PFD0Precondition;
import resourceFluent.ResourceUsageTemplate;

public class OperatorParser extends PlanReportroiryItemParser {
	
	
	private final List<ResourceUsageTemplate> rtList = new ArrayList<ResourceUsageTemplate>();
	

	public OperatorParser(String textualSpecification, FluentNetworkSolver groundSolver, int maxArgs){
		super(textualSpecification, groundSolver, maxArgs);

		// Parse Resources
		String[] resourceElements = HybridDomain.parseKeyword(HybridDomain.ACTION_RESOURCE_KEYWORD, 
				textualSpecification);
		for (String resElement : resourceElements) {
			ResourceUsageTemplate rt = HybridDomain.parseResourceUsage(resElement, false);
			rtList.add(rt);
		}
	}
	
	@Override
	public PFD0Operator create() {
		PFD0Precondition[] preconditions = createPreconditions(true);
		
		String headname = HybridDomain.extractName(head);
		EffectTemplate[] effects = createEffectTemplates(HybridDomain.EFFECT_KEYWORD);
		PFD0Operator op =  new PFD0Operator(headname, argStrings, preconditions, effects);
		op.setVariableOccurrencesMap(variableOccurrencesMap);
		
		// add additional constraints from head to head or between preconditions or effects
		op.setAdditionalConstraints(filterAdditionalConstraints());
		
		Map<String,String[]> variablesPossibleValuesMap = parseVariableDefinitions();
		op.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		
		SubDifferentDefinition[] subDiffs = parseSubDifferentDefinitions();
		op.setSubDifferentDefinitions(subDiffs);
		
		op.addResourceUsageTemplates(rtList);
		return op;
	}

}
