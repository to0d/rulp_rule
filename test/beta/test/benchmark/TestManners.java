package beta.test.benchmark;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestManners extends RuleTestBase {

	@Test
	void test_manners_1_bs_can_sit_a() {

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

	@Test
	void test_manners_1_bs_can_sit_b() {

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

}
