// Generated from /home/sebastian/code/chimp/src/main/antlr/ChimpClassic.g4 by ANTLR 4.8
package hybridDomainParsing.classic.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ChimpClassicParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ChimpClassicVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#domain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain(ChimpClassicParser.DomainContext ctx);
	/**
	 * Visit a parse tree produced by the {@code domain_item_resource}
	 * labeled alternative in {@link ChimpClassicParser#domain_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_item_resource(ChimpClassicParser.Domain_item_resourceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code domain_item_fluentresourceusage}
	 * labeled alternative in {@link ChimpClassicParser#domain_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_item_fluentresourceusage(ChimpClassicParser.Domain_item_fluentresourceusageContext ctx);
	/**
	 * Visit a parse tree produced by the {@code domain_item_statevariable}
	 * labeled alternative in {@link ChimpClassicParser#domain_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_item_statevariable(ChimpClassicParser.Domain_item_statevariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code domain_item_operator}
	 * labeled alternative in {@link ChimpClassicParser#domain_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_item_operator(ChimpClassicParser.Domain_item_operatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code domain_item_method}
	 * labeled alternative in {@link ChimpClassicParser#domain_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_item_method(ChimpClassicParser.Domain_item_methodContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#domain_name_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDomain_name_def(ChimpClassicParser.Domain_name_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#maxargs_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaxargs_def(ChimpClassicParser.Maxargs_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#maxintargs_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaxintargs_def(ChimpClassicParser.Maxintargs_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#predicatesymbols_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicatesymbols_def(ChimpClassicParser.Predicatesymbols_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#predicate_symbol}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_symbol(ChimpClassicParser.Predicate_symbolContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#resource_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_def(ChimpClassicParser.Resource_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#statevariable_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatevariable_def(ChimpClassicParser.Statevariable_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#statevariable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatevariable_name(ChimpClassicParser.Statevariable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#method_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_def(ChimpClassicParser.Method_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#operator_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_def(ChimpClassicParser.Operator_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#head}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHead(ChimpClassicParser.HeadContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#int_args_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInt_args_def(ChimpClassicParser.Int_args_defContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precondition_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecondition_m_element(ChimpClassicParser.Precondition_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code temporal_constraint_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporal_constraint_m_element(ChimpClassicParser.Temporal_constraint_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code resource_usage_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_usage_m_element(ChimpClassicParser.Resource_usage_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_restriction_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_restriction_m_element(ChimpClassicParser.Value_restriction_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notvalue_restriction_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotvalue_restriction_m_element(ChimpClassicParser.Notvalue_restriction_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typevalue_restriction_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypevalue_restriction_m_element(ChimpClassicParser.Typevalue_restriction_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nottypevalue_restriction_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNottypevalue_restriction_m_element(ChimpClassicParser.Nottypevalue_restriction_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vardifferent_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardifferent_m_element(ChimpClassicParser.Vardifferent_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subtask_def_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubtask_def_m_element(ChimpClassicParser.Subtask_def_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ordering_def_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdering_def_m_element(ChimpClassicParser.Ordering_def_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integer_constraint_m_element}
	 * labeled alternative in {@link ChimpClassicParser#method_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constraint_m_element(ChimpClassicParser.Integer_constraint_m_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code precondition_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecondition_op_element(ChimpClassicParser.Precondition_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code temporal_constraint_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporal_constraint_op_element(ChimpClassicParser.Temporal_constraint_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code positive_effect_def_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositive_effect_def_op_element(ChimpClassicParser.Positive_effect_def_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code negative_effect_def_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegative_effect_def_op_element(ChimpClassicParser.Negative_effect_def_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code resource_usage_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_usage_op_element(ChimpClassicParser.Resource_usage_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code value_restriction_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_restriction_op_element(ChimpClassicParser.Value_restriction_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notvalue_restriction_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotvalue_restriction_op_element(ChimpClassicParser.Notvalue_restriction_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typevalue_restriction_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypevalue_restriction_op_element(ChimpClassicParser.Typevalue_restriction_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nottypevalue_restriction_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNottypevalue_restriction_op_element(ChimpClassicParser.Nottypevalue_restriction_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vardifferent_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardifferent_op_element(ChimpClassicParser.Vardifferent_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integer_constraint_op_element}
	 * labeled alternative in {@link ChimpClassicParser#op_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constraint_op_element(ChimpClassicParser.Integer_constraint_op_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#precondition_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecondition_def(ChimpClassicParser.Precondition_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#subtask_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubtask_def(ChimpClassicParser.Subtask_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#positive_effect_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositive_effect_def(ChimpClassicParser.Positive_effect_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#negative_effect_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegative_effect_def(ChimpClassicParser.Negative_effect_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#ordering_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdering_constraint_def(ChimpClassicParser.Ordering_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#integer_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constraint_def(ChimpClassicParser.Integer_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#integer_constraint1_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constraint1_def(ChimpClassicParser.Integer_constraint1_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#integer_constraint2_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constraint2_def(ChimpClassicParser.Integer_constraint2_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#integer_operator1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_operator1(ChimpClassicParser.Integer_operator1Context ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#integer_operator2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_operator2(ChimpClassicParser.Integer_operator2Context ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#temporal_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemporal_constraint_def(ChimpClassicParser.Temporal_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#unary_temporal_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_temporal_constraint_def(ChimpClassicParser.Unary_temporal_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#unary_temporal_constraint_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnary_temporal_constraint_type(ChimpClassicParser.Unary_temporal_constraint_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#binary_temporal_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinary_temporal_constraint_def(ChimpClassicParser.Binary_temporal_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#binary_temporal_constraint_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinary_temporal_constraint_type(ChimpClassicParser.Binary_temporal_constraint_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#bounds_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBounds_list(ChimpClassicParser.Bounds_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#bounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBounds(ChimpClassicParser.BoundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#bound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBound(ChimpClassicParser.BoundContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#id_or_task}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_or_task(ChimpClassicParser.Id_or_taskContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#resource_usage_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_usage_def(ChimpClassicParser.Resource_usage_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#fluentresourceusage_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFluentresourceusage_def(ChimpClassicParser.Fluentresourceusage_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#fluent_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFluent_def(ChimpClassicParser.Fluent_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#usage_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsage_def(ChimpClassicParser.Usage_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#param_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_item(ChimpClassicParser.Param_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#value_restriction_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_restriction_def(ChimpClassicParser.Value_restriction_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#notvalue_restriction_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotvalue_restriction_def(ChimpClassicParser.Notvalue_restriction_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#typevalue_restriction_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypevalue_restriction_def(ChimpClassicParser.Typevalue_restriction_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#nottypevalue_restriction_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNottypevalue_restriction_def(ChimpClassicParser.Nottypevalue_restriction_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#vardifferent_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVardifferent_def(ChimpClassicParser.Vardifferent_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#constant_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_list(ChimpClassicParser.Constant_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(ChimpClassicParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#predicate_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_args(ChimpClassicParser.Predicate_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#int_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInt_args(ChimpClassicParser.Int_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#var_or_int}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_or_int(ChimpClassicParser.Var_or_intContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(ChimpClassicParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChimpClassicParser#var_or_const}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_or_const(ChimpClassicParser.Var_or_constContext ctx);
}