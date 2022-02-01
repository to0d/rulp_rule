package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class TestEton extends RuleTestBase {

	@Test
	void test_eton_1_daily_rule_1() {

		_setup();
		_run_script();
		_statsInfo("em");
	}

}
