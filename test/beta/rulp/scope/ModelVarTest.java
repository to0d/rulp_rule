package beta.rulp.scope;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ModelVarTest extends RuleTestBase {

	@Test
	void test_1_int_var_1() {

		_setup();
		_test("(new scope s)");
		_test("(s::define ?age int 37 40)", "&?age");
		_test("(s::assert (= (% ?age 2) 0))");
		_test("(s::query '(?age) '(limit 1))", "'('(38))");

		_smCount(1, "s");
		_scopeInfo("s", "test/beta/rulp/scope/var_test_1_int_var_1.txt");

		_saveTest();
	}

	@Test
	void test_1_int_var_2() {

		_setup();

		_test("(new scope s)");
		_test("(s::define ?age int 40 37)", "&?age");
		_test("(s::assert (= (% ?age 2) 0))");
		_test("(s::query '(?age))", "'('(40) '(38))");

		_smCount(1, "s");
		_scopeInfo("s", "test/beta/rulp/scope/var_test_1_int_var_2.txt");

		_saveTest();
	}

	@Test
	void test_1_int_var_3() {

		_setup();
		_test("(new scope s)");
		_test("(s::define ?age int 37 40)", "&?age");
		_test("(s::assert (= (% ?age 2) 0))");
		_test("(s::query '(?age))", "'('(38) '(40))");

		_smCount(1, "s");
		_scopeInfo("s", "test/beta/rulp/scope/var_test_1_int_var_3.txt");

		_saveTest();
	}

	@Test
	void test_1_int_var_4() {

		_setup();

		// XRRModelVarList.TRACE = true;

		_test("(new scope s)");
		_test("(s::define ?x int 1 10)", "&?x");
		_test("(s::define ?y int 1 10)", "&?y");
		_test("(s::define ?z int 1 10)", "&?z");

		// x^2 + y^2 = z^2
		_test("(s::assert (>= ?y ?x))");
		_test("(s::assert (>= ?z ?y))");
		_test("(s::assert (= (+ (^ ?x 2) (^ ?y 2)) (^ ?z 2)))");

		_test("(s::query '(?x ?y ?z))", "'('(3 4 5) '(6 8 10))");

		_smCount(1, "s");
		_scopeInfo("s", "test/beta/rulp/scope/var_test_1_int_var_4.txt");

		_saveTest();
	}

	@Test
	void test_1_int_var_5() {

		_setup();

		// XRRModelVarList.TRACE = true;

		_test("(new scope s)");
		_test("(s::define ?x int 1 10)", "&?x");
		_test("(s::define ?y int 1 10)", "&?y");
		_test("(s::define ?z int 1 10)", "&?z");

		// x^2 + y^2 = z^
		_test("(s::assert (= (+ (^ ?x 2) (^ ?y 2)) (^ ?z 2)))");
		_test("(s::query '(?x ?y ?z))", "'('(4 3 5) '(3 4 5) '(8 6 10) '(6 8 10))");

		_smCount(1, "s");
		_scopeInfo("s", "test/beta/rulp/scope/var_test_1_int_var_5.txt");

		_saveTest();
	}

	@Test
	void test_2_atom_var_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a ageIs 18))");
		_test("(add-stmt m '(b ageIs 20))");
		_test("(add-stmt m '(c ageIs 24))");

		_test("(query-stmt m '(?x ?y ?z) from '(?x ageIs ?xn) '(?y ageIs ?yn) '(?z ageIs ?zn) (> ?xn ?yn) (> ?yn ?zn) limit 1)",
				"'('(c b a))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_statsInfo("m", "test/beta/rulp/scope/var_test_2_atom_var_1.txt");
		_saveTest();
	}

	@Test
	void test_2_atom_var_2() {

		_setup();

		_test("(new model m)");
		_test("(new scope s '(m))");

		_test("(add-stmt m '(a ageIs 18))");
		_test("(add-stmt m '(b ageIs 20))");
		_test("(add-stmt m '(c ageIs 24))");

		_test("(s::define ?x '(a b c))", "&?x");
		_test("(s::define ?y '(a b c))", "&?y");
		_test("(s::define ?z '(a b c))", "&?z");

		_test("(s::assert ('(?y ageIs ?yn) '(?z ageIs ?zn) (> ?yn ?zn)))");
		_test("(s::assert ('(?x ageIs ?xn) '(?y ageIs ?yn)) (> ?xn ?yn))");

		_test("(s::query '(?z ?y ?z) limit 1)", "'('(38))");

		_smCount(1, "s");
		_eCount(1, "m");
		_mStatus(1, "m");
		_statsInfo("s", "test/beta/rulp/scope/var_test_2_atom_var_2.txt");
		_saveTest();
	}

}
