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
}
