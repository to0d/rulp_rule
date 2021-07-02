package beta.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class RuleUtilityTest {

	@Test
	void test_toStmtList() {
		_test_toStmtList("'(a b c)", "'('(a b c))");
		_test_toStmtList("'('(a b c) '(x y z))", "'('(a b c) '(x y z))");
		_test_toStmtList("'('(a b c) '('(x y z) '(m n t)))", "'('(a b c) '(x y z) '(m n t))");
	}

	void _test_toStmtList(String input, String expect) {

		try {
			IRList stmtList = RulpFactory.createList(RuleUtil.toStmtList(input));
			String output = RulpUtil.toString(stmtList);
			assertEquals(String.format("input=%s", input), expect, output);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	
}
