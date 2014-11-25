package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.metacsp.framework.VariablePrototype;

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
		
		// Create positive effects
		Map<String, VariablePrototype> effectsMap = new HashMap<String, VariablePrototype>();
		for (Entry<String, String> e : parseEffects(HybridDomain.EFFECT_KEYWORD).entrySet()) {
			String effKey = e.getKey();
			effectsMap.put(effKey, createEffect(effKey, e.getValue()));
		}
		
		String headname = HybridDomain.extractName(head);
		PFD0Operator op =  new PFD0Operator(headname, argStrings, preconditions, effectsMap);
		op.setVariableOccurrencesMap(variableOccurrencesMap);
		
		Map<String,String[]> variablesPossibleValuesMap = parseVariableDefinitions();
		op.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		op.addResourceUsageTemplates(rtList);
		return op;
	}

	
	private VariablePrototype createEffect(String effKey, String effString) {
		String name = HybridDomain.extractName(effString);
		String[] args = HybridDomain.extractArgs(effString);
		
		addVariableOccurrences(args, effKey); 

		// fill arguments array up to maxargs
		String[] filledArgs = new String[maxArgs];
		for (int i = 0; i < args.length; i++) {
			filledArgs[i] = args[i];
		}
		for (int i = args.length; i < maxArgs; i++) {
			filledArgs[i] = HybridDomain.EMPTYSTRING;
		}
		VariablePrototype ret = new VariablePrototype(groundSolver, "S", name, filledArgs);
		return ret;
	}
	
}
