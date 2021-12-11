package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveStmtTest extends RuleTestBase {

	@Test
	void test_3_varargs_1() {

		_setup();
		_run_script();

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_3() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_4() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_5() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_6() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_4_remove_assume() {

		_setup();

		_test("(new model m)", "m");
		_test("(assume-stmt m n1:'(a b c))", "true");
		_test("(list-stmt m)", "'(n1:'(a b c))");
		_test("(remove-stmt m n1:'(a b c))", "'(n1:'(a b c))");
		_test("(list-stmt m)", "'()");
		_test("(assume-stmt m n1:'(a b c))", "true");
		_test("(list-stmt m)", "'(n1:'(a b c))");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_4_remove_define() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-stmt m '(a b c))", "true");
		_test("(list-stmt m)", "'('(a b c))");
		_test("(remove-stmt m '(a b c))", "'('(a b c))");
		_test("(list-stmt m)", "'()");
		_test("(add-stmt m '(a b c))", "false");
		_test("(list-stmt m)", "'()");

		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_4_rule_auto_delete() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_conflict_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_conflict_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_conflict_3() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
		_refInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_drop_stmt_rule_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_drop_stmt_rule_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_incomplete_mode() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

}
