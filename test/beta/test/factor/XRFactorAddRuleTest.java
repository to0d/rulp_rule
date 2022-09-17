package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddRuleTest extends RuleTestBase {

	@Test
	void test_add_rule_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_rule_2_beta3_1() {

		_setup();
		_run_script();

	}

	@Test
	void test_add_rule_2_beta3_2a() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_rule_2_beta3_2b() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_rule_2_beta3_2c() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_rule_2_beta3_3() {

		_setup();
		_run_script();

	}

	@Test
	void test_add_rule_2_beta3_4() {

		_setup();
		_run_script();

	}

	@Test
	void test_add_rule_2_beta3_5() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_rule_3_condtion() {

		_setup();
		_run_script();
	}

	@Test
	void test_add_rule_4_match_1() {

		_setup();
		_run_script();

	}

	@Test
	void test_add_rule_4_match_2() {

		_setup();
		_run_script();

	}

	@Test
	void test_add_rule_5_constant() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
	
	@Test
	void test_add_rule_6_zeta0_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_add_rule_6_zeta0_2() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_add_rule_6_zeta0_3() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_add_rule_6_zeta0_4() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
