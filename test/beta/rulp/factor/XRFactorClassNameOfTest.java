package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RulpTestBase;

class XRFactorClassNameOfTest extends RulpTestBase {

	@Test
	void test() {
		_setup();
		_test("(new model core)", "core");
		_test("(name-of core)", "\"core\"");
		_test("(type-of core)", "instance");
		_test("(class-of core)", "model");
	}

}
