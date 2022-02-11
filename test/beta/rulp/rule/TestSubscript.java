package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestSubscript extends RuleTestBase {

	@Test
	void test_sub_0() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
