package beta.rulp.rbs;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class TestSQL1 extends RuleTestBase {

	@Test
	void test_sql_1_create_table_1() {

		_setup();
		_clean_model_cache();

		_test("(use namespace rbs)");
		_test("(new model m)");
		_test("(set schema m)");
		_test("(create table t1 {c1 int, c2 bool, c3 float})", "0");

		_statsInfo("m");
		_save_model_cache("m");
	}

	@Test
	void test_sql_2_insert_1() {

		_setup();
		_clean_model_cache();

		_test("(use namespace rbs)");
		_test("(new model m)");
		_test("(set schema m)");
		_test("(create table t1 {c1 int, c2 bool, c3 float})", "0");
		_test("(insert into t1 values (1, true, 1.5))", "0");
//		_test("(insert into t1 (c1, c3, c2) values (2, 1.5, false))", "0");
//		_test("(select * from t1)", "");

		_statsInfo("m");
		_save_model_cache("m");
	}
}
