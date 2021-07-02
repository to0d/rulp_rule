package beta.rulp.worker;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddLazyStmtTest extends RuleTestBase {

	@Test
	void test_1_load_twice() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(c typeof node))");
		_test("(list-stmt m from '(?x typeof node))", "'('(c typeof node))");
		_test("(add-lazy-stmt m '(?a typeof node) '('(a typeof node) '(b typeof node)))");
		_test("(query-stmt m ?x from '(?x typeof node))", "'(c a b)");
		_test("(query-stmt m ?x from '(?x typeof node))", "'(c a b)");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_2_run_without_load_stmt() {

		_setup();
		_test("(new model m)");
		_test("(add-lazy-stmt m '(?x typeof ?y) '('(a typeof node) '(b typeof node) '(c typeof tag)))");
		_test("(add-lazy-stmt m '(?x p ?y) '('(a p b)))");
		_test("(add-stmt m '(node typeof obj))");
		_test("(add-rule m if '(?a typeof ?b) '(?b typeof ?c) do (-> m '(?a typeof2 ?c)) )");
		_mStatus(1, "m");

		_test("(list-stmt m)", "'('(node typeof obj))");
		_mStatus(2, "m");

		_test("(start m)");
		_test("(list-stmt m)", "'('(node typeof obj))");
		_test("(list-stmt m state defined)", "'('(node typeof obj))");
		_test("(list-stmt m state reasoned)", "'()");
		_test("(list-stmt m state assumed)", "'()");
		_test("(list-stmt m state removed)", "'()");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");
		_saveTest();
	}

	@Test
	public void test_3_run_with_load_stmt() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-lazy-stmt m '(?x typeof ?y) '('(a typeof node) '(b typeof node) '(c typeof tag)))");
		_test("(add-lazy-stmt m '(?x p ?y) '('(a p b)))");
		_test("(add-stmt m '(node typeof obj))");
		_test("(add-rule m if '(?a typeof ?b) '(?b typeof ?c) do (-> m '(?a typeof2 ?c)) )");
		_mStatus(1, "m");

		_test("(list-stmt m)", "'('(node typeof obj))");
		_mStatus(2, "m");

		_test("(start m)");
		_test("(list-stmt m)", "'('(node typeof obj))");
		_test("(query-stmt m ?x from '(?x typeof ?))", "'(node a b c)");
		_test("(query-stmt m ?x from '(?x typeof2 ?))", "'(a b)");

		_test("(list-stmt m state defined)", "'('(node typeof obj) '(a typeof node) '(b typeof node) '(c typeof tag))");
		_test("(list-stmt m state reasoned)", "'('(a typeof2 obj) '(b typeof2 obj))");
		_test("(list-stmt m state assumed)", "'()");
		_test("(list-stmt m state removed)", "'()");
		_mStatus(4, "m");
		_mCount(4, "m");
		_eCount(4, "m");

		_saveTest();
	}

	@Test
	public void test_4_variable_length_entry() {

		_test("(new model m)");
		_test("(add-lazy-stmt m '(?n hasRelated ?key ?path) '('(a hasRelated k1 \"path 1\") "
				+ "'(a hasRelated k2 \"path 2\") '(a hasRelated k3 \"path 3\") '(b hasRelated k2 \"path 4\")))");
		_test("(query-stmt m '(?n ?k ?p) from '(?n hasRelated ?k ?p))",
				"'('(a k1 \"path 1\") '(a k2 \"path 2\") '(a k3 \"path 3\") '(b k2 \"path 4\"))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}
}
