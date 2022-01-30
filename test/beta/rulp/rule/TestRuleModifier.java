package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestRuleModifier extends RuleTestBase {

	@Test
	void test_rule_modifier_1_entry_order_1_a() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_rule_modifier_1_entry_order_1_b() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_rule_modifier_1_entry_order_1_c() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
