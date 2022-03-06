package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveStmtTest extends RuleTestBase {

	@Test
	void test_remove_stmt_0_in_rule_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_0_in_rule_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_1_conflict_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_1_conflict_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_1_conflict_3() {

		_setup();
		_run_script();
		_statsInfo("m");
		_refInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_remove_stmt_2_incomplete_mode() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_3_varargs_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_remove_stmt_3_varargs_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_3_varargs_3() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_3_varargs_4() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_3_varargs_5() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_3_varargs_6() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_stmt_4_remove_assume() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_remove_stmt_4_remove_define() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_remove_stmt_5_rule_auto_delete() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_remove_stmt_6_remove_list_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	
	@Test
	void test_remove_stmt_7_limit_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_remove_stmt_8_reverse_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_remove_stmt_9_order_by_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_remove_stmt_a_constant_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
