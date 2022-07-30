package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddIndexTest extends RuleTestBase {

	@Test
	void test_add_index_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
