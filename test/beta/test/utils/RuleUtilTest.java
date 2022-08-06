package beta.test.utils;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class RuleUtilTest extends RuleTestBase {

	@Test
	void test_toStmtList_1() {

		_setup();

		_test((input) -> {
			return RulpUtil.toString(RulpFactory.createList(RuleUtil.toStmtList(input)));
		});
	}

}
