package beta.rulp.rbs;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddViewTest extends RuleTestBase {

	@Test
	void test_1_as_select_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-view m n2:'(? ?) as select '(?x ?y) from n1:'(?x ?y ?z))", "");
		_statsInfo("m", "result/sql/XRFactorAddViewTest/test_1_as_select_1.txt");
	}
}
