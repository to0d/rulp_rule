package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorTryAddStmtTest extends RuleTestBase {

	@Test
	void test_try_add_1() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-constraint m n1:'(?x) '(type int on ?x))");
		_test("(try-add-stmt m n1:'(1))", "true");
		_test("(try-add-stmt m n1:'(2))", "true");
		_test("(try-add-stmt m n1:'(1))", "true"); // duplicated
		_test("(try-add-stmt m n1:'(a))", "false");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1) n1:'(2))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

}
