package beta.test.factor;

import static org.junit.Assert.fail;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;

class XRFactorAddStmtTest extends RuleTestBase {

	@Test
	void test_add_stmt_1_atom() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_2_int() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_3_float() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_4_string_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_4_string_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_4_string_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_5_long() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_6_double() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_7_bool() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_8_nil_a() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_8_nil_b() {

		_setup();
		_test("(new model m)", "m");

		try {
			_model("m").addStatement(
					RulpFactory.createList(RulpFactory.createAtom("a"), RulpFactory.createAtom("b"), null));
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a b nil))", "false");
		_test("(list-stmt m)", "'('(a b nil))");
		_statsInfo("m");
	}

	@Test
	void test_add_stmt_8_nil_c() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_9_fun_a() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_add_stmt_a_list_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_stmt_b_variable_length_entry() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_add_stmt_c_max_stmt_len_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
