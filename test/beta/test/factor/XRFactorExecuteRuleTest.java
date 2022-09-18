package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorExecuteRuleTest extends RuleTestBase {

	@Test
	void test_exec_rule_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

}
