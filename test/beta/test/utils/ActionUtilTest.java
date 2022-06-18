package beta.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.ActionUtil;

class ActionUtilTest extends RuleTestBase {

	@Test
	void test_buildRelatedStmtUniqNames_1() {
		_setup();
		_test_buildRelatedStmtUniqNames("(a b c)", "[]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(a b c))", "['(a b c)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?x b c))", "['(?0 b c)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(a ?b c))", "['(a ?0 c)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(a b ?c))", "['(a b ?0)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a ?b c))", "['(?0 ?1 c)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(a ?b ?c))", "['(a ?0 ?1)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a b ?c))", "['(?0 b ?1)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a ?b ?c))", "['(?0 ?1 ?2)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a ?a c))", "['(?0 ?0 c)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(a ?b ?b))", "['(a ?0 ?0)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a b ?a))", "['(?0 b ?0)]");
		_test_buildRelatedStmtUniqNames("(add-stmt '(?a ?a ?a))", "['(?0 ?0 ?0)]");
	}

	@Test
	void test_buildRelatedStmtUniqNames_2() {
		_setup();
		_test_buildRelatedStmtUniqNames("(-> '(a b c))", "['(a b c)]");
		_test_buildRelatedStmtUniqNames("(-> m2 '(a b c))", "['(a b c)]");
		_test_buildRelatedStmtUniqNames("(do (-> '(a b c)) (-> '(x y z)))", "['(a b c), '(x y z)]");
	}

	protected void _test_buildRelatedStmtUniqNames(String inputExpr, String expectNames) {

		try {

			List<IRObject> objs = this._getParser().parse(inputExpr);
			assertEquals(objs.size(), 1);

			IRExpr expr = RulpUtil.asExpression(objs.get(0));
			assertEquals(expectNames,
					"" + ActionUtil.buildRelatedStmtUniqNames(ActionUtil.buildRelatedStmtExprList(expr)));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

}
