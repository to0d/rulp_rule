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
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_back_search_5_internal_var_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
