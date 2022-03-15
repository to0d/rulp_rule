package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestOptimize extends RuleTestBase {

	@Test
	void test_opt_1_alpha_match_expr() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_2_bad_expr_1_bigger() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_2_bad_expr_2_equal_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_opt_3_beta_join_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_4_beta_join_same_node_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_4_beta_join_same_node_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_3_named() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_5_equal_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_1_orignal() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_3() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_4() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_5() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_6_not_equal_6() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_7_p2d_tag_alias_1_full_with_single_child() {

		_setup();

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tc))");

		_nodeInfo("p2d", "result/rule/TestOptimize/test_opt_7_p2d_tag_alias_1_full_with_single_child_n1.txt");
		_test("(opt-model p2d)", "1");
		_nodeInfo("p2d", "result/rule/TestOptimize/test_opt_7_p2d_tag_alias_1_full_with_single_child_n2.txt");

		_test("(start p2d)", "273");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasAliasTag ?y))", "'('(ta nm:hasAliasTag tb) '(ta nm:hasAliasTag tc))");
		_test("(list-stmt p2d from '(?x nm:beAliasTo ?y))", "'('(tb nm:beAliasTo ta) '(tc nm:beAliasTo ta))");
		_test("(list-stmt p2d from '(?x nm:typeOf nm:tag))",
				"'('(tb nm:typeOf nm:tag) '(ta nm:typeOf nm:tag) '(tc nm:typeOf nm:tag))");

		_statsInfo("p2d");
		_dumpEntryTable("p2d");
	}

	@Test
	void test_opt_8_unused_left_var() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_opt_9_has_stmt_2() {

		_setup();
		_run_script();
		_statsInfo("m");

	}
}
