package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSizeOfModelTest extends RuleTestBase {

	@Test
	void test() {

		_setup();
		_test("(new model m)");

		_test("(add-stmt m '(a1 p1 b1))");
		_test("(add-stmt m '(a1 p1 b2))");

		_test("(size-of m)", "2");
	}

}
