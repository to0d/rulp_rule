package beta.rulp.scope;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ModelVarTest extends RuleTestBase {

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
