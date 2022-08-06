package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestOptimize extends RuleTestBase {

	@Test
	void test_opt_1_alpha_match_expr() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_2_bad_expr_1_bigger() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_2_bad_expr_2_equal_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_opt_3_beta_join_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_4_beta_join_same_node_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_4_beta_join_same_node_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_3_named() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_1_orignal() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_5() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_6() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_7_p2d_tag_alias_1_full_with_single_child() {

		_setup();
		_run_script();
		_statsInfo("p2d");
		_dumpEntryTable("p2d");
	}

	@Test
	void test_opt_8_unused_left_var() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_a_has_stmt_in_rule_lhs_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
