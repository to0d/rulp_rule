package beta.rulp.optimize;

import org.junit.Test;

import alpha.rulp.utils.RuleTestBase;

public class OptimizeTest2 extends RuleTestBase {

	@Test
	public void test_tag_alias_1_full_with_single_child_optimize() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tc))");

		_nodeInfo("p2d", "result/optimize/OptimizeTest2/p2d_alias_1_full_single_child_optimize_n1.txt");
		_test("(opt-model p2d)", "1");
		_nodeInfo("p2d", "result/optimize/OptimizeTest2/p2d_alias_1_full_single_child_optimize_n2.txt");

		_test("(start p2d)", "302");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasAliasTag ?y))", "'('(ta nm:hasAliasTag tb) '(ta nm:hasAliasTag tc))");
		_test("(list-stmt p2d from '(?x nm:beAliasTo ?y))", "'('(tb nm:beAliasTo ta) '(tc nm:beAliasTo ta))");
		_test("(list-stmt p2d from '(?x nm:typeOf nm:tag))",
				"'('(ta nm:typeOf nm:tag) '(tb nm:typeOf nm:tag) '(tc nm:typeOf nm:tag))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();

		_statsInfo("p2d", "result/optimize/OptimizeTest2/p2d_alias_1_full_single_child_optimize.txt");
		_dumpEntryTable("p2d", "result/optimize/OptimizeTest2/p2d_alias_1_full_single_child_optimize.entry.dump.txt");
	}
}
