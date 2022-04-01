package beta.test.partial;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ConflictRuleTest extends RuleTestBase {

	@Test
	void test_confilct_rule_1_without_priority() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_confilct_rule_2_with_priority() {

		_setup();
		_run_script();

	}
}
