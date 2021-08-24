package beta.rulp.sql;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorCreateTableTest extends RuleTestBase {

	@Test
	void test_1_create_table_1() {

		_setup();

		_clean_cache("result/sql/XRFactorCreateTableTest/test_1_create_table_1");

		// XRModel.TRACE_RETE = true;
		_test("(new model m)");
		_test("(set-default-model m)");
		_test("(create table name1 {c1 int, c2 bool, c3 float})", "0");
		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_1_create_table_1.txt");
		_test("(set-model-cache-path m \"result/sql/XRFactorCreateTableTest/test_1_create_table_1\")");
		_test("(save-model m)");
	}

	@Test
	void test_1_new_2() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'(? ? ?))", "name1:'(?0 ?1 ?2)");
		_test("(add-table m name1:'(? ? ?))", "name1:'(?0 ?1 ?2)");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_1_new_2.txt");
	}

	@Test
	void test_2_type_1_atom() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'('(type atom) ? ?))", "");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_2_type_1_atom.txt");
	}

	@Test
	void test_2_type_2_int() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'(? ? '(? type int)))", "");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_2_type_2_int.txt");
	}

	@Test
	void test_3_constraint_1_uniq() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'('(uniq) ? ?))", "");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_3_constraint_1_uniq.txt");
	}

	@Test
	void test_3_constraint_2_not_null() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'('(not null) ? ?))", "");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_3_constraint_2_not_null.txt");
	}

	@Test
	void test_3_constraint_3_limit() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-table m name1:'(? ? ?) limit 3)", "");

		_statsInfo("m", "result/sql/XRFactorCreateTableTest/test_3_constraint_3_limit.txt");
	}

}
