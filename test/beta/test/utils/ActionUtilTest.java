package beta.test.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.ActionUtil;

class ActionUtilTest extends RuleTestBase {

	String _buildRelatedStmtUniqNames(String input) throws RException {

		List<IRObject> objs = this._getParser().parse(input);
		assertEquals(objs.size(), 1);

		IRExpr expr = RulpUtil.asExpression(objs.get(0));
		return "" + ActionUtil.buildRelatedStmtUniqNames(ActionUtil.buildRelatedStmtExprList(expr));
	}

	@Test
	void test_buildRelatedStmtUniqNames_1() {

		_setup();

		_test((input) -> {
			return _buildRelatedStmtUniqNames(input);
		});
	}

	@Test
	void test_buildRelatedStmtUniqNames_2() {

		_setup();

		_test((input) -> {
			return _buildRelatedStmtUniqNames(input);
		});
	}

	@Test
	void test_buildRelatedStmtUniqNames_3_any() {

		_setup();

		_test((input) -> {
			return _buildRelatedStmtUniqNames(input);
		});
	}
}
