package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorHasStmtTest extends RuleTestBase {

	@Test
	void test_has_stmt_1() {

		_setup();
		_run_script();

	}

	@Test
	void test_has_stmt_2() {

		_setup();
		_run_script();

		// How to let A001 node be partial executed (Define count is 1)?
		_statsInfo("m");

	}

	@Test
	void test_has_stmt_3() {

		_setup();
		_run_script();

	}

	@Test
	void test_has_stmt_4() {

		_setup();
		_run_script();

	}

	@Test
	void test_has_stmt_5() {

		_setup();
		_run_script();

	}

	@Test
	void test_has_stmt_6_uniq_1() {

		_setup();
		_run_script();

	}
}
