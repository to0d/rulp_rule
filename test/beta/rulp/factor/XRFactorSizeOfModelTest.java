package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSizeOfModelTest extends RuleTestBase {

	@Test
	void test_size_of_model_1() {

		_setup();
		_test("(new model m)");

		_test("(add-stmt m '(a1 p1 b1))");
		_test("(add-stmt m '(a1 p1 b2))");

		_test("(size-of m)", "2");
	}

	@Test
	void test_size_of_model_2() {

		_setup();
		_test("(new model m)");

		_test("(add-stmt m n1:'(a))");
		_test("(add-stmt m n2:'(b))");

		_test("(size-of m)", "2");
	}
}
