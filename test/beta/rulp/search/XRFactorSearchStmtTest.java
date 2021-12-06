package beta.rulp.search;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSearchStmtTest extends RuleTestBase {

	@Test
	void test_search_1_int_var_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
