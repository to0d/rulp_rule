package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestRuleModifier extends RuleTestBase {

	@Test
	public void test_1_entry_order_1_a() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if n1:'(?a) n1:'(?b) (!= ?a ?b) do (-> n2:'(?a ?b)))");
		_test("(add-stmt m n1:'(a))");
		_test("(add-stmt m n1:'(b))");
		_test("(query-stmt m '(?a ?b) from n2:'(?a ?b))", "'('(a b) '(b a))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m", "result/rule/TestRuleModifier/test_1_entry_order_1_a.txt");
	}

	@Test
	public void test_1_entry_order_1_b() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(n1:'(?a) n1:'(?b) entry-order) (!= ?a ?b) do (-> n2:'(?a ?b)))");
		_test("(add-stmt m n1:'(a))");
		_test("(add-stmt m n1:'(b))");
		_test("(query-stmt m '(?a ?b) from n2:'(?a ?b))", "'('(a b) '(b a))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m", "result/rule/TestRuleModifier/test_1_entry_order_1_b.txt");
	}
}
