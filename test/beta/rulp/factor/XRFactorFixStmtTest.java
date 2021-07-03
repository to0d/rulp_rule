package beta.rulp.factor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorFixStmtTest extends RuleTestBase {

	@Test
	void test_1_no_child() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p1 ?b) '(?p p2 ?c) do (-> m '(?a p3 ?c)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(fix-stmt m '(c p2 d))");
		_test("(start m)");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(c p2 d) '(a p3 d))");

		_mStatus(1, "m");
		_saveTest();

		_statsInfo("m", "result/factor/XRFactorFixStmtTest/test_1_no_child.txt");
		_dumpEntryTable("m", "result/factor/XRFactorFixStmtTest/test_1_no_child.entry.dump.txt");
	}

}
