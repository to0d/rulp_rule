package beta.rulp.partial;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class PartialReasonTest extends RuleTestBase {

	@Test
	void test_1_full() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x typeof c1) do (-> m '(?x typeof c2)) )", "R1");
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )", "R2");
		_test("(add-stmt m '(a typeof c1))", "true"); // used for R1
		_test("(add-stmt m '(a p b))", "true"); // used for R2
		_test("(add-stmt m '(b p c))", "true"); // used for R2

		_test("(start m)", "13");
		_test("(state-of m)", "completed");

		_test("(list-stmt m)", "'('(a typeof c1) '(a p b) '(b p c) '(a typeof c2) '(a p c))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");

		_saveTest();
	}

	@Test
	void test_1_full_b() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x typeof c1) do (-> m '(?x typeof c2)) )", "R1");
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )", "R2");
		_test("(add-stmt m '(a typeof c1))", "true"); // used for R1
		_test("(add-stmt m '(a p b))", "true"); // used for R2
		_test("(add-stmt m '(b p c))", "true"); // used for R2

		_test("(start m priority -1 limit 5)", "5");
		_test("(state-of m)", "runnable");

		_test("(start m)", "8");
		_test("(state-of m)", "completed");

		_test("(start m)", "0");
		_test("(state-of m)", "completed");

		_test("(list-stmt m)", "'('(a typeof c1) '(a p b) '(b p c) '(a typeof c2) '(a p c))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");

		_saveTest();
	}

	@Test
	void test_2_partial_r2_not_work() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x typeof c1) do (-> m '(?x typeof c2)) )", "R1");
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )", "R2");
		_test("(add-stmt m '(a typeof c1))", "true"); // used for R1
		_test("(add-stmt m '(a p b))", "true"); // used for R2
		_test("(add-stmt m '(b p c))", "true"); // used for R2

		_test("(list-stmt m from '(?x typeof c2))", "'()");
		_test("(list-source-node m ('(?n typeof c2)))", "'(R1)");
		_test("(query-stmt m '(?n) from '(?n typeof c2))", "'('(a))");
		_test("(list-stmt m)", "'('(a typeof c1) '(a p b) '(b p c) '(a typeof c2))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");

		_saveTest();
	}

	@Test
	public void test_3_alias_full() {

		_setup();

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(tc nm:hasAliasTag td))");
		_test("(start p2d)", "277");
		_test("(state-of p2d)", "completed");
		_test("(list-stmt p2d from '(tb nm:beAliasTo ?x))", "'('(tb nm:beAliasTo ta))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_4_alias_query_all() {

		_setup();

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(tc nm:hasAliasTag td))");
		_test("(query-stmt p2d '(?x) from '(tb nm:beAliasTo ?x))", "'('(ta))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");

		_saveTest();
	}

	@Test
	public void test_5_alias_query_1() {

		_setup();

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(tc nm:hasAliasTag td))");
		_test("(query-stmt p2d '(?x) from '(tb nm:beAliasTo ?x) limit 1)", "'('(ta))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest(); 
	}

	@Test
	void test_6_root_based_rule() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-rule m if '(?x ?y ?z) do (-> '(?z ?y ?x)))");
		_test("(add-stmt m '(x y z))");

		_test("(query-stmt m '(?x ?y ?z) from '(?x ?y ?z))", "'('(x y z) '(z y x))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/partial/PartialReasonTest/test_6_root_based_rule.txt");
	}
}
