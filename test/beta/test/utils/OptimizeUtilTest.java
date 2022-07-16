package beta.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpTestBase;
import alpha.rulp.utils.RulpUtil;

class OptimizeUtilTest extends RulpTestBase {

	void _test_optimize(String inputExpr, String expectExpr) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			LinkedList<IRObject> exprList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputExpr)) {
				exprList.add(obj);
			}
			assertEquals(inputExpr, 1, exprList.size());
			IRObject obj = exprList.get(0);
			obj = OptimizeUtil.optimizeExpr((IRExpr) obj, interpreter, interpreter.getMainFrame());
			assertEquals(inputExpr, expectExpr, RulpUtil.toString(obj));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	void _test_optimize_expr(String inputExpr) {
		_test_optimize(inputExpr, inputExpr);
	}

	void _test_optimize_rule_has_stmt(String condExpr, String actionExpr) {
		_test_optimize_rule_has_stmt(condExpr, actionExpr, condExpr, actionExpr);
	}

	void _test_optimize_rule_has_stmt(String condExpr, String actionExpr, String expectCondExpr,
			String expectActionExpr) {

		try {

			IRInterpreter interpreter = _getInterpreter();

			LinkedList<IRObject> condList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(condExpr)) {
				condList.add(obj);
			}

			LinkedList<IRObject> actionList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(actionExpr)) {
				actionList.add(obj);
			}

			Pair<IRList, IRList> rst = OptimizeUtil.optimizeRuleHasStmt(RulpFactory.createList(condList),
					RulpFactory.createList(actionList), interpreter, interpreter.getMainFrame());

			assertEquals(expectCondExpr, RulpUtil.toString(rst.getKey()));
			assertEquals(expectActionExpr, RulpUtil.toString(rst.getValue()));

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	void _test_optimize_rule_action_index_var(String condExpr, String actionExpr, String expectActionExpr) {

		try {

			LinkedList<IRObject> condList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(condExpr)) {
				condList.add(obj);
			}

			List<IRObject> actionList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(actionExpr)) {
				actionList.add(obj);
			}

			IRList optAtionList = OptimizeUtil.optimizeRuleActionIndexVar(RulpFactory.createList(condList),
					RulpFactory.createList(actionList));

			assertEquals(expectActionExpr, RulpUtil.toString(optAtionList));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_optimize_expr() {

		_setup();

		_test_optimize_expr("()");
		_test_optimize_expr("(a b c)");

		// (not (equal a b)) ==> (not-equal a b)
		_test_optimize_expr("(not a)");
		_test_optimize_expr("(equal a b)");
		_test_optimize("(not (equal a b))", "(not-equal a b)");
	}

	@Test
	void test_optimize_rule_has_stmt() {

		_setup();

		_test_optimize_rule_has_stmt("'(?a ?p ?b) '(?p nm:propertyOf nm:tagProperty)", "(-> '(?a nm:typeOf nm:tag))",
				"'('(?a ?p ?b))",
				"'((if (has-stmt '(?p nm:propertyOf nm:tagProperty)) do (-> '(?a nm:typeOf nm:tag))))");

		_test_optimize_rule_has_stmt("'(?a ?p ?b) (has-stmt (?b p c))", "(-> '(?a p2 d))", "'('(?a ?p ?b))",
				"'((if (has-stmt (?b p c)) do (-> '(?a p2 d))))");

	}

	@Test
	void test_optimize_rule_action_index_var() {

		_setup();

		_test_optimize_rule_action_index_var("'(?x p ?y) '(?y p ?z)", "(remove-stmt ?0)",
				"'((remove-stmt '(?x p ?y)))");

		_test_optimize_rule_action_index_var("'(?x p ?y) '(?y p ?z)", "(remove-stmt '(?x p ?y))",
				"'((remove-stmt '(?x p ?y)))");

	}

}
