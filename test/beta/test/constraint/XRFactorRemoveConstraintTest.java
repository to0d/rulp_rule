package beta.test.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveConstraintTest extends RuleTestBase {

	@Test
	void test_remove_constraint_1_any_1() {

		_setup();
		_run_script();

	}

	@Test
	void test_remove_constraint_1_any_2() {

		_setup();
		_run_script();
	}

	@Test
	void test_remove_constraint_2_type_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_remove_constraint_2_type_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_remove_constraint_2_type_3() {

		_setup();
		_run_script();
	}

	@Test
	void test_remove_constraint_2_type_4() {

		_setup();
		_run_script();
	}

}
