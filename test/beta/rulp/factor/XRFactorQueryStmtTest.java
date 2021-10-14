package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorQueryStmtTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 typeof node))");
		_test("(add-stmt m '(n2 typeof node))");
		_test("(add-stmt m '(n1 hasPath path1))");
		_test("(add-stmt m '(n2 hasPath path2))");

		_test("(query-stmt m '(?p) from '(?n hasPath ?p))", "'('(path1) '(path2))");
		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p))", "'('(n1) '(n2))");
		_test("(query-stmt m ?n from '(?n typeof node) '(?n hasPath ?p))", "'(n1 n2)");
		_test("(query-stmt m '(?n ?p) from '(?n hasPath ?p))", "'('(n1 path1) '(n2 path2))");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_2_loader_1() {

		_setup();
		_test("(new model m)");
		_test("(add-lazy-stmt m '(?a typeof node) '('(n1 typeof node) '(n2 typeof node)))");
		_test("(add-stmt m '(n1 hasPath path1))");
		_test("(add-stmt m '(n2 hasPath path2))");

		_test("(query-stmt m '(?p) from '(?n hasPath ?p))", "'('(path1) '(path2))");
		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p))", "'('(n1) '(n2))");
		_test("(query-stmt m '(?n ?p) from '(?n hasPath ?p))", "'('(n1 path1) '(n2 path2))");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_2_loader_2() {

		_setup();
		_test("(new model m)");
		_test("(add-lazy-stmt m '(?a typeof node) '('(n1 typeof node) '(n2 typeof node)))");
		_test("(add-stmt m '(n1 hasPath path1))");
		_test("(add-stmt m '(n2 hasPath path2))");

		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p) limit 1)", "'('(n1))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_already_has_data_a_join() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 typeof node))");
		_test("(add-stmt m '(n1 hasPath path2))");
		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p) limit 1)", "'('(n1))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_already_has_data_b_join_twice() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 typeof node))");
		_test("(add-stmt m '(n1 hasPath path2))");
		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p) limit 1)", "'('(n1))");
		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_test("(query-stmt m '(?n) from '(?n typeof node) '(?n hasPath ?p) limit 1)", "'('(n1))");
		_mCount(2, "m");
		_eCount(2, "m");
		_mStatus(2, "m");
		_saveTest();
	}

	@Test
	void test_3_query_1_already_has_data() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 typeof node))");
		_test("(query-stmt m '(?p) from '(?n typeof node) limit 1)", "'('(?p))");
		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_4_no_data_rule() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 typeof node))");
		_test("(add-stmt m '(node typeof node2))");

		_test("(add-rule \"R1\" m if '(?x typeof c1) do (-> m '(?x typeof c2)) )", "R1"); // should not work
		_test("(add-rule \"R2\" m if '(?x ?p ?y) '(?y ?p ?z) do (-> m '(?x ?p node3)) )", "R2"); // work once

		_test("(query-stmt m '(?n) from '(?n typeof node3) limit 1)", "'('(n1))");

		_rStatus(1, "m", "R1");
		_rStatus(1, "m", "R2");
		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_5_var() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(d1 nm:hasTag tag1))");
		_test("(add-stmt m '(d1 nm:hasTag tag2))");
		_test("(add-stmt m '(d2 nm:hasTag tag3))");
		_test("(add-stmt m '(d1 nm:typeOf domain))");
		_test("(add-stmt m '(d2 nm:typeOf domain))");

		_test("(foreach (?domain (query-stmt m ?d from '(?d nm:typeOf domain))) (return '(?domain (size-of (query-stmt m ?t from '(?domain nm:hasTag ?t))))))",
				"'('(d1 2) '(d2 1))");
	}

	@Test
	void test_6_distinct_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2) )");
		_test("(add-stmt m '(a2 b c2))");
		_test("(query-stmt m ?a from '(?a ?b ?c))", "'(a a2)");
		_test("(query-stmt m ?a from '(?a ?b ?c) limit 1)", "'(a)");

		_test("(add-stmt m (query-stmt n1:'(?a ?b) from '(?a ?b ?)))", "");
		_test("(list-stmt m n1:'(?...))", "'(a a2)");
		_test("(list-stmt m n1:'(?x ?...))", "'(a a2)");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_7_where_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m name1:'(a 1))");
		_test("(add-stmt m name1:'(b 10))");
		_test("(add-stmt m name1:'(b 100))");
		_test("(query-stmt m '(?x ?y) from name1:'(?x ?y) (> ?y 1))", "'('(b 10) '(b 100))");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_7_where_2() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m name1:'(a 1))");
		_test("(add-stmt m name1:'(b 10))");
		_test("(add-stmt m name1:'(b 100))");
		_test("(query-stmt m '(?x ?y) from name1:'(?x ?y) where (> ?y 1))", "'('(b 10) '(b 100))");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_8_do_1() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-stmt m '(n1 p1 100))");
		_test("(add-stmt m '(n2 p2 200))");
		_test("(list-stmt m)", "'('(n1 p1 100) '(n2 p2 200))");
		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");

		_test("(query-stmt m '(?n ?v) from '(?n p2 ?v) (> ?v 150) do (remove-stmt '(?n ? ?v)))", "nil");
		_mCount(2, "m");
		_eCount(2, "m");
		_mStatus(2, "m");

		_test("(list-stmt m)", "'('(n1 p1 100))");

		_saveTest();
		_statsInfo("m", "result/factor/XRFactorQueryStmtTest/test_8_do_1.txt");
	}
}
