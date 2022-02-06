package beta.rulp.constraint;

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
	void test_add_constraint_2_type_fail_add_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_2_type_fail_add_constraint() {

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
	void test_add_constraint_3_uniq_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_constraint_4_not_null_1() {

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

		_setup();
		_clean_model_cache();
		_test("(new model m)");
		_test("(add-constraint m n1:'(?x ?y) n2:'() (!= ?x nil))", "1");
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

		_test("(new model m)", "m");
		_test("(add-rule m if n1:'(?a ?p ?b) (> ?b 3) do (-> n2:'(?b)))");
		_test("(add-rule m if n1:'(?a ?p ?b) (> ?b 4) do (-> n3:'(?b)))");
		_test("(add-stmt m n1:'(a b 5))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'(n1:'(a b 5) n2:'(5) n3:'(5))");
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
	void test_add_constraint_d_func_diff_previous_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
