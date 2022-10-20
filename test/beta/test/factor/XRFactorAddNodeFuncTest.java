package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddNodeFuncTest extends RuleTestBase {

	@Test
	void test_add_node_func_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
