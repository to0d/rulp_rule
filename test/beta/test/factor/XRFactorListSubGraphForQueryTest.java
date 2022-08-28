package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorListSubGraphForQueryTest extends RuleTestBase {

	@Test
	void test_list_subgraph_for_query_1_backward() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_list_subgraph_for_query_1_forward() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
