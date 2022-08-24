package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestSubscript extends RuleTestBase {

	@Test
	void test_sub_0() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
