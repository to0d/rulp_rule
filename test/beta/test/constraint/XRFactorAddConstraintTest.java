package beta.test.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddConstraintTest extends RuleTestBase {

	@Test
	void test_add_constraint_1() {

		_setup();
		_clean_model_cache();
		_run_script();
		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_add_constraint_2_type_fail_add_constraint() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_2_type_fail_add_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_2_type_fail_merge_constraint() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_4_not_nil_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_5_expr_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_5_expr_2_not_equal_var() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_5_expr_3_const() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_6_var_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_constraint_6_var_2() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_constraint_6_var_3() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_constraint_7_max_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_7_max_2_remove_dup() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_8_min_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_9_one_of_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_a_cross_1() {

		// Should fail

		_setup();
		_clean_model_cache();
		_run_script();
		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_add_constraint_b_similar_constraint_with_rule_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_b_similar_constraint_with_rule_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_c_single_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_c_single_2_dup() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_d_func_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_2_asc() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_3_desc() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_4_after_add_stmt() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_5_when_has_child() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_6_query_same_order() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_e_order_by_7() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
