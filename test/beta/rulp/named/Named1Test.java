package beta.rulp.named;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class Named1Test extends RuleTestBase {

	@Test
	void test_named_1() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-rule m if '(?x ?y ?z) do (-> '(?z ?y ?x)))");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name1:'(?z ?y ?x)))");

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(add-stmt m name2:'(a b c))");
		_test("(list-stmt m)", "'('(a b c) name1:'(a b c) name2:'(a b c))");

		_test("(start m)");
		_test("(list-stmt m)", "'('(a b c) '(c b a) name1:'(a b c) name1:'(c b a) name2:'(a b c))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m");
	}

	@Test
	void test_named_2() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-rule m if '(?x ?y ?z) do (-> '(?z ?y ?x)))");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name1:'(?z ?y ?x)))");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");

		_test("(list-stmt m)", "'('(x y z) name1:'(a b c))");
		_test("(query-stmt m '(?x ?y ?z) from '(?x ?y ?z))", "'('(x y z) '(z y x))");
		_test("(query-stmt m '(?x ?y ?z) from name1:'(?x ?y ?z))", "'('(a b c) '(c b a))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m");
	}

}
