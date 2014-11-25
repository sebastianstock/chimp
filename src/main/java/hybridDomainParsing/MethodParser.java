package hybridDomainParsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.VariablePrototype;

import pfd0Symbolic.FluentConstraint;
import pfd0Symbolic.FluentNetworkSolver;
import pfd0Symbolic.PFD0Method;
import pfd0Symbolic.PFD0Precondition;

public class MethodParser extends PlanReportroiryItemParser {

	public MethodParser(String textualSpecification, FluentNetworkSolver groundSolver, int maxArgs) {
		super(textualSpecification, groundSolver, maxArgs);
		
		// TODO Should we parse resources here / Should methods consume resources?
	}
	
	public PFD0Method create() {
		PFD0Precondition[] preconditions = createPreconditions(false);
		
		Map<String, VariablePrototype> subtasksMap = new HashMap<String, VariablePrototype>();
		for (Entry<String, String> e : parseEffects(HybridDomain.SUBTASK_KEYWORD).entrySet()) {
			String subtaskKey = e.getKey();
			subtasksMap.put(subtaskKey, createSubtask(subtaskKey, e.getValue()));
		}

		String headname = HybridDomain.extractName(head);
		Constraint[] orderingCons = parseFluentBeforeConstraints(subtasksMap);
		PFD0Method ret = new PFD0Method(headname, argStrings, preconditions, subtasksMap, orderingCons);
		ret.setVariableOccurrencesMap(variableOccurrencesMap);
		
		Map<String,String[]> variablesPossibleValuesMap = parseVariableDefinitions();
		ret.setVariablesPossibleValuesMap(variablesPossibleValuesMap);
		return ret;
	}
	
	private Constraint[] parseFluentBeforeConstraints(Map<String, VariablePrototype> subtasksMap) {
		String[] orderingElements = 
				HybridDomain.parseKeyword(HybridDomain.ORDERING_CONSTRAINT_KEYWORD, textualSpecification);
		List<Constraint> cons = new ArrayList<Constraint>();
		for (String orderingElement : orderingElements) {
			String fromKey = orderingElement.substring(0,orderingElement.indexOf(" ")).trim();
			String toKey = orderingElement.substring(orderingElement.indexOf(" ")).trim();
			FluentConstraint con = new FluentConstraint(FluentConstraint.Type.BEFORE);
			con.setFrom(subtasksMap.get(fromKey));
			con.setTo(subtasksMap.get(toKey));
			cons.add(con);
		}
		return cons.toArray(new Constraint[cons.size()]);
	}

	private VariablePrototype createSubtask(String subKey, String subString) {
		String name = HybridDomain.extractName(subString);
		String[] args = HybridDomain.extractArgs(subString);

		addVariableOccurrences(args, subKey); 

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
