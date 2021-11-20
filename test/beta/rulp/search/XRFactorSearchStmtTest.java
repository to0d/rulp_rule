package beta.rulp.search;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorSearchStmtTest extends RuleTestBase {

	@Test
	void test_1_int_var_1_a() {

		_setup();
		_test("(new model m)");
		_test("(add-constraint m v1:'(?x) '(type int on ?x) '(max 40 on ?x) '(min 37 on ?x) (= (% ?x 2) 0))");
		_test("(defvar ms (search-model m v2:'(?x) from v1:'(?x) limit 1 order by ?x asc))", "completed");
		_test("(state-of ms)", "runnable");
		_test("(start ms)", "completed");
		_test("(list-stmt m from v2:'(?x))", "'()");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

}
