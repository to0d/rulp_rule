package beta.rulp.worker;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddLazyStmtTest extends RuleTestBase {

	@Test
	void test_add_lazy_stmt_1_load_twice() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_lazy_stmt_2_run_without_load_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_lazy_stmt_3_run_with_load_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_lazy_stmt_4_variable_length_entry() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
