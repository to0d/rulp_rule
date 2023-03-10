package beta.test.bs;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestBSMultiSearch extends RuleTestBase {

	@Test
	void test_bsm_1_int_var_1_a_query() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_1_b_start() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_1_c_bs() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_1_d_limit() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_1_e_order_by_asc() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_1_f_order_by_desc() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_2_a_query() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_2_b_bs() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}

	@Test
	void test_bsm_1_int_var_2_c() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}
}
