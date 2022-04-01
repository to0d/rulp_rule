package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorFixStmtTest extends RuleTestBase {

	@Test
	void test_fix_stmt_1_no_child() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_fix_stmt_2_remove_fix() {

		_setup();
		_run_script();
	}

	@Test
	void test_fix_stmt_3_auto_remove_child_ref() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p1 ?b) '(?p p2 ?c) do (-> m '(?a p3 ?c)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(c p2 d))");
		_test("(start m)");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(c p2 d) '(a p3 d))");

		_dumpEntryTable("m",
				"result/factor/XRFactorFixStmtTest/test_fix_stmt_3_auto_remove_child_ref.entry.dump.1.txt");

		_test("(fix-stmt m '(c p2 d))");
		_dumpEntryTable("m",
				"result/factor/XRFactorFixStmtTest/test_fix_stmt_3_auto_remove_child_ref.entry.dump.2.txt");
	}

	@Test
	void test_fix_stmt_4_child_is_fix() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

}
