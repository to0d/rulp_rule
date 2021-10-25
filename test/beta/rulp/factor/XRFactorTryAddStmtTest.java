package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorTryAddStmtTest extends RuleTestBase {

	@Test
	void test_try_add_1() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-constraint m n1:'(?x) '(type int on ?x))");
		_test("(try-add-stmt m '(1))", "1");
		_test("(try-add-stmt m '(2))", "1");
		_test("(try-add-stmt m '(1))", "1"); // duplicated
		_test("(try-add-stmt m '(a))", "1");
		_test("(list-stmt m)", "'('(a p1 c) '(a p2 c))");
	}

}
