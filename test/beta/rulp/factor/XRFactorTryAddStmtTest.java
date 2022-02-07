package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorTryAddStmtTest extends RuleTestBase {

	@Test
	void test_try_add_stmt_1() {

		_setup();
		_run_script();

	}

	@Test
	void test_try_add_stmt_2_constraint() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
