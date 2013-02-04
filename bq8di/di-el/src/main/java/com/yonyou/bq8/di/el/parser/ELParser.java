package com.yonyou.bq8.di.el.parser;

import java.util.ArrayList;
import java.util.List;

import com.yonyou.bq8.di.el.expression.BinaryOperatorExpression;
import com.yonyou.bq8.di.el.expression.BooleanLiteral;
import com.yonyou.bq8.di.el.expression.ComplexValue;
import com.yonyou.bq8.di.el.expression.ConditionalExpression;
import com.yonyou.bq8.di.el.expression.Expression;
import com.yonyou.bq8.di.el.expression.ExpressionString;
import com.yonyou.bq8.di.el.expression.FloatingPointLiteral;
import com.yonyou.bq8.di.el.expression.FunctionInvocation;
import com.yonyou.bq8.di.el.expression.IntegerLiteral;
import com.yonyou.bq8.di.el.expression.Literal;
import com.yonyou.bq8.di.el.expression.NamedValue;
import com.yonyou.bq8.di.el.expression.NullLiteral;
import com.yonyou.bq8.di.el.expression.StringLiteral;
import com.yonyou.bq8.di.el.expression.UnaryOperatorExpression;
import com.yonyou.bq8.di.el.expression.suffix.ArraySuffix;
import com.yonyou.bq8.di.el.expression.suffix.PropertySuffix;
import com.yonyou.bq8.di.el.expression.suffix.ValueSuffix;
import com.yonyou.bq8.di.el.operator.AndOperator;
import com.yonyou.bq8.di.el.operator.BinaryOperator;
import com.yonyou.bq8.di.el.operator.DivideOperator;
import com.yonyou.bq8.di.el.operator.EmptyOperator;
import com.yonyou.bq8.di.el.operator.EqualsOperator;
import com.yonyou.bq8.di.el.operator.GreaterThanOperator;
import com.yonyou.bq8.di.el.operator.GreaterThanOrEqualsOperator;
import com.yonyou.bq8.di.el.operator.LessThanOperator;
import com.yonyou.bq8.di.el.operator.LessThanOrEqualsOperator;
import com.yonyou.bq8.di.el.operator.MinusOperator;
import com.yonyou.bq8.di.el.operator.ModulusOperator;
import com.yonyou.bq8.di.el.operator.MultiplyOperator;
import com.yonyou.bq8.di.el.operator.NotEqualsOperator;
import com.yonyou.bq8.di.el.operator.NotOperator;
import com.yonyou.bq8.di.el.operator.OrOperator;
import com.yonyou.bq8.di.el.operator.PlusOperator;
import com.yonyou.bq8.di.el.operator.UnaryMinusOperator;
import com.yonyou.bq8.di.el.operator.UnaryOperator;

/**
 * 创建EL解析器
 * 
 * @author PeterPan
 * 
 */
public class ELParser implements ELParserConstants {

	public static void main(String args[]) throws ParseException {
		ELParser parser = new ELParser(System.in);
		parser.ExpressionString();
	}

	/*****************************************
	 * GRAMMAR PRODUCTIONS *
	 *****************************************/

	/**
	 * 如果表达式字符串是简单的String，则返回一个String；<br>
	 * 如果表达式字符串是简单的Expression，则返回一个Expression；<br>
	 * 如果表达式字符串是上述的混合，则返回一个ExpressionString<br>
	 */
	final public Object ExpressionString() throws ParseException {
		Object ret = "";
		List elems = null;
		Object elem;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case NON_EXPRESSION_TEXT:
			ret = AttrValueString();
			break;
		case START_EXPRESSION:
			ret = AttrValueExpression();
			break;
		default:
			jj_la1[0] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		label_1: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case NON_EXPRESSION_TEXT:
			case START_EXPRESSION:
				;
				break;
			default:
				jj_la1[1] = jj_gen;
				break label_1;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case NON_EXPRESSION_TEXT:
				elem = AttrValueString();
				break;
			case START_EXPRESSION:
				elem = AttrValueExpression();
				break;
			default:
				jj_la1[2] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			if (elems == null) {
				elems = new ArrayList();
				elems.add(ret);
			}
			elems.add(elem);
		}
		if (elems != null) {
			ret = new ExpressionString(elems.toArray());
		}
		{
			if (true)
				return ret;
		}
		throw new Error("Missing return statement in function");
	}

	final public String AttrValueString() throws ParseException {
		Token t;
		t = jj_consume_token(NON_EXPRESSION_TEXT);
		{
			if (true)
				return t.image;
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression AttrValueExpression() throws ParseException {
		Expression exp;
		jj_consume_token(START_EXPRESSION);
		exp = Expression();
		jj_consume_token(END_EXPRESSION);
		{
			if (true)
				return exp;
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression Expression() throws ParseException {
		Expression ret;
		if (jj_2_1(2147483647)) {
			ret = ConditionalExpression();
		} else {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case INTEGER_LITERAL:
			case FLOATING_POINT_LITERAL:
			case STRING_LITERAL:
			case TRUE:
			case FALSE:
			case NULL:
			case LPAREN:
			case MINUS:
			case NOT1:
			case NOT2:
			case EMPTY:
			case IDENTIFIER:
				ret = OrExpression();
				break;
			default:
				jj_la1[3] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
		}
		{
			if (true)
				return ret;
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression OrExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = AndExpression();
		label_2: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case OR1:
			case OR2:
				;
				break;
			default:
				jj_la1[4] = jj_gen;
				break label_2;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case OR1:
				jj_consume_token(OR1);
				break;
			case OR2:
				jj_consume_token(OR2);
				break;
			default:
				jj_la1[5] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			operator = OrOperator.SINGLETON;
			expression = AndExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression AndExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = EqualityExpression();
		label_3: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case AND1:
			case AND2:
				;
				break;
			default:
				jj_la1[6] = jj_gen;
				break label_3;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case AND1:
				jj_consume_token(AND1);
				break;
			case AND2:
				jj_consume_token(AND2);
				break;
			default:
				jj_la1[7] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			operator = AndOperator.SINGLETON;
			expression = EqualityExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression EqualityExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = RelationalExpression();
		label_4: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case EQ1:
			case EQ2:
			case NE1:
			case NE2:
				;
				break;
			default:
				jj_la1[8] = jj_gen;
				break label_4;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case EQ1:
			case EQ2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case EQ1:
					jj_consume_token(EQ1);
					break;
				case EQ2:
					jj_consume_token(EQ2);
					break;
				default:
					jj_la1[9] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = EqualsOperator.SINGLETON;
				break;
			case NE1:
			case NE2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case NE1:
					jj_consume_token(NE1);
					break;
				case NE2:
					jj_consume_token(NE2);
					break;
				default:
					jj_la1[10] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = NotEqualsOperator.SINGLETON;
				break;
			default:
				jj_la1[11] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			expression = RelationalExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression RelationalExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = AddExpression();
		label_5: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case GT1:
			case GT2:
			case LT1:
			case LT2:
			case LE1:
			case LE2:
			case GE1:
			case GE2:
				;
				break;
			default:
				jj_la1[12] = jj_gen;
				break label_5;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case LT1:
			case LT2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case LT1:
					jj_consume_token(LT1);
					break;
				case LT2:
					jj_consume_token(LT2);
					break;
				default:
					jj_la1[13] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = LessThanOperator.SINGLETON;
				break;
			case GT1:
			case GT2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case GT1:
					jj_consume_token(GT1);
					break;
				case GT2:
					jj_consume_token(GT2);
					break;
				default:
					jj_la1[14] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = GreaterThanOperator.SINGLETON;
				break;
			case GE1:
			case GE2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case GE1:
					jj_consume_token(GE1);
					break;
				case GE2:
					jj_consume_token(GE2);
					break;
				default:
					jj_la1[15] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = GreaterThanOrEqualsOperator.SINGLETON;
				break;
			case LE1:
			case LE2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case LE1:
					jj_consume_token(LE1);
					break;
				case LE2:
					jj_consume_token(LE2);
					break;
				default:
					jj_la1[16] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = LessThanOrEqualsOperator.SINGLETON;
				break;
			default:
				jj_la1[17] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			expression = AddExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression AddExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = MultiplyExpression();
		label_6: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case PLUS:
			case MINUS:
				;
				break;
			default:
				jj_la1[18] = jj_gen;
				break label_6;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case PLUS:
				jj_consume_token(PLUS);
				operator = PlusOperator.SINGLETON;
				break;
			case MINUS:
				jj_consume_token(MINUS);
				operator = MinusOperator.SINGLETON;
				break;
			default:
				jj_la1[19] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			expression = MultiplyExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression MultiplyExpression() throws ParseException {
		Expression startExpression;
		BinaryOperator operator;
		Expression expression;
		List operators = null;
		List expressions = null;
		startExpression = UnaryExpression();
		label_7: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case MULTIPLY:
			case DIVIDE1:
			case DIVIDE2:
			case MODULUS1:
			case MODULUS2:
				;
				break;
			default:
				jj_la1[20] = jj_gen;
				break label_7;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case MULTIPLY:
				jj_consume_token(MULTIPLY);
				operator = MultiplyOperator.SINGLETON;
				break;
			case DIVIDE1:
			case DIVIDE2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case DIVIDE1:
					jj_consume_token(DIVIDE1);
					break;
				case DIVIDE2:
					jj_consume_token(DIVIDE2);
					break;
				default:
					jj_la1[21] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = DivideOperator.SINGLETON;
				break;
			case MODULUS1:
			case MODULUS2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case MODULUS1:
					jj_consume_token(MODULUS1);
					break;
				case MODULUS2:
					jj_consume_token(MODULUS2);
					break;
				default:
					jj_la1[22] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = ModulusOperator.SINGLETON;
				break;
			default:
				jj_la1[23] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			expression = UnaryExpression();
			if (operators == null) {
				operators = new ArrayList();
				expressions = new ArrayList();
			}
			operators.add(operator);
			expressions.add(expression);
		}
		if (operators != null) {
			{
				if (true)
					return new BinaryOperatorExpression(startExpression,
							operators, expressions);
			}
		} else {
			{
				if (true)
					return startExpression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression ConditionalExpression() throws ParseException {
		Expression condition, trueBranch, falseBranch;
		condition = OrExpression();
		jj_consume_token(COND);
		trueBranch = Expression();
		jj_consume_token(COLON);
		falseBranch = Expression();
		{
			if (true)
				return new ConditionalExpression(condition, trueBranch,
						falseBranch);
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression UnaryExpression() throws ParseException {
		Expression expression;
		UnaryOperator singleOperator = null;
		UnaryOperator operator;
		List operators = null;
		label_8: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case MINUS:
			case NOT1:
			case NOT2:
			case EMPTY:
				;
				break;
			default:
				jj_la1[24] = jj_gen;
				break label_8;
			}
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case NOT1:
			case NOT2:
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case NOT1:
					jj_consume_token(NOT1);
					break;
				case NOT2:
					jj_consume_token(NOT2);
					break;
				default:
					jj_la1[25] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				operator = NotOperator.SINGLETON;
				break;
			case MINUS:
				jj_consume_token(MINUS);
				operator = UnaryMinusOperator.SINGLETON;
				break;
			case EMPTY:
				jj_consume_token(EMPTY);
				operator = EmptyOperator.SINGLETON;
				break;
			default:
				jj_la1[26] = jj_gen;
				jj_consume_token(-1);
				throw new ParseException();
			}
			if (singleOperator == null) {
				singleOperator = operator;
			} else if (operators == null) {
				operators = new ArrayList();
				operators.add(singleOperator);
				operators.add(operator);
			} else {
				operators.add(operator);
			}
		}
		expression = Value();
		if (operators != null) {
			{
				if (true)
					return new UnaryOperatorExpression(null, operators,
							expression);
			}
		} else if (singleOperator != null) {
			{
				if (true)
					return new UnaryOperatorExpression(singleOperator, null,
							expression);
			}
		} else {
			{
				if (true)
					return expression;
			}
		}
		throw new Error("Missing return statement in function");
	}

	final public Expression Value() throws ParseException {
		Expression prefix;
		ValueSuffix suffix;
		List suffixes = null;
		prefix = ValuePrefix();
		label_9: while (true) {
			switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
			case DOT:
			case LBRACKET:
				;
				break;
			default:
				jj_la1[27] = jj_gen;
				break label_9;
			}
			suffix = ValueSuffix();
			if (suffixes == null) {
				suffixes = new ArrayList();
			}
			suffixes.add(suffix);
		}
		if (suffixes == null) {
			{
				if (true)
					return prefix;
			}
		} else {
			{
				if (true)
					return new ComplexValue(prefix, suffixes);
			}
		}
		throw new Error("Missing return statement in function");
	}

	/**
	 * This is an element that can start a value
	 **/
	final public Expression ValuePrefix() throws ParseException {
		Expression ret;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case INTEGER_LITERAL:
		case FLOATING_POINT_LITERAL:
		case STRING_LITERAL:
		case TRUE:
		case FALSE:
		case NULL:
			ret = Literal();
			break;
		case LPAREN:
			jj_consume_token(LPAREN);
			ret = Expression();
			jj_consume_token(RPAREN);
			break;
		default:
			jj_la1[28] = jj_gen;
			if (jj_2_2(2147483647)) {
				ret = FunctionInvocation();
			} else {
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case IDENTIFIER:
					ret = NamedValue();
					break;
				default:
					jj_la1[29] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
			}
		}
		{
			if (true)
				return ret;
		}
		throw new Error("Missing return statement in function");
	}

	final public NamedValue NamedValue() throws ParseException {
		Token t;
		t = jj_consume_token(IDENTIFIER);
		{
			if (true)
				return new NamedValue(t.image);
		}
		throw new Error("Missing return statement in function");
	}

	final public FunctionInvocation FunctionInvocation() throws ParseException {
		String qualifiedName;
		List argumentList = new ArrayList();
		Expression exp;
		qualifiedName = QualifiedName();
		jj_consume_token(LPAREN);
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case INTEGER_LITERAL:
		case FLOATING_POINT_LITERAL:
		case STRING_LITERAL:
		case TRUE:
		case FALSE:
		case NULL:
		case LPAREN:
		case MINUS:
		case NOT1:
		case NOT2:
		case EMPTY:
		case IDENTIFIER:
			exp = Expression();
			argumentList.add(exp);
			label_10: while (true) {
				switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
				case COMMA:
					;
					break;
				default:
					jj_la1[30] = jj_gen;
					break label_10;
				}
				jj_consume_token(COMMA);
				exp = Expression();
				argumentList.add(exp);
			}
			break;
		default:
			jj_la1[31] = jj_gen;
			;
		}
		jj_consume_token(RPAREN);
		{
			if (true)
				return new FunctionInvocation(qualifiedName, argumentList);
		}
		throw new Error("Missing return statement in function");
	}

	final public ValueSuffix ValueSuffix() throws ParseException {
		ValueSuffix suffix;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case DOT:
			suffix = PropertySuffix();
			break;
		case LBRACKET:
			suffix = ArraySuffix();
			break;
		default:
			jj_la1[32] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		{
			if (true)
				return suffix;
		}
		throw new Error("Missing return statement in function");
	}

	final public PropertySuffix PropertySuffix() throws ParseException {
		Token t;
		String property;
		jj_consume_token(DOT);
		property = Identifier();
		{
			if (true)
				return new PropertySuffix(property);
		}
		throw new Error("Missing return statement in function");
	}

	final public ArraySuffix ArraySuffix() throws ParseException {
		Expression index;
		jj_consume_token(LBRACKET);
		index = Expression();
		jj_consume_token(RBRACKET);
		{
			if (true)
				return new ArraySuffix(index);
		}
		throw new Error("Missing return statement in function");
	}

	final public Literal Literal() throws ParseException {
		Literal ret;
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case TRUE:
		case FALSE:
			ret = BooleanLiteral();
			break;
		case INTEGER_LITERAL:
			ret = IntegerLiteral();
			break;
		case FLOATING_POINT_LITERAL:
			ret = FloatingPointLiteral();
			break;
		case STRING_LITERAL:
			ret = StringLiteral();
			break;
		case NULL:
			ret = NullLiteral();
			break;
		default:
			jj_la1[33] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		{
			if (true)
				return ret;
		}
		throw new Error("Missing return statement in function");
	}

	final public BooleanLiteral BooleanLiteral() throws ParseException {
		switch ((jj_ntk == -1) ? jj_ntk() : jj_ntk) {
		case TRUE:
			jj_consume_token(TRUE);
			{
				if (true)
					return BooleanLiteral.TRUE;
			}
			break;
		case FALSE:
			jj_consume_token(FALSE);
			{
				if (true)
					return BooleanLiteral.FALSE;
			}
			break;
		default:
			jj_la1[34] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		throw new Error("Missing return statement in function");
	}

	final public StringLiteral StringLiteral() throws ParseException {
		Token t;
		t = jj_consume_token(STRING_LITERAL);
		{
			if (true)
				return StringLiteral.fromToken(t.image);
		}
		throw new Error("Missing return statement in function");
	}

	final public IntegerLiteral IntegerLiteral() throws ParseException {
		Token t;
		t = jj_consume_token(INTEGER_LITERAL);
		{
			if (true)
				return new IntegerLiteral(t.image);
		}
		throw new Error("Missing return statement in function");
	}

	final public FloatingPointLiteral FloatingPointLiteral()
			throws ParseException {
		Token t;
		t = jj_consume_token(FLOATING_POINT_LITERAL);
		{
			if (true)
				return new FloatingPointLiteral(t.image);
		}
		throw new Error("Missing return statement in function");
	}

	final public NullLiteral NullLiteral() throws ParseException {
		jj_consume_token(NULL);
		{
			if (true)
				return NullLiteral.SINGLETON;
		}
		throw new Error("Missing return statement in function");
	}

	final public String Identifier() throws ParseException {
		Token t;
		t = jj_consume_token(IDENTIFIER);
		{
			if (true)
				return t.image;
		}
		throw new Error("Missing return statement in function");
	}

	final public String QualifiedName() throws ParseException {
		String prefix = null, localPart = null;
		if (jj_2_3(2147483647)) {
			prefix = Identifier();
			jj_consume_token(COLON);
		} else {
			;
		}
		localPart = Identifier();
		if (prefix == null) {
			if (true)
				return localPart;
		} else {
			if (true)
				return prefix + ":" + localPart;
		}
		throw new Error("Missing return statement in function");
	}

	final private boolean jj_2_1(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_1();
		jj_save(0, xla);
		return retval;
	}

	final private boolean jj_2_2(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_2();
		jj_save(1, xla);
		return retval;
	}

	final private boolean jj_2_3(int xla) {
		jj_la = xla;
		jj_lastpos = jj_scanpos = token;
		boolean retval = !jj_3_3();
		jj_save(2, xla);
		return retval;
	}

	final private boolean jj_3R_42() {
		if (jj_scan_token(NE2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_91() {
		if (jj_scan_token(FLOATING_POINT_LITERAL))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_40() {
		if (jj_scan_token(EQ2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_41() {
		if (jj_scan_token(NE1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_32() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_41()) {
			jj_scanpos = xsp;
			if (jj_3R_42())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_90() {
		if (jj_scan_token(INTEGER_LITERAL))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_39() {
		if (jj_scan_token(EQ1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_31() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_39()) {
			jj_scanpos = xsp;
			if (jj_3R_40())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_26() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_31()) {
			jj_scanpos = xsp;
			if (jj_3R_32())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_25())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_70() {
		if (jj_scan_token(NOT2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_21() {
		if (jj_3R_25())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_26()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_92() {
		if (jj_scan_token(STRING_LITERAL))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_62() {
		if (jj_scan_token(EMPTY))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_61() {
		if (jj_scan_token(MINUS))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_69() {
		if (jj_scan_token(NOT1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_60() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_69()) {
			jj_scanpos = xsp;
			if (jj_3R_70())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_55() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_60()) {
			jj_scanpos = xsp;
			if (jj_3R_61()) {
				jj_scanpos = xsp;
				if (jj_3R_62())
					return true;
				if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_96() {
		if (jj_scan_token(FALSE))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_43() {
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_55()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		if (jj_3R_56())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_95() {
		if (jj_scan_token(TRUE))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_89() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_95()) {
			jj_scanpos = xsp;
			if (jj_3R_96())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_85() {
		if (jj_3R_93())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_84() {
		if (jj_3R_92())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_83() {
		if (jj_3R_91())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_82() {
		if (jj_3R_90())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_28() {
		if (jj_scan_token(AND2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_81() {
		if (jj_3R_89())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_76() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_81()) {
			jj_scanpos = xsp;
			if (jj_3R_82()) {
				jj_scanpos = xsp;
				if (jj_3R_83()) {
					jj_scanpos = xsp;
					if (jj_3R_84()) {
						jj_scanpos = xsp;
						if (jj_3R_85())
							return true;
						if (jj_la == 0 && jj_scanpos == jj_lastpos)
							return false;
					} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
						return false;
				} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_11() {
		if (jj_3R_14())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(COND))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(COLON))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_27() {
		if (jj_scan_token(AND1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_22() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_27()) {
			jj_scanpos = xsp;
			if (jj_3R_28())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_21())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_17() {
		if (jj_3R_21())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_22()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_68() {
		if (jj_scan_token(MODULUS2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_88() {
		if (jj_scan_token(LBRACKET))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(RBRACKET))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_66() {
		if (jj_scan_token(DIVIDE2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_67() {
		if (jj_scan_token(MODULUS1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_59() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_67()) {
			jj_scanpos = xsp;
			if (jj_3R_68())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_65() {
		if (jj_scan_token(DIVIDE1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_87() {
		if (jj_scan_token(DOT))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_13())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_58() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_65()) {
			jj_scanpos = xsp;
			if (jj_3R_66())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_57() {
		if (jj_scan_token(MULTIPLY))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_44() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_57()) {
			jj_scanpos = xsp;
			if (jj_3R_58()) {
				jj_scanpos = xsp;
				if (jj_3R_59())
					return true;
				if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_43())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_24() {
		if (jj_scan_token(OR2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_80() {
		if (jj_3R_88())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_33() {
		if (jj_3R_43())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_44()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_79() {
		if (jj_3R_87())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_75() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_79()) {
			jj_scanpos = xsp;
			if (jj_3R_80())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_23() {
		if (jj_scan_token(OR1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_18() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_23()) {
			jj_scanpos = xsp;
			if (jj_3R_24())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_17())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_14() {
		if (jj_3R_17())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_18()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_94() {
		if (jj_scan_token(COMMA))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_1() {
		if (jj_3R_11())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_86() {
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_94()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_20() {
		if (jj_3R_14())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_19() {
		if (jj_3R_11())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_46() {
		if (jj_scan_token(MINUS))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_15() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_19()) {
			jj_scanpos = xsp;
			if (jj_3R_20())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_45() {
		if (jj_scan_token(PLUS))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_34() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_45()) {
			jj_scanpos = xsp;
			if (jj_3R_46())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_33())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_77() {
		if (jj_3R_12())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(LPAREN))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_86())
			jj_scanpos = xsp;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(RPAREN))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_29() {
		if (jj_3R_33())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_34()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3_2() {
		if (jj_3R_12())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(LPAREN))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_78() {
		if (jj_scan_token(IDENTIFIER))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_74() {
		if (jj_3R_78())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_73() {
		if (jj_3R_77())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_72() {
		if (jj_scan_token(LPAREN))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_15())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(RPAREN))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_71() {
		if (jj_3R_76())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3_3() {
		if (jj_3R_13())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(COLON))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_63() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_71()) {
			jj_scanpos = xsp;
			if (jj_3R_72()) {
				jj_scanpos = xsp;
				if (jj_3R_73()) {
					jj_scanpos = xsp;
					if (jj_3R_74())
						return true;
					if (jj_la == 0 && jj_scanpos == jj_lastpos)
						return false;
				} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_54() {
		if (jj_scan_token(LE2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_52() {
		if (jj_scan_token(GE2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_50() {
		if (jj_scan_token(GT2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_48() {
		if (jj_scan_token(LT2))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_53() {
		if (jj_scan_token(LE1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_16() {
		if (jj_3R_13())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_scan_token(COLON))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_38() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_53()) {
			jj_scanpos = xsp;
			if (jj_3R_54())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_51() {
		if (jj_scan_token(GE1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_37() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_51()) {
			jj_scanpos = xsp;
			if (jj_3R_52())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_49() {
		if (jj_scan_token(GT1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_36() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_49()) {
			jj_scanpos = xsp;
			if (jj_3R_50())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_47() {
		if (jj_scan_token(LT1))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_35() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_47()) {
			jj_scanpos = xsp;
			if (jj_3R_48())
				return true;
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_12() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_16())
			jj_scanpos = xsp;
		else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_13())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_30() {
		Token xsp;
		xsp = jj_scanpos;
		if (jj_3R_35()) {
			jj_scanpos = xsp;
			if (jj_3R_36()) {
				jj_scanpos = xsp;
				if (jj_3R_37()) {
					jj_scanpos = xsp;
					if (jj_3R_38())
						return true;
					if (jj_la == 0 && jj_scanpos == jj_lastpos)
						return false;
				} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
					return false;
			} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		} else if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		if (jj_3R_29())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_25() {
		if (jj_3R_29())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_30()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_64() {
		if (jj_3R_75())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_13() {
		if (jj_scan_token(IDENTIFIER))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	final private boolean jj_3R_56() {
		if (jj_3R_63())
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		Token xsp;
		while (true) {
			xsp = jj_scanpos;
			if (jj_3R_64()) {
				jj_scanpos = xsp;
				break;
			}
			if (jj_la == 0 && jj_scanpos == jj_lastpos)
				return false;
		}
		return false;
	}

	final private boolean jj_3R_93() {
		if (jj_scan_token(NULL))
			return true;
		if (jj_la == 0 && jj_scanpos == jj_lastpos)
			return false;
		return false;
	}

	public ELParserTokenManager token_source;
	SimpleCharStream jj_input_stream;
	public Token token, jj_nt;
	private int jj_ntk;
	private Token jj_scanpos, jj_lastpos;
	private int jj_la;
	public boolean lookingAhead = false;
	private boolean jj_semLA;
	private int jj_gen;
	final private int[] jj_la1 = new int[35];
	final private int[] jj_la1_0 = { 0x6, 0x6, 0x6, 0x20007580, 0x0, 0x0, 0x0,
			0x0, 0x18600000, 0x600000, 0x18000000, 0x18600000, 0x79e0000,
			0x180000, 0x60000, 0x6000000, 0x1800000, 0x79e0000, 0x0, 0x0, 0x0,
			0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x10000, 0x20007580, 0x0, 0x80000000,
			0x20007580, 0x10000, 0x7580, 0x3000, };
	final private int[] jj_la1_1 = { 0x0, 0x0, 0x0, 0x50c10, 0xc000, 0xc000,
			0x3000, 0x3000, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
			0x18, 0x18, 0x3e0, 0xc0, 0x300, 0x3e0, 0x10c10, 0xc00, 0x10c10,
			0x2, 0x0, 0x40000, 0x0, 0x50c10, 0x2, 0x0, 0x0, };
	final private JJCalls[] jj_2_rtns = new JJCalls[3];
	private boolean jj_rescan = false;
	private int jj_gc = 0;

	public ELParser(java.io.InputStream stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new ELParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(java.io.InputStream stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public ELParser(java.io.Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new ELParserTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(java.io.Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public ELParser(ELParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	public void ReInit(ELParserTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 35; i++)
			jj_la1[i] = -1;
		for (int i = 0; i < jj_2_rtns.length; i++)
			jj_2_rtns[i] = new JJCalls();
	}

	final private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken;
		if ((oldToken = token).next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		if (token.kind == kind) {
			jj_gen++;
			if (++jj_gc > 100) {
				jj_gc = 0;
				for (int i = 0; i < jj_2_rtns.length; i++) {
					JJCalls c = jj_2_rtns[i];
					while (c != null) {
						if (c.gen < jj_gen)
							c.first = null;
						c = c.next;
					}
				}
			}
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	final private boolean jj_scan_token(int kind) {
		if (jj_scanpos == jj_lastpos) {
			jj_la--;
			if (jj_scanpos.next == null) {
				jj_lastpos = jj_scanpos = jj_scanpos.next = token_source
						.getNextToken();
			} else {
				jj_lastpos = jj_scanpos = jj_scanpos.next;
			}
		} else {
			jj_scanpos = jj_scanpos.next;
		}
		if (jj_rescan) {
			int i = 0;
			Token tok = token;
			while (tok != null && tok != jj_scanpos) {
				i++;
				tok = tok.next;
			}
			if (tok != null)
				jj_add_error_token(kind, i);
		}
		return (jj_scanpos.kind != kind);
	}

	final public Token getNextToken() {
		if (token.next != null)
			token = token.next;
		else
			token = token.next = token_source.getNextToken();
		jj_ntk = -1;
		jj_gen++;
		return token;
	}

	final public Token getToken(int index) {
		Token t = lookingAhead ? jj_scanpos : token;
		for (int i = 0; i < index; i++) {
			if (t.next != null)
				t = t.next;
			else
				t = t.next = token_source.getNextToken();
		}
		return t;
	}

	final private int jj_ntk() {
		if ((jj_nt = token.next) == null)
			return (jj_ntk = (token.next = token_source.getNextToken()).kind);
		else
			return (jj_ntk = jj_nt.kind);
	}

	private java.util.Vector jj_expentries = new java.util.Vector();
	private int[] jj_expentry;
	private int jj_kind = -1;
	private int[] jj_lasttokens = new int[100];
	private int jj_endpos;

	private void jj_add_error_token(int kind, int pos) {
		if (pos >= 100)
			return;
		if (pos == jj_endpos + 1) {
			jj_lasttokens[jj_endpos++] = kind;
		} else if (jj_endpos != 0) {
			jj_expentry = new int[jj_endpos];
			for (int i = 0; i < jj_endpos; i++) {
				jj_expentry[i] = jj_lasttokens[i];
			}
			boolean exists = false;
			for (java.util.Enumeration e = jj_expentries.elements(); e
					.hasMoreElements();) {
				int[] oldentry = (int[]) (e.nextElement());
				if (oldentry.length == jj_expentry.length) {
					exists = true;
					for (int i = 0; i < jj_expentry.length; i++) {
						if (oldentry[i] != jj_expentry[i]) {
							exists = false;
							break;
						}
					}
					if (exists)
						break;
				}
			}
			if (!exists)
				jj_expentries.addElement(jj_expentry);
			if (pos != 0)
				jj_lasttokens[(jj_endpos = pos) - 1] = kind;
		}
	}

	final public ParseException generateParseException() {
		jj_expentries.removeAllElements();
		boolean[] la1tokens = new boolean[55];
		for (int i = 0; i < 55; i++) {
			la1tokens[i] = false;
		}
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (int i = 0; i < 35; i++) {
			if (jj_la1[i] == jj_gen) {
				for (int j = 0; j < 32; j++) {
					if ((jj_la1_0[i] & (1 << j)) != 0) {
						la1tokens[j] = true;
					}
					if ((jj_la1_1[i] & (1 << j)) != 0) {
						la1tokens[32 + j] = true;
					}
				}
			}
		}
		for (int i = 0; i < 55; i++) {
			if (la1tokens[i]) {
				jj_expentry = new int[1];
				jj_expentry[0] = i;
				jj_expentries.addElement(jj_expentry);
			}
		}
		jj_endpos = 0;
		jj_rescan_token();
		jj_add_error_token(0, 0);
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i = 0, size = jj_expentries.size(); i < size; i++) {
			exptokseq[i] = (int[]) jj_expentries.elementAt(i);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	final public void enable_tracing() {
	}

	final public void disable_tracing() {
	}

	final private void jj_rescan_token() {
		jj_rescan = true;
		for (int i = 0; i < 3; i++) {
			JJCalls p = jj_2_rtns[i];
			do {
				if (p.gen > jj_gen) {
					jj_la = p.arg;
					jj_lastpos = jj_scanpos = p.first;
					switch (i) {
					case 0:
						jj_3_1();
						break;
					case 1:
						jj_3_2();
						break;
					case 2:
						jj_3_3();
						break;
					}
				}
				p = p.next;
			} while (p != null);
		}
		jj_rescan = false;
	}

	final private void jj_save(int index, int xla) {
		JJCalls p = jj_2_rtns[index];
		while (p.gen > jj_gen) {
			if (p.next == null) {
				p = p.next = new JJCalls();
				break;
			}
			p = p.next;
		}
		p.gen = jj_gen + xla - jj_la;
		p.first = token;
		p.arg = xla;
	}

	static final class JJCalls {
		int gen;
		Token first;
		int arg;
		JJCalls next;
	}

}
