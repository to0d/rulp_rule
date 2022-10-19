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
	void test_opt_9_has_stmt_1_no_has_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_2_all_lhs_vars() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_3_has_vars() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_4_no_var() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_5_all_external_var() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
	
	@Test
	void test_opt_9_has_stmt_6_func_var() {

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

	@Test
	void test_opt_b_unused_var_in_action_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_5() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_6() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_b_unused_var_in_action_7() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_c_no_action_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_c_no_action_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_d_foreach_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_d_foreach_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_e_beta2_1_uniq_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_e_beta2_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_e_beta2_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

}
