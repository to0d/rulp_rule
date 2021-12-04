package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddConstraintTest extends RuleTestBase {

	@Test
	void test_1_type_1() {

		_setup();
		_clean_model_cache();
		_run_script();
		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_1_type_2_fail_add_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_1_type_3_fail_add_constraint() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_1_type_4_fail_merge_constraint() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_2_uniq_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_not_null_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_4_expr_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_4_expr_2_not_equal_var() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_4_expr_3_const() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_5_var_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_5_var_2() {

		_setup();
		_run_script();
	}

	@Test
	void test_5_var_3() {

		_setup();
		_run_script();
	}

	@Test
	void test_6_max_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_6_max_2_remove_dup() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_7_min_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_8_one_of_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_9_cross_1() {

		_setup();
		_clean_model_cache();
		_test("(new model m)");
		_test("(add-constraint m n1:'(?x ?y) n2:'() (!= ?x nil))", "1");
		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_10_similar_constraint_with_rule_1() {

		_setup();

		_run_script();

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_10_similar_constraint_with_rule_2() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule m if n1:'(?a ?p ?b) (> ?b 3) do (-> n2:'(?b)))");
		_test("(add-rule m if n1:'(?a ?p ?b) (> ?b 4) do (-> n3:'(?b)))");
		_test("(add-stmt m n1:'(a b 5))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'(n1:'(a b 5) n2:'(5) n3:'(5))");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}
}
