package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddConstraintTest extends RuleTestBase {

	@Test
	void test_1_type_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

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
		_test("(add-constraint m name5:'(?...)   '(type string on 2))", "1");
		_test("(add-constraint m name5:'(?x ?y ?z) '(uniq on '(?x ?y)))", "1");

		_test("(add-constraint m name6:'(?x ?y ?z) (!= ?x nil))", "1");
		_test("(add-constraint m name6:'(?x ?y ?z) (> ?y 5))", "1");
		_test("(add-constraint m name6:'(?x ?y ?z) (< ?x 10))", "1");

		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_1_type_1.txt");
	}

	@Test
	void test_1_type_2() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_1_type_2.rulp");
		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_1_type_2.txt");
	}

	@Test
	void test_1_type_3() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_1_type_3.rulp");
		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_1_type_3.txt");
	}

	@Test
	void test_1_type_4() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_1_type_4.rulp");
	}

	@Test
	void test_2_uniq_1() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test_script("result/constraint/XRFactorAddConstraintTest/test_2_uniq_1.rulp");
		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_2_uniq_1.txt");
	}

	@Test
	void test_3_not_null_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'(? ? ?))");
		_test("(add-constraint m '(?y not-null) on name1:'(?x ?y ?z))"); // not null constraint

		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_3_not_null_1.txt");
	}

	@Test
	void test_4_expr_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'(? ? ?))");
		_test("(add-constraint m (> ?z 1) on name1:'(?x ?y ?z))"); // user define expression constraint

		_statsInfo("m", "result/constraint/XRFactorAddConstraintTest/test_4_expr_1.txt");
	}

	@Test
	void test_5_var_1() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_5_var_1.rulp");
	}
	
	@Test
	void test_5_var_2() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_5_var_2.rulp");
	}
	
	@Test
	void test_5_var_3() {

		_setup();
		_test_script("result/constraint/XRFactorAddConstraintTest/test_5_var_3.rulp");
	}
}
