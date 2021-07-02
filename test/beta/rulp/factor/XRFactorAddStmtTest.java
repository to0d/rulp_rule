package beta.rulp.factor;

import static org.junit.Assert.fail;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;

class XRFactorAddStmtTest extends RuleTestBase {

	@Test
	void test_1_atom() {

		_setup();
		_test("(new model m)", "m");
		_test("(type-of m)", "instance");
		_test("(name-of m)", "\"m\"");
		_test("(add-stmt m '(a p1 c))", "1");
		_test("(add-stmt m '(a p1 c))", "0"); // duplicated
		_test("(add-stmt m '(a p1 b))", "1");
		_test("(add-stmt m '(a p2 c))", "1");
		_test("(list-stmt m from '(a ?x c))", "'('(a p1 c) '(a p2 c))");
		_mStatus(1, "m");
		_saveTest();

	}

	@Test
	void test_2_int() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 1))", "1");
		_test("(add-stmt m '(a p1 1))", "0");
		_test("(list-stmt m from '(a p1 ?x))", "'('(a p1 1))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_float() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 1.1))", "1");
		_test("(add-stmt m '(a p1 1.1))", "0");
		_test("(list-stmt m from '(a p1 ?x))", "'('(a p1 1.1))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_string_1() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 \"abc\"))", "1");
		_test("(add-stmt m '(a p1 \"abc\"))", "0");
		_test("(list-stmt m)", "'('(a p1 \"abc\"))");
		_mStatus(1, "m");
		_saveTest();
	}

	void test_3_string_2() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(\"typeOf\" \"/note-Ë½ÈË/My Links/My Links.md\" \"note\"))", "'(a p1 \"abc\")");
		_test("(list-stmt m)", "'('(a p1 \"abc\"))");
		_mStatus(1, "m");
		_saveTest();
	}

//	@Test
//	void test_3_string_3() {
//
//		_setup();
//		_test("(new model m)", "m");
//		_test("(add-stmt m '(\"abc\" a b))", "1");
//		_test("(add-stmt m '(a p1 \"abc\"))", "0");
//		_test("(list-stmt m)", "'('(a p1 \"abc\"))");
//		_mStatus(1, "m");
//		_saveTest();
//	}

	@Test
	void test_3_string_3() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(nt:\"a id\" a b))", "1");
		_test("(list-stmt m)", "'('(nt:\"a id\" a b))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_string_4() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m name1:'(\"a string\"))");
		_test("(list-stmt m)", "'(name1:'(\"a string\"))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_4_long() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 1L))", "1");
		_test("(add-stmt m '(a p1 1L))", "0");
		_test("(list-stmt m from '(a p1 ?x))", "'('(a p1 1L))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_5_double() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 1.1D))", "1");
		_test("(add-stmt m '(a p1 1.1D))", "0");
		_test("(list-stmt m from '(a p1 ?x))", "'('(a p1 1.1D))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_6_variable_length_entry() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 a))", "1");
		_test("(add-stmt m '(a p1 a b))", "1");
		_test("(add-stmt m '(a p1 a b c))", "1");
		_mStatus(1, "m");

		_test("(list-stmt m from '(a p1 ?x))", "'('(a p1 a))");
		_mStatus(2, "m");

		_test("(list-stmt m from '(a p1 ?x ?y))", "'('(a p1 a b))");
		_mStatus(3, "m");

		_test("(list-stmt m from '(a p1 ?x ?y ?z))", "'('(a p1 a b c))");
		_mStatus(4, "m");

		_test("(list-stmt m from '(a p1 ?x ?y ?z ?d))", "'()");
		_mStatus(5, "m");

		_test("(add-stmt m '(a b))", "1");
		_test("(list-stmt m from '(a ?x))", "'('(a b))");
		_mStatus(6, "m");

		_test("(add-stmt m '(c))", "1");
		_test("(list-stmt m from '(?x))", "'('(c))");
		_mStatus(7, "m");

		_saveTest();
	}

	@Test
	void test_7_bool() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a b true))", "1");
		_test("(add-stmt m '(a b true))", "0");
		_test("(add-stmt m '(a b c false))", "1");
		_test("(list-stmt m)", "'('(a b true) '(a b c false))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_8_nil_a() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a b nil))", "1");
		_test("(add-stmt m '(a b nil))", "0");
		_test("(list-stmt m)", "'('(a b nil))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_8_nil_b() {

		_setup();
		_test("(new model m)", "m");

		try {
			_model("m").addStatement(
					RulpFactory.createList(RulpFactory.createAtom("a"), RulpFactory.createAtom("b"), null));
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a b nil))", "0");
		_test("(list-stmt m)", "'('(a b nil))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_8_nil_c() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m name1:'(a nil b))", "1");
		_test("(add-stmt m name1:'(a nil b))", "0");
		_test("(list-stmt m)", "'(name1:'(a nil b))");
		_mStatus(1, "m");
		_saveTest();
	}
	
	@Test
	void test_9_fun_a() {
		
		_setup();
		_test("(defun f1 (?x) (setq ?x1 ?x))");
		_test("(new model m)", "m");
		_test("(add-stmt m name1:'(f1 9))");
		_test("(add-stmt m '(f1 a))");
	}
}
