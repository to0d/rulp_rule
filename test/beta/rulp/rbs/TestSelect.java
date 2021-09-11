package beta.rulp.rbs;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestSelect extends RuleTestBase {

	@Test
	void test_1_select_1() {

		// insert into xxx values yyy
		// select xxx from yyy
		// select xxx from yyy limit n
		// insert into xxxx values (select yyy from zzz)

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m n1:'(a b1 c))");
		_test("(add-stmt m n1:'(a b2 c))");

		_test("(query-stmt m ?a from (select '(?x ?y) from n1:'(?x ?y ?z)))", "'(a a2)");
		_test("(query-stmt m ?a from (select '(?x ?y) from n1:'(?x ?y ?z) limit 1))", "'(a a2)");
		_test("(query-stmt m ?a from (select '(?x) from n1:'(?x ? ?)))", "'(a a2)");
		_test("(query-stmt m ?a from (select '(?x) from n1:'(?x ?...)))", "'(a a2)");

		_test("(add-stmt m (select '(?z ?y ?x) from n1:'(?x ?y ?z)))", "");
		_test("(list-stmt m)", "'(a a2)");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

}
