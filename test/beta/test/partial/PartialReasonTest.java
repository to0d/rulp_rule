package beta.test.partial;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class PartialReasonTest extends RuleTestBase {

	@Test
	void test_partial_1_full_a() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_partial_1_full_b() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_partial_2_r2_not_work() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_partial_3_alias_full() {

		_setup();
		_run_script();
		_statsInfo("p2d");

	}

	@Test
	void test_partial_4_alias_query_all() {

		_setup();
		_run_script();
		_statsInfo("p2d");

	}

	@Test
	void test_partial_5_alias_query_1() {

		_setup();
		_run_script();
		_statsInfo("p2d");

	}

	@Test
	void test_partial_6_root_based_rule() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
