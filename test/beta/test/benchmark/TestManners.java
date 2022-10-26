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

		// Should fail

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

	@Test
	void test_manners_2_query_manners_4_a() {

		_setup();
		_run_script();
		_statsInfo("mm");

	}

	@Test
	void test_manners_2_query_manners_4_b() {

		_setup();
		_run_script();
		_statsInfo("mm");

	}

	@Test
	void test_manners_2_query_manners_4_c_func_entry() {

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

	@Test
	void test_manners_2_query_manners_4_d_func_entry_and_fix_stmt() {

		_setup();
		_run_script();
		_statsInfo("mm");
		_dumpEntryTable("mm");

	}

	@Test
	void test_manners_2_query_manners_4_e_query_init_and_gc() {

		_setup();
		_run_script();
	}
}
