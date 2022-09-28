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
	void test_uniq_constraint_1_add_3_conflict_a() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_1_add_3_conflict_b() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_1_add_3_conflict_c() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_1_add_3_conflict_d() {

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

	@Test
	void test_uniq_constraint_3_has_stmt_2_var_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_3_has_stmt_3_order_by_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_3_has_stmt_3_order_by_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_4_list_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_4_list_stmt_2_var_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_5_uniq_info_1_alpha() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_uniq_constraint_6_query_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
