package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorPrintRunnableCounterTest extends RuleTestBase {

	@Test
	void test_1() {
		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-rule \"R1\" m if '(?x typeof node) do (-> '(?x typeof2 node)))");
		_test("(start m)", "6");
		_test("(state-of m)", "completed");
		_test("(print-runnable-counter m)",
				"\"stmt=4, rule=1, match=0, fetch=8, exec=6, idle=1, state=0/3, max-queue=2, "
						+ "uniq-obj=3, entry-cnt=4, entry-maxid=4, waste-node=0, waste-match=0\"");

		_test("(print-runnable-counter (get-rule m \"R1\"))", "\"stmt=2, entry=4, node=3, exec=1, update=2\"");

		_test("(foreach (?rule (list-rule m)) (return '(?rule  (print-runnable-counter ?rule))))",
				"'('(R1 \"stmt=2, entry=4, node=3, exec=1, update=2\"))");
	}

}
