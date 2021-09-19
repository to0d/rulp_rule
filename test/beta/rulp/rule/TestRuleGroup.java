package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestRuleGroup extends RuleTestBase {

	@Test
	void test_rule_group_0() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule m::g1 if '(?a p ?b) do (-> m '(?a p2 ?b)))", "RU000");
		_test("(add-stmt m '(a p b))", "1");
		_test("(list-stmt m)", "'('(a p b))");

		_test("(start m)");
		_test("(list-stmt m)", "'('(a p b))");
		_mStatus(1, "m");
		_statsInfo("m", "result/rule/TestRuleGroup/test_rule_group_0a.txt");

		_test("(start m::g1)");
		_test("(list-stmt m)", "'('(a p b) '(a p2 b))");
		_mStatus(2, "m");
		_statsInfo("m", "result/rule/TestRuleGroup/test_rule_group_0b.txt");
		_saveTest();

	}

	@Test
	void test_rule_group_1() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule m::g1 if '(?a p ?b) do (-> m '(?a p2 ?b)))", "RU000");
		_test("(add-stmt m '(a p b))", "1");
		_test("(list-stmt m)", "'('(a p b))");

		_test("(query-stmt m '(?x ?z) from '(?x p2 ?z))", "'('(a b))");
		_mStatus(1, "m");
		_statsInfo("m", "result/rule/TestRuleGroup/test_rule_group_1a.txt");

		_test("(query-stmt m::g1 '(?x ?z) from '(?x p2 ?z))", "'('(a p b))");
		_mStatus(2, "m");
		_statsInfo("m", "result/rule/TestRuleGroup/test_rule_group_1b.txt");
		_saveTest();
	}
}
