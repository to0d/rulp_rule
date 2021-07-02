package beta.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpTestBase;
import alpha.rulp.utils.RulpUtil;

class OptimizeUtilTest extends RulpTestBase {

	void _test_optimize(String inputExpr, String expectExpr) {

		try {
			LinkedList<IRObject> exprList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputExpr)) {
				exprList.add(obj);
			}

			assertEquals(inputExpr, 1, exprList.size());

			IRObject obj = exprList.get(0);
			obj = OptimizeUtil.optimizeExpr(obj);
			assertEquals(inputExpr, expectExpr, RulpUtil.toString(obj));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	void _test_optimize_expr(String inputExpr) {
		_test_optimize(inputExpr, inputExpr);
	}

	void _test_optimize_rule(String condExpr, String actionExpr) {
		_test_optimize_rule(condExpr, actionExpr, condExpr, actionExpr);
	}

	void _test_optimize_rule(String condExpr, String actionExpr, String expectCondExpr, String expectActionExpr) {

		try {

			LinkedList<IRObject> condList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(condExpr)) {
				condList.add(obj);
			}

			LinkedList<IRObject> actionList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(actionExpr)) {
				actionList.add(obj);
			}

			Pair<IRList, IRList> rst = OptimizeUtil.optimizeRule(RulpFactory.createList(condList),
					RulpFactory.createList(actionList), _getInterpreter().getMainFrame());

			assertEquals(expectCondExpr, RulpUtil.toString(rst.getKey()));
			assertEquals(expectActionExpr, RulpUtil.toString(rst.getValue()));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_optimize_expr() {

		_test_optimize_expr("a");
		_test_optimize_expr("1");
		_test_optimize_expr("1.1");
		_test_optimize_expr("\"abc\"");
		_test_optimize_expr("()");
		_test_optimize_expr("'()");
		_test_optimize_expr("(a b c)");
		_test_optimize_expr("'(a b c)");

		// (not (equal a b)) ==> (not-equal a b)
		_test_optimize_expr("(not a)");
		_test_optimize_expr("(equal a b)");
		_test_optimize("(not (equal a b))", "(not-equal a b)");
	}

	@Test
	void test_optimize_rule() {
		_test_optimize_rule("'(?a ?p ?b) '(?p nm:propertyOf nm:tagProperty)", "(-> '(?a nm:typeOf nm:tag))",
				"'('(?a ?p ?b) '(?p nm:propertyOf nm:tagProperty))", "'((-> '(?a nm:typeOf nm:tag)))");
	}

}
