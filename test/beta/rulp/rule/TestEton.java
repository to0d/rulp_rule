package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestEton extends RuleTestBase {

	@Test
	public void test_eton_1_daily_rule_1() {

		_setup();
		_run_script();
		_statsInfo("eton-model");
	}

}
