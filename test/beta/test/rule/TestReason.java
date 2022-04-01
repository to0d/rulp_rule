package beta.test.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestReason extends RuleTestBase {

	@Test
	public void test_3_circle_reason_a() {
		_setup();
		// _enableTrace();
		_test("(new model m)");
		_test("(add-rule m if '(?x p ?y) '(p p2 p3) do (-> m '(?y p ?x)))");
		_test("(add-stmt m '(a p c))");
		_test("(add-stmt m '(p p2 p3))");
		_test("(start m)", "9");
		_test("(list-stmt m)", "'('(a p c) '(p p2 p3) '(c p a))");
		_dumpEntryTable("m", "result/rule/TestReason/test_3_circle_reason_a.dump.1.txt");

		_test("(remove-stmt m '(p p2 p3))");
		_test("(list-stmt m)", "'('(a p c))");
		_dumpEntryTable("m", "result/rule/TestReason/test_3_circle_reason_a.dump.2.txt");

		_statsInfo("m");
	}

	@Test
	public void test_3_circle_reason_b_named() {
		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
}
