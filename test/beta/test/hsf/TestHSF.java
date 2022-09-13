package beta.test.hsf;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestHSF extends RuleTestBase {

	@Test
	void test_hsf_1_int_var_1() {

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

}
