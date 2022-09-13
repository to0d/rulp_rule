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

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");

	}
}
