package beta.test.load;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class TestRuleSetNodeMultiQueue extends RuleTestBase {

	@Test
	void test_set_node_multi_queue_1_no_node() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_set_node_multi_queue_2_has_node() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}