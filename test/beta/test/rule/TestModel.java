package beta.test.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class TestModel extends RuleTestBase {

	@Test
	void test_model_0_delete_model() {

		_setup();
		_run_script();

	}

	@Test
	void test_model_0_new_model() {

		_setup();
		_run_script();
	}

	@Test
	void test_model_1_add_factor_rule_1() {

		ArrayList<IRObject> objs = new ArrayList<>();

		_setup();
		_test("(new model m)");

		try {

			RuleUtil.addRule(_model("m"), null, "'(?n typeof node)", (entry, rule, frame) -> {
				objs.add(entry.get(0));
			});

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		assertEquals(0, objs.size());

		_test("(start m)", "4");
		_test("(state-of m)", "completed");

		assertEquals(3, objs.size());

		try {
			String out = RulpUtil.toString(RulpFactory.createList(objs));
			assertEquals("'(a b c)", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_1_add_factor_rule_2() {

		ArrayList<IRObject> objs = new ArrayList<>();

		_setup();
		_test("(load \"rule\")");
		_test("(new model m)");

		try {

			RuleUtil.addRule(_model("m"), "R01", "'(?n typeof node)", (entry, rule, frame) -> {
				objs.add(entry.get(0));
			});

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		assertEquals(0, objs.size());

		_test("(start (get-rule m \"R01\"))", "4");
		_test("(state-of (get-rule m \"R01\"))", "completed");
		assertEquals(3, objs.size());

		try {
			String out = RulpUtil.toString(RulpFactory.createList(objs));
			assertEquals("'(a b c)", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_2_list_stmt_1() {

		ArrayList<IRObject> objs = new ArrayList<>();

		_setup();
		_test("(new model m)");

		IRIterator<? extends IRList> iter = null;

		try {

			iter = _model("m").buildStatementIterator(RuleUtil.toStmtFilter("'(?n typeof node)"));
			assertNotNull(iter);
			assertTrue(!iter.hasNext());

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		try {
			assertTrue(iter.hasNext());

			while (iter.hasNext()) {
				IRList stmt = iter.next();
				assertNotNull(stmt);
				objs.add(stmt.get(0));
			}

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertEquals(3, objs.size());

		try {
			String out = RulpUtil.toString(RulpFactory.createList(objs));
			assertEquals("'(a b c)", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_3_remove_stmt_after_completed() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

	@Test
	void test_model_4_stmt_listener_alpha_1() {

		_setup();
		_test("(new model m)");

		ArrayList<IRList> stmts = new ArrayList<>();
		try {
			_model("m").addStatementListener(RuleUtil.toCondList("'(?n typeof node)"), (stmt) -> {
				stmts.add(stmt);
			});
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		try {
			String out = RulpUtil.toString(RulpFactory.createList(stmts));
			assertEquals("'('(a) '(b) '(c))", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_4_stmt_listener_alpha_2() {

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a typeof node))");

		ArrayList<IRList> stmts = new ArrayList<>();
		try {
			_model("m").addStatementListener(RuleUtil.toCondList("'(?n typeof node)"), (stmt) -> {
				stmts.add(stmt);
			});
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		try {
			String out = RulpUtil.toString(RulpFactory.createList(stmts));
			assertEquals("'('(a) '(b) '(c))", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_4_stmt_listener_root_1() {

		_setup();
		_test("(new model m)");

		ArrayList<IRList> stmts = new ArrayList<>();
		try {
			_model("m").addStatementListener(RuleUtil.toCondList("'(?x ?y ?z)"), (stmt) -> {
				stmts.add(stmt);
			});
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof node))");
		_test("(add-stmt m '(b typeof node))");
		_test("(add-stmt m '(c typeof node))");

		try {
			String out = RulpUtil.toString(RulpFactory.createList(stmts));
			assertEquals("'('(a typeof node) '(b typeof node) '(c typeof node))", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_4_stmt_listener_rule_1() {

		_setup();
		_test("(new model m)");
		_test("(add-rule m if '(?a ?p ?b) '(?p p2 c) do (-> '(?b ?p ?a)))");

		ArrayList<IRList> stmts = new ArrayList<>();
		try {
			_model("m").addStatementListener(RuleUtil.toCondList("'(?x typeof ?z)"), (stmt) -> {
				stmts.add(stmt);
			});
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(add-stmt m '(a typeof b))");
		_test("(add-stmt m '(typeof p2 c))");

		try {
			String out = RulpUtil.toString(RulpFactory.createList(stmts));
			assertEquals("'('(a b))", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(start m)");

		try {
			String out = RulpUtil.toString(RulpFactory.createList(stmts));
			assertEquals("'('(a b) '(b a))", out);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_statsInfo("m");
	}

	@Test
	void test_model_5_model_vars() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_model_6_err_duplicated_new_model() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_model_7_multi_process_1_start_start_in_RHS() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_model_7_multi_process_2_query_query_in_RHS() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_model_7_multi_process_3_query_in_lhs() {

		_setup();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
	}
	
	@Test
	void test_model_8_var_override_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
