package beta.rulp.partial;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ConflictRuleTest extends RuleTestBase {

	@Test
	void test_confilct_rule_1_without_priority() {

		_setup();

//		XREntryTable.TRACE = true;

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )", "R1"); //
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (remove-stmt ?0) )", "R2");
		_test("(add-stmt m '(a p b) '(b p c))", "2");
		_test("(start m)", "8");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(b p c) '(a p c))");

		_mStatus(1, "m");
		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");
		_saveTest();

		_statsInfo("m", "result/partial/ConflictRuleTest/test_confilct_rule_1_without_priority.txt");
	}

	@Test
	void test_confilct_rule_2_with_priority() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )", "R1");
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (remove-stmt ?0))", "R2");
		_test("(set-rule-priority (get-rule m \"R2\") 199)");
		_test("(priority-of (get-rule m \"R1\"))", "99"); // default priority
		_test("(priority-of (get-rule m \"R2\"))", "199"); // high priority

		_test("(add-stmt m '(a p b) '(b p c))", "2");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(b p c))");

		_mStatus(1, "m");
		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");
		_saveTest();
	}
}
