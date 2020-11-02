grammar ChimpClassic;

domain : domain_name_def maxargs_def maxintargs_def? predicatesymbols_def domain_item*;

domain_item : resource_def #domain_item_resource
              | fluentresourceusage_def #domain_item_fluentresourceusage
              | statevariable_def #domain_item_statevariable
              | operator_def #domain_item_operator
              | method_def #domain_item_method
              ;

domain_name_def : '(HybridHTNDomain' NAME ')';

maxargs_def : '(MaxArgs' NUMBER ')';

maxintargs_def : '(MaxIntegerArgs' NUMBER ')';

predicatesymbols_def : '(PredicateSymbols' predicate_symbol* ')';

predicate_symbol : NAME;

resource_def : '(Resource' NAME NUMBER ')';

statevariable_def : '(StateVariable' statevariable_name NUMBER NAME+ ')';

statevariable_name : NAME;

method_def : '(:method' NUMBER? head method_element* ')';

operator_def : '(:operator' NUMBER? head op_element* ')';

head : '(Head' predicate_symbol '(' predicate_args ')' int_args_def? ')';

int_args_def : '(' int_args ')';

method_element : precondition_def #precondition_m_element
             | temporal_constraint_def #temporal_constraint_m_element
             | resource_usage_def #resource_usage_m_element
             | value_restriction_def #value_restriction_m_element
             | notvalue_restriction_def #notvalue_restriction_m_element
             | typevalue_restriction_def #typevalue_restriction_m_element
             | nottypevalue_restriction_def #nottypevalue_restriction_m_element
             | vardifferent_def #vardifferent_m_element
             | subtask_def #subtask_def_m_element
             | ordering_constraint_def #ordering_def_m_element
             | integer_constraint_def #integer_constraint_m_element
             ;

op_element : precondition_def #precondition_op_element
             | temporal_constraint_def #temporal_constraint_op_element
             | positive_effect_def #positive_effect_def_op_element
             | negative_effect_def #negative_effect_def_op_element
             | resource_usage_def #resource_usage_op_element
             | value_restriction_def #value_restriction_op_element
             | notvalue_restriction_def #notvalue_restriction_op_element
             | typevalue_restriction_def #typevalue_restriction_op_element
             | nottypevalue_restriction_def #nottypevalue_restriction_op_element
             | vardifferent_def #vardifferent_op_element
             | integer_constraint_def #integer_constraint_op_element
             ;

precondition_def : '(Pre' id predicate ')';

subtask_def : '(Sub' id predicate ')';

positive_effect_def : '(Add' id predicate ')';

negative_effect_def : '(Del' id ')';

ordering_constraint_def : '(Ordering' id id ')';

integer_constraint_def : integer_constraint1_def | integer_constraint2_def;

integer_constraint1_def : '(IC' VAR_NAME integer_operator1 var_or_int ')';

integer_constraint2_def : '(IC' VAR_NAME integer_operator1 VAR_NAME integer_operator2 var_or_int ')';

integer_operator1 : '=' | '!=' | '>' | '<' | '>=' | '<=';

integer_operator2 : '=' | '!=' | '>' | '<' | '>=' | '<=' | '+' | '-' | '*' | '/';

temporal_constraint_def : unary_temporal_constraint_def | binary_temporal_constraint_def;

unary_temporal_constraint_def : unary_temporal_constraint_type bounds_list '(' id_or_task ')' ')';

unary_temporal_constraint_type : '(Constraint Duration' | '(Constraint Release' | '(Constraint Deadline' | '(Constraint Forever' | '(Constraint At';

binary_temporal_constraint_def : '(Constraint' binary_temporal_constraint_type bounds_list '(' id_or_task ',' id_or_task ')' ')';

binary_temporal_constraint_type : 'Before' | 'Meets' | 'Overlaps' | 'FinishedBy' | 'Contains' | 'StartedBy'| 'Equals'
                           | 'Starts'| 'During' | 'Finishes' | 'OverlappedBy' | 'After' | 'MetBy'
                           | 'BeforeOrMeets' | 'MetByOrAfter' | 'MetByOrOverlappedBy'
                           | 'MetByOrOverlappedByOrAfter' | 'MetByOrOverlappedByOrIsFinishedByOrDuring'
                           | 'MeetsOrOverlapsOrBefore' | 'DuringOrEquals' | 'DuringOrEqualsOrStartsOrFinishes'
                           | 'MeetsOrOverlapsOrFinishedByOrContains' | 'ContainsOrStartedByOrOverlappedByOrMetBy'
                           | 'EndsDuring' | 'EndsOrEndedBy' | 'StartsOrStartedBy' | 'NotBeforeAndNotAfter';

bounds_list : bounds*;

bounds : '[' bound ',' bound ']';

bound : NUMBER | 'INF';

id_or_task : id | 'task';

resource_usage_def : '(ResourceUsage' NAME NUMBER ')'
                   | '(ResourceUsage' usage_def param_item* ')';

fluentresourceusage_def : '(FluentResourceUsage' usage_def fluent_def param_item* ')';

fluent_def : '(Fluent' NAME ')';

usage_def : '(Usage' NAME NUMBER ')';

param_item : '(Param' NUMBER NAME ')';

value_restriction_def : '(Values' VAR_NAME constant_list ')';

notvalue_restriction_def : '(NotValues' VAR_NAME constant_list ')';

typevalue_restriction_def : '(Type' VAR_NAME constant_list ')';

nottypevalue_restriction_def : '(NotType' VAR_NAME constant_list ')';

vardifferent_def : '(VarDifferent' VAR_NAME VAR_NAME ')';

constant_list : NAME+;

predicate : NAME '(' predicate_args ')' int_args_def?;

predicate_args : var_or_const*;

int_args : var_or_int*;

var_or_int : VAR_NAME | NUMBER;

id : NAME;

var_or_const : NAME | VAR_NAME;

/*
 * Lexer Rules
*/

VAR_NAME : '?'NAME;

NAME : [a-zA-Z!][a-zA-Z0-9\-_!]* ;
/*COMMENT : ('#' ~[\r\n]* ('\r'|'\n') ('\r'|'\n')? ) -> skip ; */
Comment
  :  '#' ~( '\r' | '\n' )* -> skip
  ;
WS : [ \t\r\n]+ -> skip ;
NUMBER : [0-9][0-9]*;