package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorQueryStmtTest extends RuleTestBase {

	@Test
	void test_query_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_query_stmt_2_loader_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_2_loader_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_3_already_has_data_a_join() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_3_already_has_data_b_join_twice() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_3_query_1_already_has_data() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_4_no_data_rule() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_5_var() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_6_where_1_expr_1_a() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_6_where_1_expr_1_b() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_6_where_2_uniq_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_6_where_1_expr_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_7_do_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_7_do_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_7_do_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_7_do_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_8_limit_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_9_order_by_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_a_reverse_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_query_stmt_b_any() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_query_stmt_c_opt_1_has_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_query_stmt_c_opt_1_has_stmt_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
