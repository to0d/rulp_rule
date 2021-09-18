package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestRuleGroup extends RuleTestBase {

	@Test
	void test_0() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule m::g1 if '(?a p ?b) do (-> m '(?a p2 ?b)))", "RU000");
		_test("(add-stmt m '(a p b))", "1");
		_test("(list-stmt m)", "'('(a p b))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a p b) '(a p2 b))");
		_mStatus(1, "m");
		_saveTest();
	}

}
