package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveStmtTest extends RuleTestBase {

	@Test
	void test_3_varargs_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_varargs_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_varargs_3() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_varargs_4() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_varargs_5() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_varargs_6() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_4_remove_assume() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_5_remove_list_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_4_remove_define() {

		_setup();
		_run_script();
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
	}

	@Test
	void test_conflict_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_conflict_3() {

		_setup();
		_run_script();
		_statsInfo("m");
		_refInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_drop_stmt_rule_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_drop_stmt_rule_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_incomplete_mode() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
