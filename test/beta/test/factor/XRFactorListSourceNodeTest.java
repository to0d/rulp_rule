package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorListSourceNodeTest extends RuleTestBase {

	@Test
	void test_list_source_node_1_alpha() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_2_root() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_3_entry_len() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_4_root_named() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_5_alpha_named() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_6_1_in_do_expr() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
	
	@Test
	void test_list_source_node_6_2_in_if_expr() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
	
	@Test
	void test_list_source_node_6_2_in_loop_expr_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_7_remove_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_8_special_expr_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_9_fix_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_a_assume_stmt() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_b_constant() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_c_p2d() {

		_setup();
		_run_script();
		_statsInfo("p2d");
	}

	@Test
	void test_list_source_node_d_vary_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_source_node_d_vary_2_manner() {

		_setup();
		_run_script();
		_statsInfo("mm");
	}
}
