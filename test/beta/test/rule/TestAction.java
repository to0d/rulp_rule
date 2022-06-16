package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestAction extends RuleTestBase {

	@Test
	void test_action_1_index_var() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
