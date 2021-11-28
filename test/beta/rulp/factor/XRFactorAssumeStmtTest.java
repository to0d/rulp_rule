package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAssumeStmtTest extends RuleTestBase {

	@Test
	void test_assume_1_new() {

		_setup();

		_test("(new model m)", "m");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(assume-stmt m n1:'(2))", "true");
		_test("(assume-stmt m n1:'(1))", "true"); // duplicated
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1) n1:'(2))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_assume_1_no_update() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-stmt m n1:'(1))", "1");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_assume_1_update() {

		_setup();

		_test("(new model m)", "m");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(add-stmt m n1:'(1))", "1");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_assume_2_constraint_1() {

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
	void test_assume_2_constraint_2() {

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

	@Test
	void test_assume_3_delete_1() {

		_setup();

		_test("(new model m)", "m");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(remove-stmt m n1:'(1))", "'(n1:'(1))");
		_test("(list-stmt m from n1:'(?x))", "'()");
		_test("(assume-stmt m n1:'(1))", "true");
		_test("(list-stmt m from n1:'(?x))", "'(n1:'(1))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
