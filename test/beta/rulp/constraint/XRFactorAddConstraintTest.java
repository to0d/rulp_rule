package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddConstraintTest extends RuleTestBase {

	@Test
	void test_1_type_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_clean_model_cache();

		_test("(new model m)");
		_test("(add-node m name1:'(3))", "1");
		_test("(add-node m name2:'(3))", "1");
		_test("(add-node m name3:'(3))", "1");
		_test("(add-node m name4:'(3))", "1");
		_test("(add-node m name5:'(3))", "1");
		_test("(add-node m name6:'(3))", "1");

		_test("(add-constraint m name1:'(?x ?y ?z) '(type int on ?x))", "1");
		_test("(add-constraint m name1:'(? ?y ?) '(type long on ?y))", "1");
		_test("(add-constraint m name1:'(? ? ?z) '(type float on ?z))", "1");
		_test("(add-constraint m name2:'(? ?y ?) '(type double on ?y))", "1");
		_test("(add-constraint m name3:'(? ?y ?) '(type bool on ?y))", "1");
		_test("(add-constraint m name4:'(? ?y ?) '(type atom on ?y))", "1");
		_test("(add-constraint m name4:'(? ? ?z) '(type string on ?z))", "1");
		_test("(add-constraint m name5:'(?...)   '(type string on ?2))", "1");
		_test("(add-constraint m name5:'(?x ?y ?z) '(uniq on '(?x ?y)))", "1");

		_test("(add-constraint m name6:'(?x ?y ?z) (!= ?x nil))", "1");
		_test("(add-constraint m name6:'(?x ?y ?z) (> ?y 5))", "1");
		_test("(add-constraint m name6:'(?x ?y ?) (< ?x 10))", "1");

		_test("(add-constraint m name7:'(?x ?y ?z) '(type int on ?x))", "1");
		_test("(add-constraint m name8:'(?x ?y) '(max 5 on ?x))", "1");
		_test("(add-constraint m name8:'(?x ?y) '(min 5 on ?y))", "1");
		_test("(add-constraint m name8:'(?x ?y) '(not-null on ?x))", "1");

		_test("(add-constraint m name9:'(?x ?y) '(one-of '(a b c) on ?x))", "1");

		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_1_type_2_fail_add_stmt() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_1_type_3_fail_add_constraint() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_1_type_4_fail_merge_constraint() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_2_uniq_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_3_not_null_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_4_expr_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_5_var_1() {

		_setup();
		_test_script();
	}

	@Test
	void test_5_var_2() {

		_setup();
		_test_script();
	}

	@Test
	void test_5_var_3() {

		_setup();
		_test_script();
	}

	@Test
	void test_6_max_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_6_max_2_remove_dup() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_7_min_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

	@Test
	void test_8_one_of_1() {

		_setup();
		_test_script();
		_statsInfo("m");
	}

}
