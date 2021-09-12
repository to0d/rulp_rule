package beta.rulp.rbs;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class TestSQL1 extends RuleTestBase {

	@Test
	void test_1_create_table_1() {

		_setup();
		_clean_cache("result/rbs/TestSQL1/test_1_create_table_1");
		_test("(use namespace rbs)");
		_test("(new model m)");
		_test("(set schema m)");
		_test("(create table name1 {c1 int, c2 bool, c3 float})", "0");
		_statsInfo("m", "result/rbs/TestSQL1/test_1_create_table_1.txt");
		_test("(set-model-cache-path m \"result/rbs/TestSQL1/test_1_create_table_1\")");
		_test("(save-model m)");
	}

}
