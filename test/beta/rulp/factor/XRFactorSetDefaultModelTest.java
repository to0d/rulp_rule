package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSetDefaultModelTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();

		// XRModel.TRACE_RETE = true;
		_test("(new model m)");
		_test("(set-default-model m)");

		_test("(add-stmt '(a p b))");
		_test("(add-stmt '(x p y))");
		_test("(list-stmt)", "'('(a p b) '(x p y))");

		_test("(query-stmt '(?x ?y) from '(?x p ?y))", "'('(a b) '(x y))");
		_test("(remove-stmt '(a p b))");
		_test("(list-stmt)", "'('(x p y))");

		_test("(add-rule if '(?a ?p ?b) do (add-stmt '(?b ?p ?a)))");
		_test("(start)", "1");
		_test("(list-stmt)", "'('(x p y))");

		_test("(query-stmt ?x from '(?x p ?y))", "'(x y)");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/factor/XRFactorSetDefaultModelTest/test_1.txt");
	}
}
