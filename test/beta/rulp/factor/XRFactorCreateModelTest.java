package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorCreateModelTest extends RuleTestBase {

	@Test
	void test_create_model() {

		_setup();
		_test("(new model m)", "m");
		_test("(type-of m)", "instance");
		_test("(class-of m)", "model");
		_test("(name-of m)", "\"m\"");
	}
}
