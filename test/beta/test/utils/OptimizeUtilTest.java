package beta.test.utils;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpTestBase;
import alpha.rulp.utils.RulpUtil;

class OptimizeUtilTest extends RulpTestBase {

	String _optimize_expr(String inputExpr) throws RException, IOException {

		IRInterpreter interpreter = _getInterpreter();

		LinkedList<IRObject> exprList = new LinkedList<>();
		for (IRObject obj : RulpFactory.createParser().parse(inputExpr)) {
			exprList.add(obj);
		}
		assertEquals(inputExpr, 1, exprList.size());
		IRObject obj = exprList.get(0);
		obj = OptimizeUtil.optimizeExpr((IRExpr) obj, interpreter, interpreter.getMainFrame());

		return RulpUtil.toString(obj);
	}

	String _optimize_match_tree(String inputExpr) throws RException, IOException {

		IRInterpreter interpreter = _getInterpreter();

		List<IRObject> input = RulpFactory.createParser().parse(inputExpr);
		assertEquals(2, input.size());

		IRList condList = RulpUtil.asList(input.get(0));
		IRList actionList = RulpUtil.asList(input.get(1));

		List<IRList> actualMatchStmtList = new ArrayList<>();
		IRIterator<? extends IRObject> matchIter = condList.iterator();
		while (matchIter.hasNext()) {
			IRObject action = matchIter.next();
			actualMatchStmtList.add((IRList) action);
		}

		IRList matchTree = MatchTree.build(actualMatchStmtList, interpreter, interpreter.getMainFrame());

		List<IRExpr> actionStmtList = new LinkedList<>();
		IRIterator<? extends IRObject> actionIter = actionList.iterator();
		while (actionIter.hasNext()) {
			IRObject action = actionIter.next();
			actionStmtList.add(RulpUtil.asExpression(action));
		}

		IRList optMatchTree = OptimizeUtil.optimizeMatchTree(matchTree, actionStmtList);

		return RulpUtil.toString(optMatchTree);
	}

	String _optimize_rule_action_index_var(String inputExpr) throws RException {

		List<IRObject> input = RulpFactory.createParser().parse(inputExpr);
		assertEquals(2, input.size());

		IRList condList = RulpUtil.asList(input.get(0));
		IRList actionList = RulpUtil.asList(input.get(1));

		IRList optAtionList = OptimizeUtil.optimizeRuleActionIndexVar(condList, actionList);

		return RulpUtil.toString(optAtionList);
	}

	String _optimize_rule_has_stmt(String inputExpr) throws RException, IOException {

		IRInterpreter interpreter = _getInterpreter();

		List<IRObject> input = RulpFactory.createParser().parse(inputExpr);
		assertEquals(2, input.size());

		IRList condList = RulpUtil.asList(input.get(0));
		IRList actionList = RulpUtil.asList(input.get(1));

		Pair<IRList, IRList> rst = OptimizeUtil.optimizeRuleHasStmt(condList, actionList, interpreter,
				interpreter.getMainFrame());

		return RulpUtil.toString(RulpFactory.createList(rst.getKey(), rst.getValue()));
	}

	String _optimize_unused_condition(String inputExpr) throws RException {

		List<IRObject> input = RulpFactory.createParser().parse(inputExpr);
		assertEquals(2, input.size());

		IRList condList = RulpUtil.asList(input.get(0));
		IRList actionList = RulpUtil.asList(input.get(1));

		IRList optConditionList = OptimizeUtil.optimizeRuleRemoveUnusedCondition(condList, actionList);

		return RulpUtil.toString(optConditionList);
	}

	@Test
	void test_optimize_expr() {

		_setup();

		_test((input) -> {
			return _optimize_expr(input);
		});

	}

	@Test
	void test_optimize_match_tree_unused_var() {

		_setup();

		_test((input) -> {
			return _optimize_match_tree(input);
		});

	}

	@Test
	void test_optimize_rule_action_index_var() {

		_setup();

		_test((input) -> {
			return _optimize_rule_action_index_var(input);
		});

	}

	@Test
	void test_optimize_rule_has_stmt() {

		_setup();

		_test((input) -> {
			return _optimize_rule_has_stmt(input);
		});

	}

	@Test
	void test_optimize_unused_condition() {

		_setup();

		_test((input) -> {
			return _optimize_unused_condition(input);
		});

	}
}
