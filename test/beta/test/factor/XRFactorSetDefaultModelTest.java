package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSetDefaultModelTest extends RuleTestBase {

	@Test
	void test_set_default_model_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
