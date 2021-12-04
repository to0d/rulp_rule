package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorListStmtTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();
		_test("(new model m)");

		_mCount(1, "m");
		_eCount(1, "m");
		_test("(add-stmt m '(a1 p1 b1))");
		_test("(add-stmt m '(a1 p1 b2))");
		_test("(add-stmt m '(a1 p1 b3))");
		_test("(add-stmt m '(a2 p1 b1))");
		_test("(add-stmt m '(a2 p1 b2))");
		_test("(add-stmt m '(a3 p2 b1))");
		_test("(add-stmt m '(a3 p2 b2))");
		_test("(add-stmt m '(a3 p3 b3))");

		_test("(list-stmt m)",
				"'('(a1 p1 b1) '(a1 p1 b2) '(a1 p1 b3) '(a2 p1 b1) '(a2 p1 b2) '(a3 p2 b1) '(a3 p2 b2) '(a3 p3 b3))");
		_mCount(2, "m");
		_eCount(2, "m");
		_test("(size-of m)", "8");

		_test("(list-stmt m from '(a1 ?x ?y))", "'('(a1 p1 b1) '(a1 p1 b2) '(a1 p1 b3))");
		_mCount(3, "m");
		_eCount(3, "m");

		_test("(list-stmt m from '(a ?x ?y))", "'()");
		_mCount(4, "m");
		_eCount(4, "m");

		_test("(list-stmt m from '(?x p1 ?y))", "'('(a1 p1 b1) '(a1 p1 b2) '(a1 p1 b3) '(a2 p1 b1) '(a2 p1 b2))");
		_mCount(5, "m");
		_eCount(5, "m");

		_test("(list-stmt m from '(?x p ?y))", "'()");
		_mCount(6, "m");
		_eCount(6, "m");

		_test("(list-stmt m from '(?x ?y b3))", "'('(a1 p1 b3) '(a3 p3 b3))");
		_mCount(7, "m");
		_eCount(7, "m");

		_test("(list-stmt m from '(?x ?y b))", "'()");
		_mCount(8, "m");
		_eCount(8, "m");

		_saveTest();
	}

	@Test
	void test_2() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a p b))", "true");
		_test("(add-stmt m '(b p c))", "true");
		_test("(add-rule m if '(?a ?p ?b) '(?b ?p ?c) do (-> m '(?a ?p ?c)) )");

		_test("(list-stmt m)", "'('(a p b) '(b p c))");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(size-of m)", "2");
		_mStatus(1, "m");

		_test("(start m)");
		_test("(list-stmt m)", "'('(a p b) '(b p c) '(a p c))");
		_test("(size-of m)", "3");
		_mCount(2, "m");
		_eCount(2, "m");
		_mStatus(2, "m");

		_test("(list-stmt m state defined)", "'('(a p b) '(b p c))");
		_test("(list-stmt m state reasoned)", "'('(a p c))");
		_test("(list-stmt m state assumed)", "'()");
		_test("(list-stmt m state removed)", "'()");
		_test("(list-stmt m state defined reasoned)", "'('(a p b) '(b p c) '(a p c))");
		_mCount(3, "m");
		_eCount(3, "m");
		_mStatus(3, "m");

		_saveTest();
	}

	@Test
	void test_3_varargs_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a))");
		_test("(add-stmt m '(a b))");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");

		_test("(list-stmt m from '(?...))", "'('(a) '(a b) '(a b c1) '(a b c2))");
		_test("(list-stmt m from '(?x ?...))", "'('(a) '(a b) '(a b c1) '(a b c2))");
		_test("(list-stmt m from '(a ?...))", "'('(a) '(a b) '(a b c1) '(a b c2))");
		_test("(list-stmt m from '(?x ?y ?...))", "'('(a b) '(a b c1) '(a b c2))");
		_test("(list-stmt m from n1:'(?...))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");
		_test("(list-stmt m from n2:'(?...))", "'()");
		_test("(list-stmt m from n1:'(?x ?...))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");
		_test("(list-stmt m from n1:'(a ?...))", "'(n1:'(a b c1) n1:'(a b c2))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();

		_statsInfo("m", "result/factor/XRFactorListStmtTest/test_3_varargs_1.txt");
	}

	@Test
	void test_3_varargs_2() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");

		_test("(list-stmt m from '(? ? c1))", "'('(a b c1))");
		_test("(list-stmt m from '(?x ? ?))", "'('(a b c1) '(a b c2))");
		_test("(list-stmt m from '(a ? ?))", "'('(a b c1) '(a b c2))");
		_test("(list-stmt m from n1:'(? ? c1))", "'(n1:'(a b c1))");
		_test("(list-stmt m from n1:'(?x ? ?))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");
		_test("(list-stmt m from n1:'(a ? ?))", "'(n1:'(a b c1) n1:'(a b c2))");
		_test("(list-stmt m from n1:'(?x ? ?...))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");
		_test("(list-stmt m from n1:'(a ? ?...))", "'(n1:'(a b c1) n1:'(a b c2))");
		_test("(list-stmt m from n1:'(a ?))", "'()");
		_test("(list-stmt m from n2:'(? ?))", "'()");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m", "result/factor/XRFactorListStmtTest/test_3_varargs_2.txt");
	}

	@Test
	void test_4_limit_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a))");
		_test("(add-stmt m '(a b))");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2))");

		_test("(list-stmt m from '(a ?...) limit 1)", "'('(a))");
		_test("(list-stmt m from '(a ?...) limit 3)", "'('(a) '(a b) '(a b c1))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m", "result/factor/XRFactorListStmtTest/test_4_limit_1.txt");
	}

}