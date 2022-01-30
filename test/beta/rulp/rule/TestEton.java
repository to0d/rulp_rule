package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestEton extends RuleTestBase {

	@Test
	public void test_eton_1_rule_priority_a() {

		_setup();
		_run_script();
	}
	
	@Test
	public void test_eton_1_rule_priority_b() {

		_setup();
		_run_script();
	}
}
