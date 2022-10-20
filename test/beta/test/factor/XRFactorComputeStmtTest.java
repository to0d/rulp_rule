package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorComputeStmtTest extends RuleTestBase {

	@Test
	void test_add_compute_stmt_1_has_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
