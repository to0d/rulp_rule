package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorHasStmtTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(a p1 b))");
		_test("(has-stmt m '(a p1 b))", "true");
		_test("(has-stmt m '(a ?x b))", "true");
		_test("(has-stmt m '(a p2 b))", "false");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_saveTest();
	}

	@Test
	void test_2() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a1 p1 b))");
		_test("(has-stmt m '(?x p1 b))", "true");

		_test("(add-stmt m '(a2 p1 b))");
		_test("(has-stmt m '(?x p1 b))", "true");

		// How to let A001 node be partial executed (Define count is 1)?
		_statsInfo("m", "result/factor/XRFactorHasStmtTest/test_2.txt");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_saveTest();
	}

	@Test
	void test_3() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?x p1 ?y) do (if (has-stmt '(?y p1 ?x)) (-> m '(?x p2 ?y)) ))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(b p1 a))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 a) '(a p2 b) '(b p2 a))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_saveTest();
	}

	@Test
	void test_4() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m named1:'(\"a\"))");
		_test("(has-stmt m named1:'(\"a\"))", "true");

		_test("(add-stmt m named2:'(a1 p1 b))");
		_test("(has-stmt m named2:'(?x p1 b))", "true");

		_test("(add-stmt m named2:'(a2 p1 b))");
		_test("(has-stmt m named2:'(?x p1 b))", "true");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_saveTest();
	}

	@Test
	void test_5() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");
//		_test("(has-stmt m '(?...))", "true");
//		_test("(has-stmt m '(?x ?...))", "true");
		_test("(has-stmt m '(?x c d))", "false");
		_test("(has-stmt m '(a ?...))", "true");
		_test("(has-stmt m n1:'(?...))", "true");
		_test("(has-stmt m n1:'(?x ?...))", "true");
		_test("(has-stmt m n1:'(a ?...))", "true");
		_test("(has-stmt m n2:'(?...))", "false");
		_test("(has-stmt m n1:'(\"t1\" ?...))", "false");
	}

	@Test
	void test_6_uniq_1() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");
		_test("(has-stmt m n1:'(a b c1))", "true");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}
}
