package beta.test.named;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class Named1Test extends RuleTestBase {

	@Test
	void test_named_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_named_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

}
