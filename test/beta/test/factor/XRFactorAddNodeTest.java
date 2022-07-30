package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddNodeTest extends RuleTestBase {

	@Test
	void test_add_node_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_node_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
