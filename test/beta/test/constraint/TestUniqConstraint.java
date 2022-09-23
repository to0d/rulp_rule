package beta.test.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestUniqConstraint extends RuleTestBase {

	@Test
	void test_uniq_constraint_1_add_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_1_add_2_change_queue() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_2_remove_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_uniq_constraint_2_remove_2() {

		_setup();
		_run_script();
	}

	@Test
	void test_uniq_constraint_2_remove_3() {

		_setup();
		_run_script();
	}

	@Test
	void test_uniq_constraint_2_remove_4() {

		_setup();
		_run_script();
	}

	@Test
	void test_uniq_constraint_3_has_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
