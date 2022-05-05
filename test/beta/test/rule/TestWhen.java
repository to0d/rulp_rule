package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestWhen extends RuleTestBase {

	@Test
	void test_when_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

}
