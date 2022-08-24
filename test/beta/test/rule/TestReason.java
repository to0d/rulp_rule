package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestReason extends RuleTestBase {

	@Test
	void test_3_circle_reason_a() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_3_circle_reason_b_named() {

		// Should fail

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
