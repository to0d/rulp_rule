package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorAddNodeTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-node m n1:'(?x ?y ?z))", "1");
		_test("(add-node m n1:'(?x ?y ?z))", "0");

		_test_error("(add-node m n1:'(?x ?y))",
				"unmatch entry length: expect=2, actual=3\n" + "at main: (add-node m n1:'(?x ?y))");
		_test_error("(add-node m '(?x ?y))", "Invalid named filter: '(?x ?y)\n" + "at main: (add-node m '(?x ?y))");

		_test("(add-node m n2:'(? ? ?))", "1");
		_test("(add-node m n3:'(3))", "1");

		_statsInfo("m", "result/factor/XRFactorAddNodeTest/test_1.txt");
	}

}
