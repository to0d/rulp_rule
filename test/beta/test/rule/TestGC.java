package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestGC extends RuleTestBase {

	@Test
	void test_gc_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
