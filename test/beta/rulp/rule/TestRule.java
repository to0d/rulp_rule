package beta.rulp.rule;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRError;
import alpha.rulp.lang.IRList;
import alpha.rulp.utils.RuleTestBase;

public class TestRule extends RuleTestBase {

	@Test
	void test_0() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"TG1\" m if '(?c hasChild ?cc) do (-> m '(?c hasGroupChild ?cc)))", "TG1");
		_test("(add-stmt m '(a hasChild b))", "1");
		_test("(list-stmt m)", "'('(a hasChild b))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a hasChild b) '(a hasGroupChild b))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_0_default_model() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"TG1\" m if '(?c hasChild ?cc) do (-> '(?c hasGroupChild ?cc)))", "TG1");
		_test("(add-stmt m '(a hasChild b))", "1");
		_test("(list-stmt m)", "'('(a hasChild b))");
		_test("(start m)", "5");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a hasChild b) '(a hasGroupChild b))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_0_error_rule_1() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"R1\" m if '(?a p1 ?b) do (if (equal ?a ?b) (error err-r1 '(?a ?b) )) (-> '(?b p1 ?a)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(a p1 a))");
		_test("(add-stmt m '(b p1 b))");
		_test("(list-stmt m)", "'('(a p1 b) '(a p1 a) '(b p1 b))");
//		_test("(list-obj m)", "'(a p1 b)");

		ArrayList<String> failedRules = new ArrayList<>();
		ArrayList<IRList> failedEntrys = new ArrayList<>();
		ArrayList<IRError> failedErrors = new ArrayList<>();

		_model("m").addRuleFailedListener((rule) -> {
			failedRules.add(rule.getRuleName());
			failedEntrys.add(rule.getLastValues());
			failedErrors.add(rule.getLastError());
		});

		_test("(start m)");
		_test("(list-stmt m)", "'('(a p1 b) '(a p1 a) '(b p1 b) '(b p1 a))");
//		_test("(list-obj m)", "'(a p1 b)");

		_mStatus(1, "m");
		_saveTest();

		assertEquals("[R1]", failedRules.toString());
		assertEquals("['(a p1 a)]", failedEntrys.toString());
		assertEquals("[error: err-r1, '(a a)]", failedErrors.toString());

	}

	@Test
	void test_0_error_rule_halt() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"R1\" m if '(?a p1 ?b) do (if (equal ?a ?b) (error err-r1 '(?a ?b))) (-> '(?b p1 ?a)))");
		_test("(add-stmt m '(a p1 a))");
		_test("(add-stmt m '(a p1 b))");
		_test("(list-stmt m)", "'('(a p1 a) '(a p1 b))");
//		_test("(list-obj m)", "'(a p1 b)");

		_model("m").addRuleFailedListener((rule) -> {
			_model("m").halt();
		});

		_test("(start m)");
		_test("(state-of m)", "halting");
		_test("(list-stmt m)", "'('(a p1 a) '(a p1 b))");
//		_test("(list-obj m)", "'(a p1 b)");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_0_similiar_match_alpha_1() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"RA001\" m if '(?a p1 ?b) '(?b p2 ?c) do (-> '(?a p2 ?c)))");
		_test("(add-rule \"RA002\" m if '(?c p2 ?cc) do (-> '(?c p3 ?cc)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(b p2 c))");
		_test("(list-stmt m)", "'('(a p1 b) '(b p2 c))");
//		_test("(list-obj m)", "'(a p1 b p2 c)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a p1 b) '(b p2 c) '(b p3 c) '(a p2 c) '(a p3 c))");
//		_test("(list-obj m)", "'(a p1 b p2 c p3)");

		_mStatus(1, "m");
		_rStatus(1, "m", "RA001");
		_rStatus(1, "m", "RA002");
		_saveTest();

	}

	@Test
	void test_0_similiar_match_alpha_2() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p1 ?a) do (-> '(?a p2 ?a)))");
		_test("(add-rule m if '(?x p1 ?x) do (-> '(?x p3 ?x)))");
		_test("(add-stmt m '(a p1 a))");
		_test("(list-stmt m)", "'('(a p1 a))");
//		_test("(list-obj m)", "'(a p1)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a p1 a) '(a p2 a) '(a p3 a))");
//		_test("(list-obj m)", "'(a p1 p2 p3)");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_0_similiar_match_beta() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p1 ?b) '(?b p2 ?c) do (-> '(?a p3 ?c)))");
		_test("(add-rule m if '(?x p1 ?y) '(?y p2 ?z) do (-> '(?x p4 ?z)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(b p2 c))");
		_test("(list-stmt m)", "'('(a p1 b) '(b p2 c))");
//		_test("(list-obj m)", "'(a p1 b p2 c)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a p1 b) '(b p2 c) '(a p3 c) '(a p4 c))");
//		_test("(list-obj m)", "'(a p1 b p2 c p3 p4)");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_1_forward() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?p propertyOf fowardProperty) '(?a ?p ?b) '(?b ?p ?c) do (-> m '(?a ?p ?c)) )");
		_test("(add-stmt m '(p1 propertyOf fowardProperty))");
		_test("(add-stmt m '(x p1 y))");
		_test("(add-stmt m '(y p1 z))");
		_test("(start m)");
		_test("(list-stmt m from '(?x p1 ?y))", "'('(x p1 y) '(y p1 z) '(x p1 z))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_2_inverse_add_rule_after_stmt() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(p1 propertyOf inverse))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-rule m if '(?p propertyOf inverse) '(?a ?p ?b) do (-> m '(?b ?p ?a)) )");
		_test("(start m)");
		_test("(list-stmt m from '(?x p1 ?y))", "'('(a p1 b) '(b p1 a))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_2_inverse_add_rule_before_stmt() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?p propertyOf inverse) '(?a ?p ?b) do (-> m '(?b ?p ?a)) )");
		_test("(add-stmt m '(p1 propertyOf inverse))");
		_test("(add-stmt m '(a p1 b))");
		_test("(start m)");
		_test("(list-stmt m from '(?x p1 ?y))", "'('(a p1 b) '(b p1 a))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_3_variable_entry_1() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(t1 t2 t3 t4))");
		_test("(add-rule m if '(?a ?b ?c ?d) do (-> m '(?b ?c ?d ?a)) )");
		_test("(start m)");
		_test("(list-stmt m)", "'('(t1 t2 t3 t4) '(t2 t3 t4 t1) '(t3 t4 t1 t2) '(t4 t1 t2 t3))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_3_variable_entry_2() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(t1 t2 t3 t4))");
		_test("(add-stmt m '(t1 p t2))");
		_test("(add-stmt m '(t2 p t3))");
		_test("(add-rule m if '(?a ?b ?c ?d) '(?a p ?b) do (-> m '(?b ?c ?d ?a)) )");
		_test("(start m)");
		_test("(list-stmt m from '(?x1 ?x2 ?x3 ?x4))", "'('(t1 t2 t3 t4) '(t2 t3 t4 t1) '(t3 t4 t1 t2))");
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_4_const_expr_1() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(p x y) '(?a p ?b) '(?b p ?c) (= 1 2) do (-> m '(?a p ?c)) )");
		_test("(add-stmt m '(x p y))");
		_test("(add-stmt m '(y p z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z))");

		_test("(add-stmt m '(p x y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z) '(p x y))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_4_const_expr_2() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(p x y) '(?a p ?b) '(?b p ?c) (= 1 1) do (-> m '(?a p ?c)) )");
		_test("(add-stmt m '(x p y))");
		_test("(add-stmt m '(y p z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z))");

		_test("(add-stmt m '(p x y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z) '(p x y) '(x p z))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_4_const_expr_3() {

		_setup();
		_test("(new model m)");
		_test("(defun fun1 () (return (> 3 2)))");
		_test("(add-rule m if '(?a ?b ?c) (fun1) do (-> m '(?b ?c ?a)) )");
		_test("(add-stmt m '(a b c))");
		_test("(add-stmt m '(x y z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(a b c) '(x y z) '(b c a) '(y z x) '(c a b) '(z x y))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_4_const_stmt() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(p x y) '(?a p ?b) '(?b p ?c) do (-> m '(?a p ?c)) )");
		_test("(add-stmt m '(x p y))");
		_test("(add-stmt m '(y p z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z))");

		_test("(add-stmt m '(p x y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z) '(p x y) '(x p z))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_4_redunt_beta() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a ?p ?b) '(?b ?p ?c) do (-> m '(?a p3 ?c)) )");
		_test("(add-stmt m '(x p1 y))");
		_test("(add-stmt m '(y p1 z))");
		_test("(add-stmt m '(x p2 y))");
		_test("(add-stmt m '(y p2 z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p1 y) '(y p1 z) '(x p2 y) '(y p2 z) '(x p3 z))");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_5_waste_var() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?from ?to) '(?a p ?b) '(?b p ?c) do (-> m '(?a p ?c)) )");
		_test("(add-stmt m '(x p y))");
		_test("(add-stmt m '(y p z))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_5_waste_var_2() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x)");
		_test("(add-rule m if (var-changed ?x ?from ?to) '(?a p ?b) '(?b p ?c) '(?c p ?d) do (-> m '(?a p ?d)) )");
		_test("(add-stmt m '(x p y))");
		_test("(add-stmt m '(y p z))");
		_test("(add-stmt m '(z p z2))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p z) '(z p z2))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_1() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 1)");
		_test("(add-rule m if '(?a p ?b) (= ?x 1) do (-> m '(?b p ?a)) )");
		_test("(add-stmt m '(x p y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p x))");

		_test("(setq ?x 3)");
		_test("(add-stmt m '(a p b))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p x) '(a p b))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_2() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 1)");
		_test("(add-rule m if '(?a p ?b) (= ?x 2) do (-> m '(?b p ?a)) )");
		_test("(add-stmt m '(x p y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y))");

		_test("(setq ?x 2)");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_3() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 2)");
		_test("(add-rule m if '(?a p ?b) (> ?x 1) do (-> m '(?b p ?a)) )");
		_test("(add-stmt m '(x p y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p x))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_4() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 2)");
		_test("(defun fun1 () (return (> ?x 1)))");
		_test("(add-rule m if '(?a p ?b) (fun1) do (-> m '(?b p ?a)) )");
		_test("(add-stmt m '(x p y))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p y) '(y p x))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_5() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 1)");
		_test("(add-rule m if '(?a p ?b) (> ?b ?x) (< ?b 20) do (-> m '(?a p (+ 10 ?b))) )");
		_test("(add-stmt m '(x p 2))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p 2) '(x p 12) '(x p 22))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_6() {

		_setup();
		_test("(new model m)");
		_test("(defun f1 (?x) (return (> ?x 5)))");
		_test("(defun f2 (?x) (return (< ?x 5)))");
		_test("(add-rule m if name1:'(?a ?b) (?a ?b) do (-> name2:'(?a ?b)))");
		_test("(add-stmt m name1:'(f1 1))");
		_test("(add-stmt m name1:'(f1 9))");
		_test("(add-stmt m name1:'(f2 1))");
		_test("(add-stmt m name1:'(f2 9))");

		_test("(start m)");
		_test("(list-stmt m)",
				"'(name1:'(f1 1) name1:'(f1 9) name1:'(f2 1) name1:'(f2 9) name2:'(f1 9) name2:'(f2 1))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_7() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x1 0)");
		_test("(defvar ?x2 0)");
		_test("(defun f1 (?x) (setq ?x1 ?x))");
		_test("(defun f2 (?x) (setq ?x2 ?x))");
		_test("(add-rule m if name1:'(?a ?b) do (?a ?b))");
		_test("(add-stmt m name1:'(f1 1))");
		_test("(add-stmt m name1:'(f2 9))");

		_test("(start m)");
		_test("(list-stmt m)", "'(name1:'(f1 1) name1:'(f2 9))");

		_test("?x1", "1");
		_test("?x2", "9");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_8() {

		_setup();
		_test("(new model m)");
		_test("(defvar m::?xmode false)");
		_test("(add-rule m if (var-changed ?xmode ?v true) name1:'(?a) do (-> name2:'(?a)))");
		_test("(add-stmt m name1:'(a))");
		_test("(query-stmt m ?x from name2:'(?x))", "'()");
		_test("(setq m::?xmode true)");
		_test("(query-stmt m ?x from name2:'(?x))", "'(a)");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_6_var_expr_9() {

		_setup();
		_test("(new model m)");
		_test("(defvar m::?xmode false)");
		_test("(add-rule m if (var-changed ?xmode true) name1:'(?a) do (-> name2:'(?a)))");
		_test("(add-stmt m name1:'(a))");
		_test("(query-stmt m ?x from name2:'(?x))", "'()");
		_test("(setq m::?xmode true)");
		_test("(query-stmt m ?x from name2:'(?x))", "'(a)");

		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	public void test_7_null_expr_1() {

		_setup();
		_test("(new model m)");
		_test("(defvar ?x 1)");
		_test("(add-rule m if '(?a p ?b) (= ?b nil) do (-> m '(?a p2)))");
		_test("(add-stmt m '(x p nil))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p nil) '(x p2))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_7_null_expr_2() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p ?b) '(?c p ?b) (not (equal ?a ?c)) do (-> m name1:'(?a ?c)))");
		_test("(add-stmt m '(x p nil))");
		_test("(add-stmt m '(y p nil))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p nil) '(y p nil) name1:'(x y) name1:'(y x))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_9_stmtchange_1() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a b c1))");
		_test("(add-stmt m '(a b c2))");
		_test("(add-rule m if (stmt-changed '(?x ?y ?z1) '(?x ?y ?z2)) do (-> name1:'(?z1 ?z2)))");
		_test("(start m)");
		_test("(list-stmt m)", "");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_a_similar_expr_1() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a p ?b) (< ?b 1) do (remove-stmt ?0))");
		_test("(add-rule m if '(?x p ?y) (> ?y 5) do (remove-stmt ?0))");
		_test("(add-stmt m '(x p 0))");
		_test("(add-stmt m '(x p 2))");
		_test("(add-stmt m '(x p 6))");
		_test("(start m)");
		_test("(list-stmt m)", "'('(x p 2))");

		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	public void test_b_defvar_in_rule_body_1() {

		_setup();
		_test_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	public void test_c_action_var_tostring_1() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if n1:'(?a) do (-> n2:'(?a (to-string $(value-of (to-nonamed-list ?0))))))");
		_test("(add-stmt m n1:'(a))");
		_test("(start m)");
		_test("(list-stmt m)", "'(n1:'(a) n2:'(a \"'(a)\"))");

		_mStatus(1, "m");
		_saveTest();
	}

}
