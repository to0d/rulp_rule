package beta.rulp.factor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;

import alpha.rulp.rule.IRRule;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.ximpl.node.RReteType;

class XRFactorAddRuleTest extends RuleTestBase {

	@Test
	void test_1() {

		_setup();
		_test("(new model m)", "m");
		_test("(type-of m)", "instance");
		_test("(name-of m)", "\"m\"");
		_test("(add-stmt m '(a p1 c))", "true");
		_test("(add-stmt m '(a p1 c))", "false"); // duplicated
		_test("(add-stmt m '(a p1 b))", "true");
		_test("(add-stmt m '(a p2 c))", "true");
		_test("(add-rule \"R1\" m if '(?x p2 ?y) do (-> m '(?y p2 ?x)))", "R1"); // rule not work
		_test("(state-of m)", "runnable");
		_test("(list-stmt m)", "'('(a p1 c) '(a p1 b) '(a p2 c))");
		_mStatus(1, "m");

		_test("(start m)", "6");
		_test("(state-of m)", "completed");
		_test("(list-stmt m from '(?x p2 ?y))", "'('(a p2 c) '(c p2 a))"); // rule works
		_mStatus(2, "m");
		_saveTest();
	}

	@Test
	void test_rule_beta3_1() {

		_setup();
		_test("(new model m)");

		// Should be beta3
		_test("(add-rule \"RULE1\" m if '(?x ?y ?z) '(?a ?b ?c) do (-> m '(?x ?y ?z ?a ?b ?c)))");
		IRRule rule = _model("m").getNodeGraph().getRule("RULE1");
		assertNotNull(rule);
		assertEquals("[]", rule.getNodeMatrix().getNodeList(RReteType.BETA0).toString());
		assertEquals("[B3002: '('(?0 ?1 ?2) '(?3 ?4 ?5))]",
				rule.getNodeMatrix().getNodeList(RReteType.BETA3).toString());

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "4");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(a b c a b c) '(x y z a b c) '(a b c x y z) '(x y z x y z))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_beta3_2_a() {

		_setup();
		_test("(new model m)");

		// Should be beta0
		_test("(add-rule \"RULE1\" m if '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) do (-> m '(?x ?y ?z ?b ?c)))");
		IRRule rule = _model("m").getNodeGraph().getRule("RULE1");
		assertNotNull(rule);
		assertEquals("[B0002: '('(?0 ?1 ?2) '(?3 ?4 ?5) (equal ?3 ?0))]",
				rule.getNodeMatrix().getNodeList(RReteType.BETA0).toString());
		assertEquals("[]", rule.getNodeMatrix().getNodeList(RReteType.BETA3).toString());

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "4");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(a b c b c) '(x y z y z))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_beta3_2_b() {

		_setup();
		_test("(new model m)");

		// Should be beta0
		_test("(add-rule \"RULE1\" m if '(?x ?y ?z) '(?a ?b ?c) (equal ?a ?x) do (-> m '(?x ?y ?z ?b ?c)))");
		IRRule rule = _model("m").getNodeGraph().getRule("RULE1");
		assertNotNull(rule);
		assertEquals("[B0002: '('(?0 ?1 ?2) '(?3 ?4 ?5) (equal ?0 ?3))]",
				rule.getNodeMatrix().getNodeList(RReteType.BETA0).toString());
		assertEquals("[]", rule.getNodeMatrix().getNodeList(RReteType.BETA3).toString());

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "4");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(a b c b c) '(x y z y z))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_beta3_2_c() {

		_setup();
		_test("(new model m)");

		// Should be beta0
		_test("(add-rule \"RULE1\" m if '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) (!= ?0 ?1) do (-> m '(?x ?y ?z ?b ?c)))");
		IRRule rule = _model("m").getNodeGraph().getRule("RULE1");
		assertNotNull(rule);
		assertEquals("[B0002: '('(?0 ?1 ?2) '(?3 ?4 ?5) (equal ?3 ?0))]",
				rule.getNodeMatrix().getNodeList(RReteType.BETA0).toString());
		assertEquals("[]", rule.getNodeMatrix().getNodeList(RReteType.BETA3).toString());

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "3");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z))");
		_mStatus(1, "m");
		_saveTest();

		_statsInfo("m", "result/factor/XRFactorAddRuleTest/test_rule_beta3_2_c.txt");
	}

	@Test
	void test_rule_beta3_3() {

		_setup();
		_test("(new model m)");

		// Should be beta0 & expr1
		_test("(add-rule \"RULE1\" m if '(?x ?y ?z) '(?a ?b ?c) (equal ?x ?a) (equal ?y ?b) do (-> m '(?x ?y ?z ?b ?c)))");
		IRRule rule = _model("m").getNodeGraph().getRule("RULE1");
		assertNotNull(rule);

		assertEquals("[B0002: '('(?0 ?1 ?2) '(?3 ?4 ?5) (equal ?3 ?0))]",
				rule.getNodeMatrix().getNodeList(RReteType.BETA0).toString());
		assertEquals("[]", rule.getNodeMatrix().getNodeList(RReteType.BETA3).toString());
		assertEquals("[E1003: '('('(?0 ?1 ?2) '(?3 ?4 ?5) (equal ?3 ?0)) (equal ?4 ?1))]",
				rule.getNodeMatrix().getNodeList(RReteType.EXPR1).toString());

		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(a b c b c) '(x y z y z))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_beta3_4() {

		_setup();
		_test("(new model m)");

		// Should be beta3
		_test("(add-rule m if '(?x ?y ?z) '(?a ?b ?c) (not (equal ?x ?a)) do (-> m '(?x ?y ?z ?b ?c)))");
		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)", "4");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(x y z b c) '(a b c y z))");
		_mStatus(1, "m");

		_saveTest();

	}

	@Test
	void test_rule_beta3_5() {
		_setup();
		_test("(new model m)");

		// Should be beta3
		_test("(add-rule m if '(?x ?y ?z) '(?a ?b ?c) (= (+ ?c ?z) 0) (> ?z ?c) do (-> m '(?x ?y ?z ?b ?c)))");
		_test("(add-stmt m '(a b 2))");
		_test("(add-stmt m '(a b 1))");
		_test("(add-stmt m '(x y -2))");
		_test("(add-stmt m '(x y -3))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a b 2) '(a b 1) '(x y -2) '(x y -3) '(a b 2 y -2))");
		_mStatus(1, "m");

		_saveTest();

	}

	@Test
	void test_rule_condtion() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-rule m if '(?a age ?n) (>= ?n 18) do (-> m '(?a is adult)))", "RU000");
		_test("(add-rule m if '(?a age ?n) (< ?n 18) do (-> m '(?a is child)))", "RU001");
		_test("(list-rule m)", "'(RU000 RU001)");
		_test("(add-stmt m '(todd age 39))");
		_test("(add-stmt m '(yifan age 7))");
		_test("(start m)", "9");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(todd age 39) '(yifan age 7) '(todd is adult) '(yifan is child))");
		_mStatus(1, "m");
		_rStatus(1, "m", "RU000");
		_rStatus(1, "m", "RU001");
		_saveTest();
	}

	@Test
	void test_rule_match() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(p1 propertyOf inverse))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-rule m if '(?p propertyOf inverse) '(?a ?p ?b) do (-> m '(?b ?p ?a)))", "RU000");
		_test("(start m)", "10");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(p1 propertyOf inverse) '(a p1 b) '(b p1 a))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_match_2() {

		_setup();

		// XRModel.TRACE_RETE = true;
		_test("(new model m)");
		_test("(add-rule m if '(?a ?a ?a) do (-> m '(?a typeOf xxx)))", "RU000");
		_test("(add-stmt m '(a a a))");
		_test("(add-stmt m '(c b b))");
		_test("(start m)", "6");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a a a) '(c b b) '(a typeOf xxx))");

		_mStatus(1, "m");
		_saveTest();
	}

}
