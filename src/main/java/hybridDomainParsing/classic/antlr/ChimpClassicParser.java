// Generated from /home/sebastian/code/chimp/src/main/antlr/ChimpClassic.g4 by ANTLR 4.8
package hybridDomainParsing.classic.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ChimpClassicParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, T__36=37, T__37=38, 
		T__38=39, T__39=40, T__40=41, T__41=42, T__42=43, T__43=44, T__44=45, 
		T__45=46, T__46=47, T__47=48, T__48=49, T__49=50, T__50=51, T__51=52, 
		T__52=53, T__53=54, T__54=55, T__55=56, T__56=57, T__57=58, T__58=59, 
		T__59=60, T__60=61, T__61=62, T__62=63, T__63=64, T__64=65, T__65=66, 
		T__66=67, T__67=68, T__68=69, T__69=70, T__70=71, T__71=72, T__72=73, 
		T__73=74, T__74=75, VAR_NAME=76, NAME=77, Comment=78, WS=79, NUMBER=80;
	public static final int
		RULE_domain = 0, RULE_domain_item = 1, RULE_domain_name_def = 2, RULE_maxargs_def = 3, 
		RULE_maxintargs_def = 4, RULE_predicatesymbols_def = 5, RULE_predicate_symbol = 6, 
		RULE_resource_def = 7, RULE_statevariable_def = 8, RULE_statevariable_name = 9, 
		RULE_method_def = 10, RULE_operator_def = 11, RULE_head = 12, RULE_int_args_def = 13, 
		RULE_method_element = 14, RULE_op_element = 15, RULE_precondition_def = 16, 
		RULE_subtask_def = 17, RULE_positive_effect_def = 18, RULE_negative_effect_def = 19, 
		RULE_ordering_constraint_def = 20, RULE_integer_constraint_def = 21, RULE_integer_constraint1_def = 22, 
		RULE_integer_constraint2_def = 23, RULE_integer_operator1 = 24, RULE_integer_operator2 = 25, 
		RULE_temporal_constraint_def = 26, RULE_unary_temporal_constraint_def = 27, 
		RULE_unary_temporal_constraint_type = 28, RULE_binary_temporal_constraint_def = 29, 
		RULE_binary_temporal_constraint_type = 30, RULE_bounds_list = 31, RULE_bounds = 32, 
		RULE_bound = 33, RULE_id_or_task = 34, RULE_resource_usage_def = 35, RULE_fluentresourceusage_def = 36, 
		RULE_fluent_def = 37, RULE_usage_def = 38, RULE_param_item = 39, RULE_value_restriction_def = 40, 
		RULE_notvalue_restriction_def = 41, RULE_typevalue_restriction_def = 42, 
		RULE_nottypevalue_restriction_def = 43, RULE_vardifferent_def = 44, RULE_constant_list = 45, 
		RULE_predicate = 46, RULE_predicate_args = 47, RULE_int_args = 48, RULE_var_or_int = 49, 
		RULE_id = 50, RULE_var_or_const = 51;
	private static String[] makeRuleNames() {
		return new String[] {
			"domain", "domain_item", "domain_name_def", "maxargs_def", "maxintargs_def", 
			"predicatesymbols_def", "predicate_symbol", "resource_def", "statevariable_def", 
			"statevariable_name", "method_def", "operator_def", "head", "int_args_def", 
			"method_element", "op_element", "precondition_def", "subtask_def", "positive_effect_def", 
			"negative_effect_def", "ordering_constraint_def", "integer_constraint_def", 
			"integer_constraint1_def", "integer_constraint2_def", "integer_operator1", 
			"integer_operator2", "temporal_constraint_def", "unary_temporal_constraint_def", 
			"unary_temporal_constraint_type", "binary_temporal_constraint_def", "binary_temporal_constraint_type", 
			"bounds_list", "bounds", "bound", "id_or_task", "resource_usage_def", 
			"fluentresourceusage_def", "fluent_def", "usage_def", "param_item", "value_restriction_def", 
			"notvalue_restriction_def", "typevalue_restriction_def", "nottypevalue_restriction_def", 
			"vardifferent_def", "constant_list", "predicate", "predicate_args", "int_args", 
			"var_or_int", "id", "var_or_const"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'(HybridHTNDomain'", "')'", "'(MaxArgs'", "'(MaxIntegerArgs'", 
			"'(PredicateSymbols'", "'(Resource'", "'(StateVariable'", "'(:method'", 
			"'(:operator'", "'(Head'", "'('", "'(Pre'", "'(Sub'", "'(Add'", "'(Del'", 
			"'(Ordering'", "'(IC'", "'='", "'!='", "'>'", "'<'", "'>='", "'<='", 
			"'+'", "'-'", "'*'", "'/'", "'(Constraint Duration'", "'(Constraint Release'", 
			"'(Constraint Deadline'", "'(Constraint Forever'", "'(Constraint At'", 
			"'(Constraint'", "','", "'Before'", "'Meets'", "'Overlaps'", "'FinishedBy'", 
			"'Contains'", "'StartedBy'", "'Equals'", "'Starts'", "'During'", "'Finishes'", 
			"'OverlappedBy'", "'After'", "'MetBy'", "'BeforeOrMeets'", "'MetByOrAfter'", 
			"'MetByOrOverlappedBy'", "'MetByOrOverlappedByOrAfter'", "'MetByOrOverlappedByOrIsFinishedByOrDuring'", 
			"'MeetsOrOverlapsOrBefore'", "'DuringOrEquals'", "'DuringOrEqualsOrStartsOrFinishes'", 
			"'MeetsOrOverlapsOrFinishedByOrContains'", "'ContainsOrStartedByOrOverlappedByOrMetBy'", 
			"'EndsDuring'", "'EndsOrEndedBy'", "'StartsOrStartedBy'", "'NotBeforeAndNotAfter'", 
			"'['", "']'", "'INF'", "'task'", "'(ResourceUsage'", "'(FluentResourceUsage'", 
			"'(Fluent'", "'(Usage'", "'(Param'", "'(Values'", "'(NotValues'", "'(Type'", 
			"'(NotType'", "'(VarDifferent'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, "VAR_NAME", "NAME", "Comment", "WS", "NUMBER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ChimpClassic.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ChimpClassicParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class DomainContext extends ParserRuleContext {
		public Domain_name_defContext domain_name_def() {
			return getRuleContext(Domain_name_defContext.class,0);
		}
		public Maxargs_defContext maxargs_def() {
			return getRuleContext(Maxargs_defContext.class,0);
		}
		public Predicatesymbols_defContext predicatesymbols_def() {
			return getRuleContext(Predicatesymbols_defContext.class,0);
		}
		public Maxintargs_defContext maxintargs_def() {
			return getRuleContext(Maxintargs_defContext.class,0);
		}
		public List<Domain_itemContext> domain_item() {
			return getRuleContexts(Domain_itemContext.class);
		}
		public Domain_itemContext domain_item(int i) {
			return getRuleContext(Domain_itemContext.class,i);
		}
		public DomainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domain; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DomainContext domain() throws RecognitionException {
		DomainContext _localctx = new DomainContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_domain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			domain_name_def();
			setState(105);
			maxargs_def();
			setState(107);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(106);
				maxintargs_def();
				}
			}

			setState(109);
			predicatesymbols_def();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 6)) & ~0x3f) == 0 && ((1L << (_la - 6)) & ((1L << (T__5 - 6)) | (1L << (T__6 - 6)) | (1L << (T__7 - 6)) | (1L << (T__8 - 6)) | (1L << (T__66 - 6)))) != 0)) {
				{
				{
				setState(110);
				domain_item();
				}
				}
				setState(115);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Domain_itemContext extends ParserRuleContext {
		public Domain_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domain_item; }
	 
		public Domain_itemContext() { }
		public void copyFrom(Domain_itemContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Domain_item_operatorContext extends Domain_itemContext {
		public Operator_defContext operator_def() {
			return getRuleContext(Operator_defContext.class,0);
		}
		public Domain_item_operatorContext(Domain_itemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_item_operator(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Domain_item_statevariableContext extends Domain_itemContext {
		public Statevariable_defContext statevariable_def() {
			return getRuleContext(Statevariable_defContext.class,0);
		}
		public Domain_item_statevariableContext(Domain_itemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_item_statevariable(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Domain_item_resourceContext extends Domain_itemContext {
		public Resource_defContext resource_def() {
			return getRuleContext(Resource_defContext.class,0);
		}
		public Domain_item_resourceContext(Domain_itemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_item_resource(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Domain_item_fluentresourceusageContext extends Domain_itemContext {
		public Fluentresourceusage_defContext fluentresourceusage_def() {
			return getRuleContext(Fluentresourceusage_defContext.class,0);
		}
		public Domain_item_fluentresourceusageContext(Domain_itemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_item_fluentresourceusage(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Domain_item_methodContext extends Domain_itemContext {
		public Method_defContext method_def() {
			return getRuleContext(Method_defContext.class,0);
		}
		public Domain_item_methodContext(Domain_itemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_item_method(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Domain_itemContext domain_item() throws RecognitionException {
		Domain_itemContext _localctx = new Domain_itemContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_domain_item);
		try {
			setState(121);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
				_localctx = new Domain_item_resourceContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(116);
				resource_def();
				}
				break;
			case T__66:
				_localctx = new Domain_item_fluentresourceusageContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(117);
				fluentresourceusage_def();
				}
				break;
			case T__6:
				_localctx = new Domain_item_statevariableContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(118);
				statevariable_def();
				}
				break;
			case T__8:
				_localctx = new Domain_item_operatorContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(119);
				operator_def();
				}
				break;
			case T__7:
				_localctx = new Domain_item_methodContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(120);
				method_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Domain_name_defContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Domain_name_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domain_name_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitDomain_name_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Domain_name_defContext domain_name_def() throws RecognitionException {
		Domain_name_defContext _localctx = new Domain_name_defContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_domain_name_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(T__0);
			setState(124);
			match(NAME);
			setState(125);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Maxargs_defContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Maxargs_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maxargs_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitMaxargs_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Maxargs_defContext maxargs_def() throws RecognitionException {
		Maxargs_defContext _localctx = new Maxargs_defContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_maxargs_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(T__2);
			setState(128);
			match(NUMBER);
			setState(129);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Maxintargs_defContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Maxintargs_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maxintargs_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitMaxintargs_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Maxintargs_defContext maxintargs_def() throws RecognitionException {
		Maxintargs_defContext _localctx = new Maxintargs_defContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_maxintargs_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(T__3);
			setState(132);
			match(NUMBER);
			setState(133);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Predicatesymbols_defContext extends ParserRuleContext {
		public List<Predicate_symbolContext> predicate_symbol() {
			return getRuleContexts(Predicate_symbolContext.class);
		}
		public Predicate_symbolContext predicate_symbol(int i) {
			return getRuleContext(Predicate_symbolContext.class,i);
		}
		public Predicatesymbols_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicatesymbols_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPredicatesymbols_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicatesymbols_defContext predicatesymbols_def() throws RecognitionException {
		Predicatesymbols_defContext _localctx = new Predicatesymbols_defContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_predicatesymbols_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(T__4);
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NAME) {
				{
				{
				setState(136);
				predicate_symbol();
				}
				}
				setState(141);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(142);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Predicate_symbolContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Predicate_symbolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_symbol; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPredicate_symbol(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_symbolContext predicate_symbol() throws RecognitionException {
		Predicate_symbolContext _localctx = new Predicate_symbolContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_predicate_symbol);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Resource_defContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Resource_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitResource_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Resource_defContext resource_def() throws RecognitionException {
		Resource_defContext _localctx = new Resource_defContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_resource_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(T__5);
			setState(147);
			match(NAME);
			setState(148);
			match(NUMBER);
			setState(149);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Statevariable_defContext extends ParserRuleContext {
		public Statevariable_nameContext statevariable_name() {
			return getRuleContext(Statevariable_nameContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public List<TerminalNode> NAME() { return getTokens(ChimpClassicParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(ChimpClassicParser.NAME, i);
		}
		public Statevariable_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statevariable_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitStatevariable_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Statevariable_defContext statevariable_def() throws RecognitionException {
		Statevariable_defContext _localctx = new Statevariable_defContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_statevariable_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(T__6);
			setState(152);
			statevariable_name();
			setState(153);
			match(NUMBER);
			setState(155); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(154);
				match(NAME);
				}
				}
				setState(157); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NAME );
			setState(159);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Statevariable_nameContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Statevariable_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statevariable_name; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitStatevariable_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Statevariable_nameContext statevariable_name() throws RecognitionException {
		Statevariable_nameContext _localctx = new Statevariable_nameContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_statevariable_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(161);
			match(NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_defContext extends ParserRuleContext {
		public HeadContext head() {
			return getRuleContext(HeadContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public List<Method_elementContext> method_element() {
			return getRuleContexts(Method_elementContext.class);
		}
		public Method_elementContext method_element(int i) {
			return getRuleContext(Method_elementContext.class,i);
		}
		public Method_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitMethod_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_defContext method_def() throws RecognitionException {
		Method_defContext _localctx = new Method_defContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_method_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(T__7);
			setState(165);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER) {
				{
				setState(164);
				match(NUMBER);
				}
			}

			setState(167);
			head();
			setState(171);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 12)) & ~0x3f) == 0 && ((1L << (_la - 12)) & ((1L << (T__11 - 12)) | (1L << (T__12 - 12)) | (1L << (T__15 - 12)) | (1L << (T__16 - 12)) | (1L << (T__27 - 12)) | (1L << (T__28 - 12)) | (1L << (T__29 - 12)) | (1L << (T__30 - 12)) | (1L << (T__31 - 12)) | (1L << (T__32 - 12)) | (1L << (T__65 - 12)) | (1L << (T__70 - 12)) | (1L << (T__71 - 12)) | (1L << (T__72 - 12)) | (1L << (T__73 - 12)) | (1L << (T__74 - 12)))) != 0)) {
				{
				{
				setState(168);
				method_element();
				}
				}
				setState(173);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(174);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Operator_defContext extends ParserRuleContext {
		public HeadContext head() {
			return getRuleContext(HeadContext.class,0);
		}
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public List<Op_elementContext> op_element() {
			return getRuleContexts(Op_elementContext.class);
		}
		public Op_elementContext op_element(int i) {
			return getRuleContext(Op_elementContext.class,i);
		}
		public Operator_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_operator_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitOperator_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Operator_defContext operator_def() throws RecognitionException {
		Operator_defContext _localctx = new Operator_defContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_operator_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			match(T__8);
			setState(178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NUMBER) {
				{
				setState(177);
				match(NUMBER);
				}
			}

			setState(180);
			head();
			setState(184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 12)) & ~0x3f) == 0 && ((1L << (_la - 12)) & ((1L << (T__11 - 12)) | (1L << (T__13 - 12)) | (1L << (T__14 - 12)) | (1L << (T__16 - 12)) | (1L << (T__27 - 12)) | (1L << (T__28 - 12)) | (1L << (T__29 - 12)) | (1L << (T__30 - 12)) | (1L << (T__31 - 12)) | (1L << (T__32 - 12)) | (1L << (T__65 - 12)) | (1L << (T__70 - 12)) | (1L << (T__71 - 12)) | (1L << (T__72 - 12)) | (1L << (T__73 - 12)) | (1L << (T__74 - 12)))) != 0)) {
				{
				{
				setState(181);
				op_element();
				}
				}
				setState(186);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(187);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class HeadContext extends ParserRuleContext {
		public Predicate_symbolContext predicate_symbol() {
			return getRuleContext(Predicate_symbolContext.class,0);
		}
		public Predicate_argsContext predicate_args() {
			return getRuleContext(Predicate_argsContext.class,0);
		}
		public Int_args_defContext int_args_def() {
			return getRuleContext(Int_args_defContext.class,0);
		}
		public HeadContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_head; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitHead(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HeadContext head() throws RecognitionException {
		HeadContext _localctx = new HeadContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_head);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(T__9);
			setState(190);
			predicate_symbol();
			setState(191);
			match(T__10);
			setState(192);
			predicate_args();
			setState(193);
			match(T__1);
			setState(195);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(194);
				int_args_def();
				}
			}

			setState(197);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Int_args_defContext extends ParserRuleContext {
		public Int_argsContext int_args() {
			return getRuleContext(Int_argsContext.class,0);
		}
		public Int_args_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_int_args_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInt_args_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Int_args_defContext int_args_def() throws RecognitionException {
		Int_args_defContext _localctx = new Int_args_defContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_int_args_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(199);
			match(T__10);
			setState(200);
			int_args();
			setState(201);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Method_elementContext extends ParserRuleContext {
		public Method_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_method_element; }
	 
		public Method_elementContext() { }
		public void copyFrom(Method_elementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Precondition_m_elementContext extends Method_elementContext {
		public Precondition_defContext precondition_def() {
			return getRuleContext(Precondition_defContext.class,0);
		}
		public Precondition_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPrecondition_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Nottypevalue_restriction_m_elementContext extends Method_elementContext {
		public Nottypevalue_restriction_defContext nottypevalue_restriction_def() {
			return getRuleContext(Nottypevalue_restriction_defContext.class,0);
		}
		public Nottypevalue_restriction_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNottypevalue_restriction_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Temporal_constraint_m_elementContext extends Method_elementContext {
		public Temporal_constraint_defContext temporal_constraint_def() {
			return getRuleContext(Temporal_constraint_defContext.class,0);
		}
		public Temporal_constraint_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTemporal_constraint_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Resource_usage_m_elementContext extends Method_elementContext {
		public Resource_usage_defContext resource_usage_def() {
			return getRuleContext(Resource_usage_defContext.class,0);
		}
		public Resource_usage_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitResource_usage_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Vardifferent_m_elementContext extends Method_elementContext {
		public Vardifferent_defContext vardifferent_def() {
			return getRuleContext(Vardifferent_defContext.class,0);
		}
		public Vardifferent_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitVardifferent_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Value_restriction_m_elementContext extends Method_elementContext {
		public Value_restriction_defContext value_restriction_def() {
			return getRuleContext(Value_restriction_defContext.class,0);
		}
		public Value_restriction_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitValue_restriction_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Subtask_def_m_elementContext extends Method_elementContext {
		public Subtask_defContext subtask_def() {
			return getRuleContext(Subtask_defContext.class,0);
		}
		public Subtask_def_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitSubtask_def_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Integer_constraint_m_elementContext extends Method_elementContext {
		public Integer_constraint_defContext integer_constraint_def() {
			return getRuleContext(Integer_constraint_defContext.class,0);
		}
		public Integer_constraint_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_constraint_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Notvalue_restriction_m_elementContext extends Method_elementContext {
		public Notvalue_restriction_defContext notvalue_restriction_def() {
			return getRuleContext(Notvalue_restriction_defContext.class,0);
		}
		public Notvalue_restriction_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNotvalue_restriction_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Typevalue_restriction_m_elementContext extends Method_elementContext {
		public Typevalue_restriction_defContext typevalue_restriction_def() {
			return getRuleContext(Typevalue_restriction_defContext.class,0);
		}
		public Typevalue_restriction_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTypevalue_restriction_m_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Ordering_def_m_elementContext extends Method_elementContext {
		public Ordering_constraint_defContext ordering_constraint_def() {
			return getRuleContext(Ordering_constraint_defContext.class,0);
		}
		public Ordering_def_m_elementContext(Method_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitOrdering_def_m_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Method_elementContext method_element() throws RecognitionException {
		Method_elementContext _localctx = new Method_elementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_method_element);
		try {
			setState(214);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__11:
				_localctx = new Precondition_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				precondition_def();
				}
				break;
			case T__27:
			case T__28:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
				_localctx = new Temporal_constraint_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(204);
				temporal_constraint_def();
				}
				break;
			case T__65:
				_localctx = new Resource_usage_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(205);
				resource_usage_def();
				}
				break;
			case T__70:
				_localctx = new Value_restriction_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(206);
				value_restriction_def();
				}
				break;
			case T__71:
				_localctx = new Notvalue_restriction_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(207);
				notvalue_restriction_def();
				}
				break;
			case T__72:
				_localctx = new Typevalue_restriction_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(208);
				typevalue_restriction_def();
				}
				break;
			case T__73:
				_localctx = new Nottypevalue_restriction_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(209);
				nottypevalue_restriction_def();
				}
				break;
			case T__74:
				_localctx = new Vardifferent_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(210);
				vardifferent_def();
				}
				break;
			case T__12:
				_localctx = new Subtask_def_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(211);
				subtask_def();
				}
				break;
			case T__15:
				_localctx = new Ordering_def_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(212);
				ordering_constraint_def();
				}
				break;
			case T__16:
				_localctx = new Integer_constraint_m_elementContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(213);
				integer_constraint_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Op_elementContext extends ParserRuleContext {
		public Op_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_op_element; }
	 
		public Op_elementContext() { }
		public void copyFrom(Op_elementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class Negative_effect_def_op_elementContext extends Op_elementContext {
		public Negative_effect_defContext negative_effect_def() {
			return getRuleContext(Negative_effect_defContext.class,0);
		}
		public Negative_effect_def_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNegative_effect_def_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Temporal_constraint_op_elementContext extends Op_elementContext {
		public Temporal_constraint_defContext temporal_constraint_def() {
			return getRuleContext(Temporal_constraint_defContext.class,0);
		}
		public Temporal_constraint_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTemporal_constraint_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Positive_effect_def_op_elementContext extends Op_elementContext {
		public Positive_effect_defContext positive_effect_def() {
			return getRuleContext(Positive_effect_defContext.class,0);
		}
		public Positive_effect_def_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPositive_effect_def_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Resource_usage_op_elementContext extends Op_elementContext {
		public Resource_usage_defContext resource_usage_def() {
			return getRuleContext(Resource_usage_defContext.class,0);
		}
		public Resource_usage_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitResource_usage_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Notvalue_restriction_op_elementContext extends Op_elementContext {
		public Notvalue_restriction_defContext notvalue_restriction_def() {
			return getRuleContext(Notvalue_restriction_defContext.class,0);
		}
		public Notvalue_restriction_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNotvalue_restriction_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Precondition_op_elementContext extends Op_elementContext {
		public Precondition_defContext precondition_def() {
			return getRuleContext(Precondition_defContext.class,0);
		}
		public Precondition_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPrecondition_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Value_restriction_op_elementContext extends Op_elementContext {
		public Value_restriction_defContext value_restriction_def() {
			return getRuleContext(Value_restriction_defContext.class,0);
		}
		public Value_restriction_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitValue_restriction_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Nottypevalue_restriction_op_elementContext extends Op_elementContext {
		public Nottypevalue_restriction_defContext nottypevalue_restriction_def() {
			return getRuleContext(Nottypevalue_restriction_defContext.class,0);
		}
		public Nottypevalue_restriction_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNottypevalue_restriction_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Typevalue_restriction_op_elementContext extends Op_elementContext {
		public Typevalue_restriction_defContext typevalue_restriction_def() {
			return getRuleContext(Typevalue_restriction_defContext.class,0);
		}
		public Typevalue_restriction_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTypevalue_restriction_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Vardifferent_op_elementContext extends Op_elementContext {
		public Vardifferent_defContext vardifferent_def() {
			return getRuleContext(Vardifferent_defContext.class,0);
		}
		public Vardifferent_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitVardifferent_op_element(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Integer_constraint_op_elementContext extends Op_elementContext {
		public Integer_constraint_defContext integer_constraint_def() {
			return getRuleContext(Integer_constraint_defContext.class,0);
		}
		public Integer_constraint_op_elementContext(Op_elementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_constraint_op_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Op_elementContext op_element() throws RecognitionException {
		Op_elementContext _localctx = new Op_elementContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_op_element);
		try {
			setState(227);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__11:
				_localctx = new Precondition_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(216);
				precondition_def();
				}
				break;
			case T__27:
			case T__28:
			case T__29:
			case T__30:
			case T__31:
			case T__32:
				_localctx = new Temporal_constraint_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(217);
				temporal_constraint_def();
				}
				break;
			case T__13:
				_localctx = new Positive_effect_def_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(218);
				positive_effect_def();
				}
				break;
			case T__14:
				_localctx = new Negative_effect_def_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(219);
				negative_effect_def();
				}
				break;
			case T__65:
				_localctx = new Resource_usage_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(220);
				resource_usage_def();
				}
				break;
			case T__70:
				_localctx = new Value_restriction_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(221);
				value_restriction_def();
				}
				break;
			case T__71:
				_localctx = new Notvalue_restriction_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(222);
				notvalue_restriction_def();
				}
				break;
			case T__72:
				_localctx = new Typevalue_restriction_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(223);
				typevalue_restriction_def();
				}
				break;
			case T__73:
				_localctx = new Nottypevalue_restriction_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(224);
				nottypevalue_restriction_def();
				}
				break;
			case T__74:
				_localctx = new Vardifferent_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(225);
				vardifferent_def();
				}
				break;
			case T__16:
				_localctx = new Integer_constraint_op_elementContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(226);
				integer_constraint_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Precondition_defContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public Precondition_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_precondition_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPrecondition_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Precondition_defContext precondition_def() throws RecognitionException {
		Precondition_defContext _localctx = new Precondition_defContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_precondition_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(T__11);
			setState(230);
			id();
			setState(231);
			predicate();
			setState(232);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Subtask_defContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public Subtask_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subtask_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitSubtask_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Subtask_defContext subtask_def() throws RecognitionException {
		Subtask_defContext _localctx = new Subtask_defContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_subtask_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(234);
			match(T__12);
			setState(235);
			id();
			setState(236);
			predicate();
			setState(237);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Positive_effect_defContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public Positive_effect_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positive_effect_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPositive_effect_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Positive_effect_defContext positive_effect_def() throws RecognitionException {
		Positive_effect_defContext _localctx = new Positive_effect_defContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_positive_effect_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(T__13);
			setState(240);
			id();
			setState(241);
			predicate();
			setState(242);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Negative_effect_defContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Negative_effect_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negative_effect_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNegative_effect_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Negative_effect_defContext negative_effect_def() throws RecognitionException {
		Negative_effect_defContext _localctx = new Negative_effect_defContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_negative_effect_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(T__14);
			setState(245);
			id();
			setState(246);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ordering_constraint_defContext extends ParserRuleContext {
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public Ordering_constraint_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ordering_constraint_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitOrdering_constraint_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ordering_constraint_defContext ordering_constraint_def() throws RecognitionException {
		Ordering_constraint_defContext _localctx = new Ordering_constraint_defContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_ordering_constraint_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			match(T__15);
			setState(249);
			id();
			setState(250);
			id();
			setState(251);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_constraint_defContext extends ParserRuleContext {
		public Integer_constraint1_defContext integer_constraint1_def() {
			return getRuleContext(Integer_constraint1_defContext.class,0);
		}
		public Integer_constraint2_defContext integer_constraint2_def() {
			return getRuleContext(Integer_constraint2_defContext.class,0);
		}
		public Integer_constraint_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constraint_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_constraint_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_constraint_defContext integer_constraint_def() throws RecognitionException {
		Integer_constraint_defContext _localctx = new Integer_constraint_defContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_integer_constraint_def);
		try {
			setState(255);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(253);
				integer_constraint1_def();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(254);
				integer_constraint2_def();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_constraint1_defContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Integer_operator1Context integer_operator1() {
			return getRuleContext(Integer_operator1Context.class,0);
		}
		public Var_or_intContext var_or_int() {
			return getRuleContext(Var_or_intContext.class,0);
		}
		public Integer_constraint1_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constraint1_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_constraint1_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_constraint1_defContext integer_constraint1_def() throws RecognitionException {
		Integer_constraint1_defContext _localctx = new Integer_constraint1_defContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_integer_constraint1_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			match(T__16);
			setState(258);
			match(VAR_NAME);
			setState(259);
			integer_operator1();
			setState(260);
			var_or_int();
			setState(261);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_constraint2_defContext extends ParserRuleContext {
		public List<TerminalNode> VAR_NAME() { return getTokens(ChimpClassicParser.VAR_NAME); }
		public TerminalNode VAR_NAME(int i) {
			return getToken(ChimpClassicParser.VAR_NAME, i);
		}
		public Integer_operator1Context integer_operator1() {
			return getRuleContext(Integer_operator1Context.class,0);
		}
		public Integer_operator2Context integer_operator2() {
			return getRuleContext(Integer_operator2Context.class,0);
		}
		public Var_or_intContext var_or_int() {
			return getRuleContext(Var_or_intContext.class,0);
		}
		public Integer_constraint2_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constraint2_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_constraint2_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_constraint2_defContext integer_constraint2_def() throws RecognitionException {
		Integer_constraint2_defContext _localctx = new Integer_constraint2_defContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_integer_constraint2_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263);
			match(T__16);
			setState(264);
			match(VAR_NAME);
			setState(265);
			integer_operator1();
			setState(266);
			match(VAR_NAME);
			setState(267);
			integer_operator2();
			setState(268);
			var_or_int();
			setState(269);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_operator1Context extends ParserRuleContext {
		public Integer_operator1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_operator1; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_operator1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_operator1Context integer_operator1() throws RecognitionException {
		Integer_operator1Context _localctx = new Integer_operator1Context(_ctx, getState());
		enterRule(_localctx, 48, RULE_integer_operator1);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_operator2Context extends ParserRuleContext {
		public Integer_operator2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_operator2; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInteger_operator2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_operator2Context integer_operator2() throws RecognitionException {
		Integer_operator2Context _localctx = new Integer_operator2Context(_ctx, getState());
		enterRule(_localctx, 50, RULE_integer_operator2);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << T__25) | (1L << T__26))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Temporal_constraint_defContext extends ParserRuleContext {
		public Unary_temporal_constraint_defContext unary_temporal_constraint_def() {
			return getRuleContext(Unary_temporal_constraint_defContext.class,0);
		}
		public Binary_temporal_constraint_defContext binary_temporal_constraint_def() {
			return getRuleContext(Binary_temporal_constraint_defContext.class,0);
		}
		public Temporal_constraint_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_temporal_constraint_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTemporal_constraint_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Temporal_constraint_defContext temporal_constraint_def() throws RecognitionException {
		Temporal_constraint_defContext _localctx = new Temporal_constraint_defContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_temporal_constraint_def);
		try {
			setState(277);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__27:
			case T__28:
			case T__29:
			case T__30:
			case T__31:
				enterOuterAlt(_localctx, 1);
				{
				setState(275);
				unary_temporal_constraint_def();
				}
				break;
			case T__32:
				enterOuterAlt(_localctx, 2);
				{
				setState(276);
				binary_temporal_constraint_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Unary_temporal_constraint_defContext extends ParserRuleContext {
		public Unary_temporal_constraint_typeContext unary_temporal_constraint_type() {
			return getRuleContext(Unary_temporal_constraint_typeContext.class,0);
		}
		public Bounds_listContext bounds_list() {
			return getRuleContext(Bounds_listContext.class,0);
		}
		public Id_or_taskContext id_or_task() {
			return getRuleContext(Id_or_taskContext.class,0);
		}
		public Unary_temporal_constraint_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary_temporal_constraint_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitUnary_temporal_constraint_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unary_temporal_constraint_defContext unary_temporal_constraint_def() throws RecognitionException {
		Unary_temporal_constraint_defContext _localctx = new Unary_temporal_constraint_defContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_unary_temporal_constraint_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(279);
			unary_temporal_constraint_type();
			setState(280);
			bounds_list();
			setState(281);
			match(T__10);
			setState(282);
			id_or_task();
			setState(283);
			match(T__1);
			setState(284);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Unary_temporal_constraint_typeContext extends ParserRuleContext {
		public Unary_temporal_constraint_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary_temporal_constraint_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitUnary_temporal_constraint_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unary_temporal_constraint_typeContext unary_temporal_constraint_type() throws RecognitionException {
		Unary_temporal_constraint_typeContext _localctx = new Unary_temporal_constraint_typeContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_unary_temporal_constraint_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__27) | (1L << T__28) | (1L << T__29) | (1L << T__30) | (1L << T__31))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Binary_temporal_constraint_defContext extends ParserRuleContext {
		public Binary_temporal_constraint_typeContext binary_temporal_constraint_type() {
			return getRuleContext(Binary_temporal_constraint_typeContext.class,0);
		}
		public Bounds_listContext bounds_list() {
			return getRuleContext(Bounds_listContext.class,0);
		}
		public List<Id_or_taskContext> id_or_task() {
			return getRuleContexts(Id_or_taskContext.class);
		}
		public Id_or_taskContext id_or_task(int i) {
			return getRuleContext(Id_or_taskContext.class,i);
		}
		public Binary_temporal_constraint_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binary_temporal_constraint_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitBinary_temporal_constraint_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Binary_temporal_constraint_defContext binary_temporal_constraint_def() throws RecognitionException {
		Binary_temporal_constraint_defContext _localctx = new Binary_temporal_constraint_defContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_binary_temporal_constraint_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(288);
			match(T__32);
			setState(289);
			binary_temporal_constraint_type();
			setState(290);
			bounds_list();
			setState(291);
			match(T__10);
			setState(292);
			id_or_task();
			setState(293);
			match(T__33);
			setState(294);
			id_or_task();
			setState(295);
			match(T__1);
			setState(296);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Binary_temporal_constraint_typeContext extends ParserRuleContext {
		public Binary_temporal_constraint_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binary_temporal_constraint_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitBinary_temporal_constraint_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Binary_temporal_constraint_typeContext binary_temporal_constraint_type() throws RecognitionException {
		Binary_temporal_constraint_typeContext _localctx = new Binary_temporal_constraint_typeContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_binary_temporal_constraint_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__34) | (1L << T__35) | (1L << T__36) | (1L << T__37) | (1L << T__38) | (1L << T__39) | (1L << T__40) | (1L << T__41) | (1L << T__42) | (1L << T__43) | (1L << T__44) | (1L << T__45) | (1L << T__46) | (1L << T__47) | (1L << T__48) | (1L << T__49) | (1L << T__50) | (1L << T__51) | (1L << T__52) | (1L << T__53) | (1L << T__54) | (1L << T__55) | (1L << T__56) | (1L << T__57) | (1L << T__58) | (1L << T__59) | (1L << T__60))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bounds_listContext extends ParserRuleContext {
		public List<BoundsContext> bounds() {
			return getRuleContexts(BoundsContext.class);
		}
		public BoundsContext bounds(int i) {
			return getRuleContext(BoundsContext.class,i);
		}
		public Bounds_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bounds_list; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitBounds_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Bounds_listContext bounds_list() throws RecognitionException {
		Bounds_listContext _localctx = new Bounds_listContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_bounds_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__61) {
				{
				{
				setState(300);
				bounds();
				}
				}
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoundsContext extends ParserRuleContext {
		public List<BoundContext> bound() {
			return getRuleContexts(BoundContext.class);
		}
		public BoundContext bound(int i) {
			return getRuleContext(BoundContext.class,i);
		}
		public BoundsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bounds; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitBounds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoundsContext bounds() throws RecognitionException {
		BoundsContext _localctx = new BoundsContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_bounds);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			match(T__61);
			setState(307);
			bound();
			setState(308);
			match(T__33);
			setState(309);
			bound();
			setState(310);
			match(T__62);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoundContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public BoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bound; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoundContext bound() throws RecognitionException {
		BoundContext _localctx = new BoundContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_bound);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			_la = _input.LA(1);
			if ( !(_la==T__63 || _la==NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Id_or_taskContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Id_or_taskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_or_task; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitId_or_task(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Id_or_taskContext id_or_task() throws RecognitionException {
		Id_or_taskContext _localctx = new Id_or_taskContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_id_or_task);
		try {
			setState(316);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(314);
				id();
				}
				break;
			case T__64:
				enterOuterAlt(_localctx, 2);
				{
				setState(315);
				match(T__64);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Resource_usage_defContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Usage_defContext usage_def() {
			return getRuleContext(Usage_defContext.class,0);
		}
		public List<Param_itemContext> param_item() {
			return getRuleContexts(Param_itemContext.class);
		}
		public Param_itemContext param_item(int i) {
			return getRuleContext(Param_itemContext.class,i);
		}
		public Resource_usage_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource_usage_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitResource_usage_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Resource_usage_defContext resource_usage_def() throws RecognitionException {
		Resource_usage_defContext _localctx = new Resource_usage_defContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_resource_usage_def);
		int _la;
		try {
			setState(332);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(318);
				match(T__65);
				setState(319);
				match(NAME);
				setState(320);
				match(NUMBER);
				setState(321);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(322);
				match(T__65);
				setState(323);
				usage_def();
				setState(327);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__69) {
					{
					{
					setState(324);
					param_item();
					}
					}
					setState(329);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(330);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fluentresourceusage_defContext extends ParserRuleContext {
		public Usage_defContext usage_def() {
			return getRuleContext(Usage_defContext.class,0);
		}
		public Fluent_defContext fluent_def() {
			return getRuleContext(Fluent_defContext.class,0);
		}
		public List<Param_itemContext> param_item() {
			return getRuleContexts(Param_itemContext.class);
		}
		public Param_itemContext param_item(int i) {
			return getRuleContext(Param_itemContext.class,i);
		}
		public Fluentresourceusage_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fluentresourceusage_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitFluentresourceusage_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Fluentresourceusage_defContext fluentresourceusage_def() throws RecognitionException {
		Fluentresourceusage_defContext _localctx = new Fluentresourceusage_defContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_fluentresourceusage_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			match(T__66);
			setState(335);
			usage_def();
			setState(336);
			fluent_def();
			setState(340);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__69) {
				{
				{
				setState(337);
				param_item();
				}
				}
				setState(342);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(343);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fluent_defContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Fluent_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fluent_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitFluent_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Fluent_defContext fluent_def() throws RecognitionException {
		Fluent_defContext _localctx = new Fluent_defContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_fluent_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(345);
			match(T__67);
			setState(346);
			match(NAME);
			setState(347);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Usage_defContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Usage_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_usage_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitUsage_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Usage_defContext usage_def() throws RecognitionException {
		Usage_defContext _localctx = new Usage_defContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_usage_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			match(T__68);
			setState(350);
			match(NAME);
			setState(351);
			match(NUMBER);
			setState(352);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Param_itemContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Param_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_item; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitParam_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Param_itemContext param_item() throws RecognitionException {
		Param_itemContext _localctx = new Param_itemContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_param_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			match(T__69);
			setState(355);
			match(NUMBER);
			setState(356);
			match(NAME);
			setState(357);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Value_restriction_defContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Constant_listContext constant_list() {
			return getRuleContext(Constant_listContext.class,0);
		}
		public Value_restriction_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_restriction_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitValue_restriction_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Value_restriction_defContext value_restriction_def() throws RecognitionException {
		Value_restriction_defContext _localctx = new Value_restriction_defContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_value_restriction_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			match(T__70);
			setState(360);
			match(VAR_NAME);
			setState(361);
			constant_list();
			setState(362);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Notvalue_restriction_defContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Constant_listContext constant_list() {
			return getRuleContext(Constant_listContext.class,0);
		}
		public Notvalue_restriction_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notvalue_restriction_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNotvalue_restriction_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Notvalue_restriction_defContext notvalue_restriction_def() throws RecognitionException {
		Notvalue_restriction_defContext _localctx = new Notvalue_restriction_defContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_notvalue_restriction_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(364);
			match(T__71);
			setState(365);
			match(VAR_NAME);
			setState(366);
			constant_list();
			setState(367);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Typevalue_restriction_defContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Constant_listContext constant_list() {
			return getRuleContext(Constant_listContext.class,0);
		}
		public Typevalue_restriction_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typevalue_restriction_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitTypevalue_restriction_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Typevalue_restriction_defContext typevalue_restriction_def() throws RecognitionException {
		Typevalue_restriction_defContext _localctx = new Typevalue_restriction_defContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_typevalue_restriction_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			match(T__72);
			setState(370);
			match(VAR_NAME);
			setState(371);
			constant_list();
			setState(372);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Nottypevalue_restriction_defContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Constant_listContext constant_list() {
			return getRuleContext(Constant_listContext.class,0);
		}
		public Nottypevalue_restriction_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nottypevalue_restriction_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitNottypevalue_restriction_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Nottypevalue_restriction_defContext nottypevalue_restriction_def() throws RecognitionException {
		Nottypevalue_restriction_defContext _localctx = new Nottypevalue_restriction_defContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_nottypevalue_restriction_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			match(T__73);
			setState(375);
			match(VAR_NAME);
			setState(376);
			constant_list();
			setState(377);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vardifferent_defContext extends ParserRuleContext {
		public List<TerminalNode> VAR_NAME() { return getTokens(ChimpClassicParser.VAR_NAME); }
		public TerminalNode VAR_NAME(int i) {
			return getToken(ChimpClassicParser.VAR_NAME, i);
		}
		public Vardifferent_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vardifferent_def; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitVardifferent_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Vardifferent_defContext vardifferent_def() throws RecognitionException {
		Vardifferent_defContext _localctx = new Vardifferent_defContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_vardifferent_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			match(T__74);
			setState(380);
			match(VAR_NAME);
			setState(381);
			match(VAR_NAME);
			setState(382);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Constant_listContext extends ParserRuleContext {
		public List<TerminalNode> NAME() { return getTokens(ChimpClassicParser.NAME); }
		public TerminalNode NAME(int i) {
			return getToken(ChimpClassicParser.NAME, i);
		}
		public Constant_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_list; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitConstant_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constant_listContext constant_list() throws RecognitionException {
		Constant_listContext _localctx = new Constant_listContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_constant_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(384);
				match(NAME);
				}
				}
				setState(387); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NAME );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public Predicate_argsContext predicate_args() {
			return getRuleContext(Predicate_argsContext.class,0);
		}
		public Int_args_defContext int_args_def() {
			return getRuleContext(Int_args_defContext.class,0);
		}
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_predicate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			match(NAME);
			setState(390);
			match(T__10);
			setState(391);
			predicate_args();
			setState(392);
			match(T__1);
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(393);
				int_args_def();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Predicate_argsContext extends ParserRuleContext {
		public List<Var_or_constContext> var_or_const() {
			return getRuleContexts(Var_or_constContext.class);
		}
		public Var_or_constContext var_or_const(int i) {
			return getRuleContext(Var_or_constContext.class,i);
		}
		public Predicate_argsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate_args; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitPredicate_args(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Predicate_argsContext predicate_args() throws RecognitionException {
		Predicate_argsContext _localctx = new Predicate_argsContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_predicate_args);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==VAR_NAME || _la==NAME) {
				{
				{
				setState(396);
				var_or_const();
				}
				}
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Int_argsContext extends ParserRuleContext {
		public List<Var_or_intContext> var_or_int() {
			return getRuleContexts(Var_or_intContext.class);
		}
		public Var_or_intContext var_or_int(int i) {
			return getRuleContext(Var_or_intContext.class,i);
		}
		public Int_argsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_int_args; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitInt_args(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Int_argsContext int_args() throws RecognitionException {
		Int_argsContext _localctx = new Int_argsContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_int_args);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==VAR_NAME || _la==NUMBER) {
				{
				{
				setState(402);
				var_or_int();
				}
				}
				setState(407);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_or_intContext extends ParserRuleContext {
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public TerminalNode NUMBER() { return getToken(ChimpClassicParser.NUMBER, 0); }
		public Var_or_intContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_or_int; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitVar_or_int(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_or_intContext var_or_int() throws RecognitionException {
		Var_or_intContext _localctx = new Var_or_intContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_var_or_int);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(408);
			_la = _input.LA(1);
			if ( !(_la==VAR_NAME || _la==NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			match(NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_or_constContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(ChimpClassicParser.NAME, 0); }
		public TerminalNode VAR_NAME() { return getToken(ChimpClassicParser.VAR_NAME, 0); }
		public Var_or_constContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_or_const; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChimpClassicVisitor ) return ((ChimpClassicVisitor<? extends T>)visitor).visitVar_or_const(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Var_or_constContext var_or_const() throws RecognitionException {
		Var_or_constContext _localctx = new Var_or_constContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_var_or_const);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412);
			_la = _input.LA(1);
			if ( !(_la==VAR_NAME || _la==NAME) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3R\u01a1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\3\2\3\2\3\2\5\2n\n\2\3\2\3\2\7\2r\n\2\f\2\16\2u\13\2\3\3"+
		"\3\3\3\3\3\3\3\3\5\3|\n\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6"+
		"\3\6\3\7\3\7\7\7\u008c\n\7\f\7\16\7\u008f\13\7\3\7\3\7\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\3\t\3\n\3\n\3\n\3\n\6\n\u009e\n\n\r\n\16\n\u009f\3\n\3\n\3\13"+
		"\3\13\3\f\3\f\5\f\u00a8\n\f\3\f\3\f\7\f\u00ac\n\f\f\f\16\f\u00af\13\f"+
		"\3\f\3\f\3\r\3\r\5\r\u00b5\n\r\3\r\3\r\7\r\u00b9\n\r\f\r\16\r\u00bc\13"+
		"\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00c6\n\16\3\16\3\16\3\17"+
		"\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\5\20\u00d9\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\5\21\u00e6\n\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\24"+
		"\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\27"+
		"\3\27\5\27\u0102\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\5\34\u0118\n\34\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\3 \3 \3!\7!\u0130\n!\f!\16!\u0133\13!\3\"\3\"\3\""+
		"\3\"\3\"\3\"\3#\3#\3$\3$\5$\u013f\n$\3%\3%\3%\3%\3%\3%\3%\7%\u0148\n%"+
		"\f%\16%\u014b\13%\3%\3%\5%\u014f\n%\3&\3&\3&\3&\7&\u0155\n&\f&\16&\u0158"+
		"\13&\3&\3&\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3*\3*\3*\3*\3"+
		"*\3+\3+\3+\3+\3+\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3.\3.\3.\3.\3.\3/\6/\u0184"+
		"\n/\r/\16/\u0185\3\60\3\60\3\60\3\60\3\60\5\60\u018d\n\60\3\61\7\61\u0190"+
		"\n\61\f\61\16\61\u0193\13\61\3\62\7\62\u0196\n\62\f\62\16\62\u0199\13"+
		"\62\3\63\3\63\3\64\3\64\3\65\3\65\3\65\2\2\66\2\4\6\b\n\f\16\20\22\24"+
		"\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfh\2\t\3\2"+
		"\24\31\3\2\24\35\3\2\36\"\3\2%?\4\2BBRR\4\2NNRR\3\2NO\2\u0198\2j\3\2\2"+
		"\2\4{\3\2\2\2\6}\3\2\2\2\b\u0081\3\2\2\2\n\u0085\3\2\2\2\f\u0089\3\2\2"+
		"\2\16\u0092\3\2\2\2\20\u0094\3\2\2\2\22\u0099\3\2\2\2\24\u00a3\3\2\2\2"+
		"\26\u00a5\3\2\2\2\30\u00b2\3\2\2\2\32\u00bf\3\2\2\2\34\u00c9\3\2\2\2\36"+
		"\u00d8\3\2\2\2 \u00e5\3\2\2\2\"\u00e7\3\2\2\2$\u00ec\3\2\2\2&\u00f1\3"+
		"\2\2\2(\u00f6\3\2\2\2*\u00fa\3\2\2\2,\u0101\3\2\2\2.\u0103\3\2\2\2\60"+
		"\u0109\3\2\2\2\62\u0111\3\2\2\2\64\u0113\3\2\2\2\66\u0117\3\2\2\28\u0119"+
		"\3\2\2\2:\u0120\3\2\2\2<\u0122\3\2\2\2>\u012c\3\2\2\2@\u0131\3\2\2\2B"+
		"\u0134\3\2\2\2D\u013a\3\2\2\2F\u013e\3\2\2\2H\u014e\3\2\2\2J\u0150\3\2"+
		"\2\2L\u015b\3\2\2\2N\u015f\3\2\2\2P\u0164\3\2\2\2R\u0169\3\2\2\2T\u016e"+
		"\3\2\2\2V\u0173\3\2\2\2X\u0178\3\2\2\2Z\u017d\3\2\2\2\\\u0183\3\2\2\2"+
		"^\u0187\3\2\2\2`\u0191\3\2\2\2b\u0197\3\2\2\2d\u019a\3\2\2\2f\u019c\3"+
		"\2\2\2h\u019e\3\2\2\2jk\5\6\4\2km\5\b\5\2ln\5\n\6\2ml\3\2\2\2mn\3\2\2"+
		"\2no\3\2\2\2os\5\f\7\2pr\5\4\3\2qp\3\2\2\2ru\3\2\2\2sq\3\2\2\2st\3\2\2"+
		"\2t\3\3\2\2\2us\3\2\2\2v|\5\20\t\2w|\5J&\2x|\5\22\n\2y|\5\30\r\2z|\5\26"+
		"\f\2{v\3\2\2\2{w\3\2\2\2{x\3\2\2\2{y\3\2\2\2{z\3\2\2\2|\5\3\2\2\2}~\7"+
		"\3\2\2~\177\7O\2\2\177\u0080\7\4\2\2\u0080\7\3\2\2\2\u0081\u0082\7\5\2"+
		"\2\u0082\u0083\7R\2\2\u0083\u0084\7\4\2\2\u0084\t\3\2\2\2\u0085\u0086"+
		"\7\6\2\2\u0086\u0087\7R\2\2\u0087\u0088\7\4\2\2\u0088\13\3\2\2\2\u0089"+
		"\u008d\7\7\2\2\u008a\u008c\5\16\b\2\u008b\u008a\3\2\2\2\u008c\u008f\3"+
		"\2\2\2\u008d\u008b\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u0090\3\2\2\2\u008f"+
		"\u008d\3\2\2\2\u0090\u0091\7\4\2\2\u0091\r\3\2\2\2\u0092\u0093\7O\2\2"+
		"\u0093\17\3\2\2\2\u0094\u0095\7\b\2\2\u0095\u0096\7O\2\2\u0096\u0097\7"+
		"R\2\2\u0097\u0098\7\4\2\2\u0098\21\3\2\2\2\u0099\u009a\7\t\2\2\u009a\u009b"+
		"\5\24\13\2\u009b\u009d\7R\2\2\u009c\u009e\7O\2\2\u009d\u009c\3\2\2\2\u009e"+
		"\u009f\3\2\2\2\u009f\u009d\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\3\2"+
		"\2\2\u00a1\u00a2\7\4\2\2\u00a2\23\3\2\2\2\u00a3\u00a4\7O\2\2\u00a4\25"+
		"\3\2\2\2\u00a5\u00a7\7\n\2\2\u00a6\u00a8\7R\2\2\u00a7\u00a6\3\2\2\2\u00a7"+
		"\u00a8\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ad\5\32\16\2\u00aa\u00ac\5"+
		"\36\20\2\u00ab\u00aa\3\2\2\2\u00ac\u00af\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad"+
		"\u00ae\3\2\2\2\u00ae\u00b0\3\2\2\2\u00af\u00ad\3\2\2\2\u00b0\u00b1\7\4"+
		"\2\2\u00b1\27\3\2\2\2\u00b2\u00b4\7\13\2\2\u00b3\u00b5\7R\2\2\u00b4\u00b3"+
		"\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00ba\5\32\16\2"+
		"\u00b7\u00b9\5 \21\2\u00b8\u00b7\3\2\2\2\u00b9\u00bc\3\2\2\2\u00ba\u00b8"+
		"\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00bd\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bd"+
		"\u00be\7\4\2\2\u00be\31\3\2\2\2\u00bf\u00c0\7\f\2\2\u00c0\u00c1\5\16\b"+
		"\2\u00c1\u00c2\7\r\2\2\u00c2\u00c3\5`\61\2\u00c3\u00c5\7\4\2\2\u00c4\u00c6"+
		"\5\34\17\2\u00c5\u00c4\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\u00c7\3\2\2\2"+
		"\u00c7\u00c8\7\4\2\2\u00c8\33\3\2\2\2\u00c9\u00ca\7\r\2\2\u00ca\u00cb"+
		"\5b\62\2\u00cb\u00cc\7\4\2\2\u00cc\35\3\2\2\2\u00cd\u00d9\5\"\22\2\u00ce"+
		"\u00d9\5\66\34\2\u00cf\u00d9\5H%\2\u00d0\u00d9\5R*\2\u00d1\u00d9\5T+\2"+
		"\u00d2\u00d9\5V,\2\u00d3\u00d9\5X-\2\u00d4\u00d9\5Z.\2\u00d5\u00d9\5$"+
		"\23\2\u00d6\u00d9\5*\26\2\u00d7\u00d9\5,\27\2\u00d8\u00cd\3\2\2\2\u00d8"+
		"\u00ce\3\2\2\2\u00d8\u00cf\3\2\2\2\u00d8\u00d0\3\2\2\2\u00d8\u00d1\3\2"+
		"\2\2\u00d8\u00d2\3\2\2\2\u00d8\u00d3\3\2\2\2\u00d8\u00d4\3\2\2\2\u00d8"+
		"\u00d5\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d8\u00d7\3\2\2\2\u00d9\37\3\2\2"+
		"\2\u00da\u00e6\5\"\22\2\u00db\u00e6\5\66\34\2\u00dc\u00e6\5&\24\2\u00dd"+
		"\u00e6\5(\25\2\u00de\u00e6\5H%\2\u00df\u00e6\5R*\2\u00e0\u00e6\5T+\2\u00e1"+
		"\u00e6\5V,\2\u00e2\u00e6\5X-\2\u00e3\u00e6\5Z.\2\u00e4\u00e6\5,\27\2\u00e5"+
		"\u00da\3\2\2\2\u00e5\u00db\3\2\2\2\u00e5\u00dc\3\2\2\2\u00e5\u00dd\3\2"+
		"\2\2\u00e5\u00de\3\2\2\2\u00e5\u00df\3\2\2\2\u00e5\u00e0\3\2\2\2\u00e5"+
		"\u00e1\3\2\2\2\u00e5\u00e2\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e4\3\2"+
		"\2\2\u00e6!\3\2\2\2\u00e7\u00e8\7\16\2\2\u00e8\u00e9\5f\64\2\u00e9\u00ea"+
		"\5^\60\2\u00ea\u00eb\7\4\2\2\u00eb#\3\2\2\2\u00ec\u00ed\7\17\2\2\u00ed"+
		"\u00ee\5f\64\2\u00ee\u00ef\5^\60\2\u00ef\u00f0\7\4\2\2\u00f0%\3\2\2\2"+
		"\u00f1\u00f2\7\20\2\2\u00f2\u00f3\5f\64\2\u00f3\u00f4\5^\60\2\u00f4\u00f5"+
		"\7\4\2\2\u00f5\'\3\2\2\2\u00f6\u00f7\7\21\2\2\u00f7\u00f8\5f\64\2\u00f8"+
		"\u00f9\7\4\2\2\u00f9)\3\2\2\2\u00fa\u00fb\7\22\2\2\u00fb\u00fc\5f\64\2"+
		"\u00fc\u00fd\5f\64\2\u00fd\u00fe\7\4\2\2\u00fe+\3\2\2\2\u00ff\u0102\5"+
		".\30\2\u0100\u0102\5\60\31\2\u0101\u00ff\3\2\2\2\u0101\u0100\3\2\2\2\u0102"+
		"-\3\2\2\2\u0103\u0104\7\23\2\2\u0104\u0105\7N\2\2\u0105\u0106\5\62\32"+
		"\2\u0106\u0107\5d\63\2\u0107\u0108\7\4\2\2\u0108/\3\2\2\2\u0109\u010a"+
		"\7\23\2\2\u010a\u010b\7N\2\2\u010b\u010c\5\62\32\2\u010c\u010d\7N\2\2"+
		"\u010d\u010e\5\64\33\2\u010e\u010f\5d\63\2\u010f\u0110\7\4\2\2\u0110\61"+
		"\3\2\2\2\u0111\u0112\t\2\2\2\u0112\63\3\2\2\2\u0113\u0114\t\3\2\2\u0114"+
		"\65\3\2\2\2\u0115\u0118\58\35\2\u0116\u0118\5<\37\2\u0117\u0115\3\2\2"+
		"\2\u0117\u0116\3\2\2\2\u0118\67\3\2\2\2\u0119\u011a\5:\36\2\u011a\u011b"+
		"\5@!\2\u011b\u011c\7\r\2\2\u011c\u011d\5F$\2\u011d\u011e\7\4\2\2\u011e"+
		"\u011f\7\4\2\2\u011f9\3\2\2\2\u0120\u0121\t\4\2\2\u0121;\3\2\2\2\u0122"+
		"\u0123\7#\2\2\u0123\u0124\5> \2\u0124\u0125\5@!\2\u0125\u0126\7\r\2\2"+
		"\u0126\u0127\5F$\2\u0127\u0128\7$\2\2\u0128\u0129\5F$\2\u0129\u012a\7"+
		"\4\2\2\u012a\u012b\7\4\2\2\u012b=\3\2\2\2\u012c\u012d\t\5\2\2\u012d?\3"+
		"\2\2\2\u012e\u0130\5B\"\2\u012f\u012e\3\2\2\2\u0130\u0133\3\2\2\2\u0131"+
		"\u012f\3\2\2\2\u0131\u0132\3\2\2\2\u0132A\3\2\2\2\u0133\u0131\3\2\2\2"+
		"\u0134\u0135\7@\2\2\u0135\u0136\5D#\2\u0136\u0137\7$\2\2\u0137\u0138\5"+
		"D#\2\u0138\u0139\7A\2\2\u0139C\3\2\2\2\u013a\u013b\t\6\2\2\u013bE\3\2"+
		"\2\2\u013c\u013f\5f\64\2\u013d\u013f\7C\2\2\u013e\u013c\3\2\2\2\u013e"+
		"\u013d\3\2\2\2\u013fG\3\2\2\2\u0140\u0141\7D\2\2\u0141\u0142\7O\2\2\u0142"+
		"\u0143\7R\2\2\u0143\u014f\7\4\2\2\u0144\u0145\7D\2\2\u0145\u0149\5N(\2"+
		"\u0146\u0148\5P)\2\u0147\u0146\3\2\2\2\u0148\u014b\3\2\2\2\u0149\u0147"+
		"\3\2\2\2\u0149\u014a\3\2\2\2\u014a\u014c\3\2\2\2\u014b\u0149\3\2\2\2\u014c"+
		"\u014d\7\4\2\2\u014d\u014f\3\2\2\2\u014e\u0140\3\2\2\2\u014e\u0144\3\2"+
		"\2\2\u014fI\3\2\2\2\u0150\u0151\7E\2\2\u0151\u0152\5N(\2\u0152\u0156\5"+
		"L\'\2\u0153\u0155\5P)\2\u0154\u0153\3\2\2\2\u0155\u0158\3\2\2\2\u0156"+
		"\u0154\3\2\2\2\u0156\u0157\3\2\2\2\u0157\u0159\3\2\2\2\u0158\u0156\3\2"+
		"\2\2\u0159\u015a\7\4\2\2\u015aK\3\2\2\2\u015b\u015c\7F\2\2\u015c\u015d"+
		"\7O\2\2\u015d\u015e\7\4\2\2\u015eM\3\2\2\2\u015f\u0160\7G\2\2\u0160\u0161"+
		"\7O\2\2\u0161\u0162\7R\2\2\u0162\u0163\7\4\2\2\u0163O\3\2\2\2\u0164\u0165"+
		"\7H\2\2\u0165\u0166\7R\2\2\u0166\u0167\7O\2\2\u0167\u0168\7\4\2\2\u0168"+
		"Q\3\2\2\2\u0169\u016a\7I\2\2\u016a\u016b\7N\2\2\u016b\u016c\5\\/\2\u016c"+
		"\u016d\7\4\2\2\u016dS\3\2\2\2\u016e\u016f\7J\2\2\u016f\u0170\7N\2\2\u0170"+
		"\u0171\5\\/\2\u0171\u0172\7\4\2\2\u0172U\3\2\2\2\u0173\u0174\7K\2\2\u0174"+
		"\u0175\7N\2\2\u0175\u0176\5\\/\2\u0176\u0177\7\4\2\2\u0177W\3\2\2\2\u0178"+
		"\u0179\7L\2\2\u0179\u017a\7N\2\2\u017a\u017b\5\\/\2\u017b\u017c\7\4\2"+
		"\2\u017cY\3\2\2\2\u017d\u017e\7M\2\2\u017e\u017f\7N\2\2\u017f\u0180\7"+
		"N\2\2\u0180\u0181\7\4\2\2\u0181[\3\2\2\2\u0182\u0184\7O\2\2\u0183\u0182"+
		"\3\2\2\2\u0184\u0185\3\2\2\2\u0185\u0183\3\2\2\2\u0185\u0186\3\2\2\2\u0186"+
		"]\3\2\2\2\u0187\u0188\7O\2\2\u0188\u0189\7\r\2\2\u0189\u018a\5`\61\2\u018a"+
		"\u018c\7\4\2\2\u018b\u018d\5\34\17\2\u018c\u018b\3\2\2\2\u018c\u018d\3"+
		"\2\2\2\u018d_\3\2\2\2\u018e\u0190\5h\65\2\u018f\u018e\3\2\2\2\u0190\u0193"+
		"\3\2\2\2\u0191\u018f\3\2\2\2\u0191\u0192\3\2\2\2\u0192a\3\2\2\2\u0193"+
		"\u0191\3\2\2\2\u0194\u0196\5d\63\2\u0195\u0194\3\2\2\2\u0196\u0199\3\2"+
		"\2\2\u0197\u0195\3\2\2\2\u0197\u0198\3\2\2\2\u0198c\3\2\2\2\u0199\u0197"+
		"\3\2\2\2\u019a\u019b\t\7\2\2\u019be\3\2\2\2\u019c\u019d\7O\2\2\u019dg"+
		"\3\2\2\2\u019e\u019f\t\b\2\2\u019fi\3\2\2\2\31ms{\u008d\u009f\u00a7\u00ad"+
		"\u00b4\u00ba\u00c5\u00d8\u00e5\u0101\u0117\u0131\u013e\u0149\u014e\u0156"+
		"\u0185\u018c\u0191\u0197";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}