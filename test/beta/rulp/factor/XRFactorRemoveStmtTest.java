package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveStmtTest extends RuleTestBase {

	@Test
	void test_3_varargs_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(remove-stmt m '(?...))", "'('(a b c1))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_2() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(remove-stmt m '(?x ?...))", "'('(a b c1))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_3() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(remove-stmt m '(a ?...))", "'('(a b c1))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_4() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(remove-stmt m n2:'(?...))", "'()");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_5() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");
		_test("(remove-stmt m n1:'(?...))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_3_varargs_6() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m n1:'(a b c1))");
		_test("(add-stmt m n1:'(a b c2))");
		_test("(add-stmt m n1:'(a2 b c2))");
		_test("(remove-stmt m n1:'(? ?...))", "'(n1:'(a b c1) n1:'(a b c2) n1:'(a2 b c2))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_conflict_1() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(new model m)", "m");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?x) do (remove-stmt m '(?x p1 ?y)))", "RU000");
		_test("(add-stmt m '(a p1 b) '(b p1 a))", "2");
		_test("(list-stmt m from '(?x ?y ?z))", "'('(a p1 b) '(b p1 a))");
		_test("(state-of m)", "runnable");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(start m)", "4");
		_test("(state-of m)", "completed");
		_test("(list-stmt m from '(?x ?y ?z))", "'('(b p1 a))");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_test("(gc-model m)", "4");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");

		_saveTest();
	}

	@Test
	void test_conflict_2() {

		_setup();

//		XRReteEntryTable.TRACE = true;
//		XRModel.TRACE_RETE = true;

		_test("(new model m)", "m");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?z) do (add-stmt m '(?x p1 ?z)))", "RU000");
		_test("(add-stmt m '(a p1 b) '(b p1 c) '(c p1 a))", "3");
		_test("(list-stmt m from '(?x ?y ?z))", "'('(a p1 b) '(b p1 c) '(c p1 a))");
		_test("(state-of m)", "runnable");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(start m)", "12");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)",
				"'('(a p1 b) '(b p1 c) '(c p1 a) '(a p1 c) '(b p1 a) '(c p1 b) '(a p1 a) '(b p1 b) '(c p1 c))");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?x) (not (equal ?x ?y)) do (remove-stmt m '(?x p1 ?y)))", "RU001");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c) '(c p1 a) '(a p1 a) '(b p1 b) '(c p1 c))");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");

		_test("(gc-model m)", "30");
		_mStatus(4, "m");
		_mCount(4, "m");
		_eCount(4, "m");

		_saveTest();
	}

	@Test
	void test_conflict_3() {

		_setup();

		// XRModel.TRACE_RETE = true;
		_test("(new model m)", "m");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?x) (not (equal ?x ?y)) do (remove-stmt m '(?x p1 ?y)))", "RU000");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?z) do (add-stmt m '(?x p1 ?z)))", "RU001");
		_test("(add-stmt m '(a p1 b) '(b p1 c) '(c p1 a))", "3");
		_test("(list-stmt m from '(?x ?y ?z))", "'('(a p1 b) '(b p1 c) '(c p1 a))");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(start m)", "13");
		_test("(state-of m)", "completed");
		// System.out.println(OptimizeUtil.printStatsInfo(_model("m")));
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c) '(a p1 c))");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_test("(gc-model m)", "26");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");

		_saveTest();
		// System.out.println(OptimizeUtil.printStatsInfo(_model("m")));
		_statsInfo("m", "result/factor/XRFactorRemoveStmtTest/test_conflict_3.stats.txt");
		_refInfo("m", "result/factor/XRFactorRemoveStmtTest/test_conflict_3.ref.txt");
		_dumpEntryTable("m", "result/factor/XRFactorRemoveStmtTest/test_conflict_3.dump.txt");
	}

	@Test
	void test_drop_stmt_rule_1() {

		// XRModel.TRACE_RETE = true;

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?x) (not (equal ?x ?y)) do (remove-stmt '(?x p1 ?y) '(?y p1 ?x)))",
				"RU000");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?z) do (add-stmt m '(?x p1 ?z)))", "RU001");
		_test("(add-stmt m '(a p1 b) '(b p1 c) '(c p1 a))", "3");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c) '(c p1 a))");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(start m)", "13");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c))");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_test("(gc-model m)", "29");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");

		_saveTest();

	}

	@Test
	void test_drop_stmt_rule_2() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)", "m");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?x) (not (equal ?x ?y)) do (remove-stmt ?0 ?1))", "RU000");
		_test("(add-rule m if '(?x p1 ?y) '(?y p1 ?z) do (add-stmt m '(?x p1 ?z)))", "RU001");
		_test("(add-stmt m '(a p1 b) '(b p1 c) '(c p1 a))", "3");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c) '(c p1 a))");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(start m)", "13");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(b p1 c))");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_test("(gc-model m)", "29");
		_mStatus(3, "m");
		_mCount(3, "m");
		_eCount(3, "m");

		_saveTest();

	}

	@Test
	void test_incomplete_mode() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-stmt m '(a p1 c) '(a p1 b) '(a p2 c))", "3");
		_test("(list-stmt m)", "'('(a p1 c) '(a p1 b) '(a p2 c))");

		_test("(remove-stmt m '(a p2 c))", "'('(a p2 c))");
		_test("(list-stmt m)", "'('(a p1 c) '(a p1 b))");
		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");

		_test("(gc-model m)", "1");
		_mStatus(2, "m");
		_mCount(2, "m");
		_eCount(2, "m");

		_saveTest();
	}
}
