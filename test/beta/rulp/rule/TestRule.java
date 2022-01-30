package beta.rulp.rule;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRError;
import alpha.rulp.lang.IRList;
import alpha.rulp.utils.RuleTestBase;

public class TestRule extends RuleTestBase {

	@Test
	void test_rule_0() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_0_default_model() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_0_error_rule_1() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"R1\" m if '(?a p1 ?b) do (if (equal ?a ?b) (throw err-r1 '(?a ?b) )) (-> '(?b p1 ?a)))");
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
	void test_rule_0_error_rule_halt() {

		// XRModel.TRACE_RETE = true;
		_setup();
		_test("(new model m)");
		_test("(add-rule \"R1\" m if '(?a p1 ?b) do (if (equal ?a ?b) (throw err-r1 '(?a ?b))) (-> '(?b p1 ?a)))");
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
	void test_rule_0_similiar_match_alpha_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_rStatus(1, "m", "RA001");
		_rStatus(1, "m", "RA002");
		_saveTest();

	}

	@Test
	void test_rule_0_similiar_match_alpha_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_0_similiar_match_beta() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_1_forward() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_2_inverse_add_rule_after_stmt() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_2_inverse_add_rule_before_stmt() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_3_variable_entry_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_3_variable_entry_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_4_const_expr_1() {

		_setup();
		_run_script();
	}

	@Test
	void test_rule_4_const_expr_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_4_const_expr_3() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_4_const_expr_4() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_4_const_stmt() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_4_redunt_beta() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_5_waste_var_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_5_waste_var_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_5_waste_var_3() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_3() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_4() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_5() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_6() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_7() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_8() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_9() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_6_var_expr_a() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_7_null_expr_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_7_null_expr_2() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_9_stmtchange_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_a_similar_expr_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_b_defvar_in_rule_body_1() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}

	@Test
	void test_rule_c_action_var_tostring_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_rule_d_type_expr_1() {

		_setup();
		_run_script();
		_mStatus(1, "m");
		_saveTest();
		_statsInfo("m");
	}

	@Test
	void test_rule_e_fun_1() {

		_setup();
		_run_script();
	}
}
