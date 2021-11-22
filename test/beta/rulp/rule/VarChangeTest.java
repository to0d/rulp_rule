package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class VarChangeTest extends RuleTestBase {

	@Test
	public void test_1_expr_tree_1() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?from ?to) do (-> m '(a b ?to)) )");
		_test("(setq ?x c)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b c))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_2() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?from ?to) (> ?to 1) do (-> m '(a b ?to)) )");
		_test("(setq ?x 2)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b 2))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_3() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?val) do (-> m '(a b ?val)) )");
		_test("(setq ?x c)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b c))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_4() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?val) (> ?val 1) do (-> m '(a b ?val)) )");
		_test("(setq ?x 2)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b 2))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_5() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(defvar ?x x1)");
		_test("(defvar ?y y1)");
		_test("(add-rule m if (var-changed ?x ?xv) (var-changed ?y ?yv) do (-> m '(a ?xv ?yv)) )");
		_test("(setq ?x x2)");
		_test("(setq ?x x3)");
		_test("(setq ?y y2)");
		_test("(setq ?y y3)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a x3 y3))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_6() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?from d) do (-> m '(a b ?from)) )");
		_test("(setq ?x c)");
		_test("(setq ?x d)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b c))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_7() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x c ?to) do (-> m '(a b ?to)) )");
		_test("(setq ?x c)");
		_test("(setq ?x d)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b d))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_8() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(defvar ?xurl \"abc\")");
		_test("(add-rule m if (var-changed ?x ?url) (str-start-with ?url ?xurl) do (-> m '(a b ?url)) )");
		_test("(setq ?x \"abcd\")");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b \"abcd\"))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_1_expr_tree_9() {

		_setup();

		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(defvar ?xurl \"abc\")");
		_test("(add-stmt m url-entry:'(head1 \"abcd\"))");
		_test("(add-rule m if (var-changed ?x ?url) (str-start-with ?url ?xurl) url-entry:'(?url-name ?url) do (-> m '(a b ?url ?url-name)) )");
		_test("(setq ?x \"abcd\")");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b \"abcd\" head1) url-entry:'(head1 \"abcd\"))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_2_contant_bool_1() {

		_setup();

		_test("(new model m)");
		_test("(defvar ?x false)");
		_test("(add-rule m if (var-changed ?x ?v true) '(?a ?b) do (-> '(?b ?a)) )");
		_test("(add-stmt m '(a b))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b))");

		_test("(setq ?x true)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b) '(b a))");

		_test("(setq ?x false)");
		_test("(add-stmt m '(x y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b) '(b a) '(x y))");

		_test("(setq ?x true)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b) '(b a) '(x y) '(y x))");

		_mStatus(1, "m");
		_saveTest();

		_statsInfo("m");

	}

	@Test
	public void test_2_contant_bool_2() {

		_setup();

		_test("(new model m)");
		_test("(defvar ?x false)");
		_test("(add-rule m if (var-changed ?x ?v true) '(?a ?b) do (-> '(?b ?a)) )");
		_test("(add-stmt m '(a b))");
		_test("(query-stmt m '(?a ?b) from '(?a ?b))", "'('(a b))");

		_test("(setq ?x true)");
		_test("(query-stmt m '(?a ?b) from '(?a ?b))", "'('(a b) '(b a))");

		_test("(setq ?x false)");
		_test("(add-stmt m '(x y))");
		_test("(query-stmt m '(?a ?b) from '(?a ?b))", "'('(a b) '(b a) '(x y))");

		_test("(setq ?x true)");
		_test("(query-stmt m '(?a ?b) from '(?a ?b))", "'('(a b) '(b a) '(x y) '(y x))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_2_scope_1() {

		_setup();
		_test_script();
	}

	@Test
	public void test_3_same_1() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?f1 ?t1) do (-> m n1:'(?t1)) )");
		_test("(add-rule m if (var-changed ?x ?f2 ?t2) do (-> m n2:'(?t2)) )");
		_test("(setq ?x c)");
		_test("(setq ?x d)");
		_test("(start m)");
		_test("(list-stmt m)", "'(n1:'(d) n2:'(d))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}
}
