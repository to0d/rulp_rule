package beta.rulp.rule;

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
	public void test_0_p2d_ls() {

		_setup();
		_test_match("(ls-print root)", "nil", "result/rule/TestModel/p2d_ls_root.txt");
		_test("(ls-print system)", "nil", _load("result/rule/TestModel/p2d_ls_system.txt") + "\n");
		_test("(ls-print main)", "nil", _load("result/rule/TestModel/p2d_ls_main_1.txt") + "\n");
		_test("(load \"result/p2d.rulp\")");
		_test("(ls-print main)", "nil", _load("result/rule/TestModel/p2d_ls_main_2.txt") + "\n");
	}

	@Test
	void test_1_add_factor_rule_1() {

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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_1_add_factor_rule_2() {

		ArrayList<IRObject> objs = new ArrayList<>();

		_setup();
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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_2_list_stmt_1() {

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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_3_remove_stmt_after_completed() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(new model m)");
		_test("(add-rule m if (var-changed ?model-state running completed) '(?a hasChild ?b) '(?b hasChild ?c) '(?a hasChild ?c) (not-equal ?a ?b) (not-equal ?b ?c) (not-equal ?a ?c) do (remove-stmt '(?b hasChild ?c)))");

		_test("(add-stmt m '(a hasChild b))");
		_test("(add-stmt m '(b hasChild c))");
		_test("(add-stmt m '(a hasChild c))");

		_test("(start m)", "11");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a hasChild b) '(a hasChild c))");

		_mStatus(1, "m");
		_saveTest();

	}

	@Test
	void test_4_stmt_listener_alpha_1() {

		ArrayList<IRList> stmts = new ArrayList<>();

		_setup();
		_test("(new model m)");

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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_4_stmt_listener_alpha_2() {

		ArrayList<IRList> stmts = new ArrayList<>();

		_setup();
		_test("(new model m)");
		_test("(add-stmt m '(a typeof node))");

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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_4_stmt_listener_root_1() {

		ArrayList<IRList> stmts = new ArrayList<>();

		_setup();
		_test("(new model m)");

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

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_5_model_vars() {

		_setup();
		_test("(new model m)");
		_test("(value-of (value-of m::?model-state))", "completed");
		_test("(value-of (value-of m::?str-sql-init))", "false");

		_test("(defvar ?m m)");
		_test("(value-of (value-of m::?model-state))", "completed");
		_test("(value-of (value-of m::?str-sql-init))", "false");
	}
}
