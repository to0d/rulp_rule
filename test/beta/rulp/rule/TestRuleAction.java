package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestRuleAction extends RuleTestBase {

	@Test
	void test_rule_action_0_external_var() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
