package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAssumeStmttTest extends RuleTestBase {

	@Test
	void test_assume_add_1() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-constraint m n1:'(?x) (type int on ?x))");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(assume-stmt m n1:'(2))", "true");
		_test("(assume-stmt m n1:'(1))", "true"); // duplicated
		_test("(assume-stmt m n1:'(a))", "false");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1) n1:'(2))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_assume_add_2() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-constraint m n1:'(?x) (type int on ?x))");
		_test("(add-constraint m n2:'(?x) (< ?x 5))");
		_test("(add-rule m if n1:'(?x) do (-> m n2:'(?x)))", "RU000");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(assume-stmt m n1:'(2))", "true");
		_test("(assume-stmt m n1:'(5))", "false");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1) n1:'(2))");
		_test("(list-stmt m from n2:'(?x))", "'(n2:'(1) n2:'(2))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
