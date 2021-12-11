package beta.rulp.search;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSearchStmtTest extends RuleTestBase {

	@Test
	void test_search_1_int_var_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}

	@Test
	void test_search_1_int_var_2_a() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}
	
	@Test
	void test_search_1_int_var_2_b() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}

	@Test
	void test_search_2_limit_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}

	@Test
	void test_search_2_order_by_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}

	@Test
	void test_search_2_order_by_3() {

		_setup();
		_run_script();
		_statsInfo("m");
		_asmInfo("?s");
	}

}
