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
		_run_script();

	}

	@Test
	void test_fix_stmt_4_child_is_fix() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_fix_stmt_5_inherit_dup_entry() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_fix_stmt_6_named_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_fix_stmt_7_array_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
