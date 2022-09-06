package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorBackSearchTest extends RuleTestBase {

	@Test
	void test_back_search_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_back_search_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_3() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_4_circular_proof() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_5_internal_var_1() {

		_setup();
		_run_script();
//		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_5_internal_var_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_6_multi_cond_1_and() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_6_multi_cond_2_or() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_6_multi_cond_3_duplicate_and() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_6_multi_cond_3_tree() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_7_deep_first() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_8_rhs_expr_1_a() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_8_rhs_expr_1_b() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_9_query_var_1() {

		_setup();
		_run_script();
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_9_query_var_2() {

		_setup();
		_run_script();
		_dumpEntryTable("m");
	}

	@Test
	void test_back_search_9_query_var_3_limit() {

		_setup();
		_run_script();
		_dumpEntryTable("m");
	}
}
