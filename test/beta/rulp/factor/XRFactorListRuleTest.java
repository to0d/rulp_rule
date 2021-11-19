package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorListRuleTest extends RuleTestBase {

	@Test
	void test_list_failed_rule() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"R1\" m if '(?a p1 ?b) do (if (equal ?a ?b) (error err-r1)) (-> (?b p1 ?a)))");
		_test("(add-rule \"R2\" m if '(?a p2 ?b) do (-> (?b p1 ?a)))");
		_test("(add-stmt m '(a p1 a))");
		_test("(list-rule m)", "'(R1 R2)");
		_test("(list-with-state (list-rule m) failed)", "'()");
		_test("(list-without-state (list-rule m) Failed)", "'(R1 R2)");

		_test("(start m)");

		_test("(list-with-state (list-rule m) failed)", "'(R1)");
		_test("(list-without-state (list-rule m) failed)", "'(R2)");
	}

}
