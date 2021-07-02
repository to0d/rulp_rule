package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorDeleteModelTest extends RuleTestBase {

	@Test
	void test() {

		_setup();
		_test("(new model m)");
		_test("(type-of m)", "instance");

		_test("(delete m)");
		_test("(type-of m)", "atom");
	}

}
