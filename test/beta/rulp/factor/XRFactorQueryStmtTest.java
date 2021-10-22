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

		_test("(new model m)");
		_test("(add-stmt m '(d1 nm:hasTag tag1))");
		_test("(add-stmt m '(d1 nm:hasTag tag2))");
		_test("(add-stmt m '(d2 nm:hasTag tag3))");
		_test("(add-stmt m '(d1 nm:typeOf domain))");
		_test("(add-stmt m '(d2 nm:typeOf domain))");
		_test("(query-stmt m ?d from '(?d nm:typeOf domain))", "'(d1 d2)");
		_test("(query-stmt m ?t from '(d1 nm:hasTag ?t))", "'(tag1 tag2)");
		_test("(query-stmt m ?t from '(d2 nm:hasTag ?t))", "'(tag3)");

		_test("(foreach (?domain (query-stmt m ?d from '(?d nm:typeOf domain))) (return '(?domain (size-of (query-stmt m ?t from '(?domain nm:hasTag ?t))))))",
				"'('(d1 2) '(d2 1))");
	}

	@Test
	void test_7_where_1_expr_1_a() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m name1:'(a 1))");
		_test("(add-stmt m name1:'(b 10))");
		_test("(add-stmt m name1:'(b 100))");
		_test("(query-stmt m '(?x ?y) from name1:'(?x ?y) (> ?y 1))", "'('(b 10) '(b 100))");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_7_where_1_expr_1_b() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m name1:'(a 1))");
		_test("(add-stmt m name1:'(b 10))");
		_test("(add-stmt m name1:'(b 100))");
		_test("(query-stmt m '(?x ?y) from name1:'(?x ?y) where (> ?y 1))", "'('(b 10) '(b 100))");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_7_where_1_uniq_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2) )");
		_test("(add-stmt m '(a2 b c2))");
		_test("(query-stmt m ?a from '(?a ?b ?c))", "'(a a a2)");
		_test("(query-stmt m ?a from '(?a ?b ?c) where '(uniq on ?a))", "'(a a2)");

		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();
	}

	@Test
	void test_9_order_by_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m n1:'(10))");
		_test("(add-stmt m n1:'(100))");
		_test("(add-stmt m n1:'(1))");

		_test("(query-stmt m ?x from n1:'(?x))", "'(10 100 1)");
		_test("(query-stmt m ?x from n1:'(?x) limit 1)", "'(10)");
		_test("(query-stmt m ?x from n1:'(?x) order by ?x)", "'(10)");
	}

	@Test
	void test_8_do_1() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(n1 p1 100))");
		_test("(add-stmt m '(n2 p2 200))");
		_test("(list-stmt m)", "'('(n1 p1 100) '(n2 p2 200))");
		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_test("(query-stmt m '(?n ?v ?x) from '(?n p2 ?v) (> ?v 150) do (defvar ?x (size-of (remove-stmt '(?n ? ?v)))))",
				"'('(n2 200 1))");
		_mCount(2, "m");
		_eCount(2, "m");
		_mStatus(2, "m");

		_test("(list-stmt m)", "'('(n1 p1 100))");

		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_8_do_2() {

		_setup();

		_test("(new model m)");
		_test("(add-rule m if n1:'(?x) (< ?x 3) do (-> m n1:'((+ ?x 1))))");
		_test("(add-stmt m n1:'(1))");
		_test("(list-stmt m)", "'(n1:'(1))");
		_test("(query-stmt m ?x from n1:'(?x) do (remove-stmt n1:'(?x)))", "'(1)");
		_test("(list-stmt m)", "'()");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_8_do_3() {

		_setup();

		_test("(new model m)");
		_test("(add-stmt m '(n1 p1 100))");
		_test("(add-stmt m '(n2 p2 200))");
		_test("(list-stmt m)", "'('(n1 p1 100) '(n2 p2 200))");
		_test("(query-stmt m (+ ?z 1) from '(?x ?y ?z))", "'(101 201)");
	}
}
