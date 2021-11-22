package beta.rulp.utils;

import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class ReteUtilTest extends RuleTestBase {

	protected void _test_buildVarList(String inputTree, String expectVarList) {
		try {

			LinkedList<IRList> treeList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
				treeList.add(RulpUtil.asList(obj));
			}

			assertEquals(inputTree, 1, treeList.size());

			IRList tree = treeList.get(0);
			ArrayList<IRObject> vars = ReteUtil.buildVarList(tree);
			assertEquals(inputTree, expectVarList, vars.toString());

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _test_entry_uniqname(String inputTree, String expectName) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
				stmtList.add(RulpUtil.asList(obj));
			}

			assertEquals(1, stmtList.size());
			IRList tree = stmtList.get(0);

			IRObject entry[] = new IRObject[tree.size()];
			for (int i = 0; i < entry.length; ++i) {
				entry[i] = tree.get(i);
			}

			assertTrue(inputTree, ReteUtil.isReteTree(tree));

			String entryName = ReteUtil.uniqName(entry);
			assertEquals(inputTree, expectName, entryName);

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _test_isReteStmt(String input, boolean expect) {

		try {

			List<IRObject> stmtList = _getParser().parse(input);
			assertEquals(1, stmtList.size());
			boolean rc = ReteUtil.isReteStmt(RulpUtil.asList(stmtList.get(0)));
			assertEquals(String.format("input=%s", input), expect, rc);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _test_isUniqReteStmt(String inputStmt, boolean expect) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : this._getParser().parse(inputStmt)) {
				stmtList.add(RulpUtil.asList(obj));
			}

			assertEquals(1, stmtList.size());
			IRList stmt = stmtList.get(0);

			assertEquals(inputStmt, expect, ReteUtil.isUniqReteStmt(stmt));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	protected void _test_matchUniqStmt(String inputSrcStmt, String inputDstStmt, boolean match) {

		try {

			IRList srcStmt = null;
			IRList dstStmt = null;

			{
				LinkedList<IRList> stmtList = new LinkedList<>();
				for (IRObject obj : this._getParser().parse(inputSrcStmt)) {
					stmtList.add(RulpUtil.asList(obj));
				}

				assertEquals(1, stmtList.size());
				srcStmt = stmtList.get(0);
			}

			{
				LinkedList<IRList> stmtList = new LinkedList<>();
				for (IRObject obj : this._getParser().parse(inputDstStmt)) {
					stmtList.add(RulpUtil.asList(obj));
				}

				assertEquals(1, stmtList.size());
				dstStmt = stmtList.get(0);
			}

			assertEquals(match, ReteUtil.matchUniqStmt(srcStmt, dstStmt));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

//	protected void _test_tree_uniqname_stmt(String inputTree,  String expectName) {
//
//		try {
//
//			LinkedList<IRList> stmtList = new LinkedList<>();
//			for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
//				stmtList.add(RulpUtil.asList(obj));
//			}
//
//			assertEquals(1, stmtList.size());
//			IRList stmt = stmtList.get(0);
//
//			assertTrue(inputTree, ReteUtility.isReteStmtNoVar(stmt));
//
//			String treeName = ReteUtility.uniqName(stmt);
//			assertEquals(inputTree, expectName, treeName);
//
//		} catch (RException e) {
//			e.printStackTrace();
//			fail(e.toString());
//		}
//	}

	protected void _test_tree_uniqname(String inputTree, String expectName) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
				stmtList.add(RulpUtil.asList(obj));
			}

			assertEquals(1, stmtList.size());
			IRList tree = stmtList.get(0);

			assertTrue(inputTree, ReteUtil.isReteTree(tree));

			String treeName = ReteUtil.uniqName(tree);
			assertEquals(inputTree, expectName, treeName);

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_buildVarList() {
		_setup();
		_test_buildVarList("'(a ?x b)", "[?x]");
		_test_buildVarList("'(a ?x ?y)", "[?x, ?y]");
		_test_buildVarList("'(?y ?x ?y)", "[?y, ?x]");
		_test_buildVarList("'(? ?y ?)", "[?y]");
		_test_buildVarList("'('(?p1 p2 c) '(?x1 ?p1 ?x2))", "[?p1, ?x1, ?x2]");
		_test_buildVarList("'('('(?y ?p2 ?z) '('(a ?p1 b) '('(?x ?p1 b) '(?x ?p2 b)))))", "[?y, ?p2, ?z, ?p1, ?x]");
		_test_buildVarList("'('(?p1 ?p2 ?p3))", "[?p1, ?p2, ?p3]");
		_test_buildVarList("'('(?x ?p2 ?x))", "[?x, ?p2]");
	}

	@Test
	void test_entry_uniqname() {
		_setup();
		_test_entry_uniqname("'(?p1 p2 c)", "?0 p2 c");
		_test_entry_uniqname("'(?x ?x z)", "?0 ?0 z");
		_test_entry_uniqname("'(?x ?y z)", "?0 ?1 z");
	}

	@Test
	void test_getReteStatus() {
		_setup();
		assertEquals(DEFINE, ReteUtil.getReteStatus(DEFINE, DEFINE));
		assertEquals(DEFINE, ReteUtil.getReteStatus(DEFINE, REASON));
		assertEquals(DEFINE, ReteUtil.getReteStatus(DEFINE, ASSUME));
		assertEquals(REMOVE, ReteUtil.getReteStatus(DEFINE, REMOVE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(DEFINE, FIXED_));
		assertEquals(DEFINE, ReteUtil.getReteStatus(DEFINE, TEMP__));

		assertEquals(DEFINE, ReteUtil.getReteStatus(REASON, DEFINE));
		assertEquals(REASON, ReteUtil.getReteStatus(REASON, REASON));
		assertEquals(REASON, ReteUtil.getReteStatus(REASON, ASSUME));
		assertEquals(REMOVE, ReteUtil.getReteStatus(REASON, REMOVE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(REASON, FIXED_));
		assertEquals(REASON, ReteUtil.getReteStatus(REASON, TEMP__));

		assertEquals(DEFINE, ReteUtil.getReteStatus(ASSUME, DEFINE));
		assertEquals(REASON, ReteUtil.getReteStatus(ASSUME, REASON));
		assertEquals(ASSUME, ReteUtil.getReteStatus(ASSUME, ASSUME));
		assertEquals(REMOVE, ReteUtil.getReteStatus(ASSUME, REMOVE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(ASSUME, FIXED_));
		assertEquals(ASSUME, ReteUtil.getReteStatus(ASSUME, TEMP__));

		assertEquals(REMOVE, ReteUtil.getReteStatus(REMOVE, DEFINE));
		assertEquals(REMOVE, ReteUtil.getReteStatus(REMOVE, REASON));
		assertEquals(REMOVE, ReteUtil.getReteStatus(REMOVE, ASSUME));
		assertEquals(REMOVE, ReteUtil.getReteStatus(REMOVE, REMOVE));
		assertEquals(null, ReteUtil.getReteStatus(REMOVE, FIXED_));
		assertEquals(REMOVE, ReteUtil.getReteStatus(REMOVE, TEMP__));

		assertEquals(FIXED_, ReteUtil.getReteStatus(FIXED_, DEFINE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(FIXED_, REASON));
		assertEquals(FIXED_, ReteUtil.getReteStatus(FIXED_, ASSUME));
		assertEquals(null, ReteUtil.getReteStatus(FIXED_, REMOVE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(FIXED_, FIXED_));
		assertEquals(FIXED_, ReteUtil.getReteStatus(FIXED_, TEMP__));

		assertEquals(DEFINE, ReteUtil.getReteStatus(TEMP__, DEFINE));
		assertEquals(REASON, ReteUtil.getReteStatus(TEMP__, REASON));
		assertEquals(ASSUME, ReteUtil.getReteStatus(TEMP__, ASSUME));
		assertEquals(REMOVE, ReteUtil.getReteStatus(TEMP__, REMOVE));
		assertEquals(FIXED_, ReteUtil.getReteStatus(TEMP__, FIXED_));
		assertEquals(TEMP__, ReteUtil.getReteStatus(TEMP__, TEMP__));
	}

	@Test
	void test_isReteStmt() {

		_setup();

		_test_isReteStmt("'(a b c)", true);
		_test_isReteStmt("'(a b 1)", true);
		_test_isReteStmt("'(a b 1.1)", true);
		_test_isReteStmt("'(a b \"str\")", true);
		_test_isReteStmt("'(a b 1L)", true);
		_test_isReteStmt("'(a b 1.1d)", true);

		_test_isReteStmt("'(a 1 c)", false);
		_test_isReteStmt("'(a 1.1 c)", false);
		_test_isReteStmt("'(a \"str\" c)", false);
		_test_isReteStmt("'(a 1L c)", false);
		_test_isReteStmt("'(a 1.1d c)", false);

		_test_isReteStmt("'(a b c d)", true);
		_test_isReteStmt("'(a b c 1)", true);
		_test_isReteStmt("'(a b c 1.1)", true);
		_test_isReteStmt("'(a b c \"str\")", true);
		_test_isReteStmt("'(a b c 1L)", true);
		_test_isReteStmt("'(a b c 1.1d)", true);

		_test_isReteStmt("'(a b c d e)", true);
		_test_isReteStmt("'(a b c d 1)", true);
		_test_isReteStmt("'(a b c d 1.1)", true);
		_test_isReteStmt("'(a b c d \"str\")", true);
		_test_isReteStmt("'(a b c d 1L)", true);
		_test_isReteStmt("'(a b c d 1.1d)", true);
	}

	@Test
	void test_isUniqReteStmt() {

		_setup();

		_test_isUniqReteStmt("'(?0 ?1 ?2)", true);
		_test_isUniqReteStmt("'(?0 ?1 ?1)", true);
		_test_isUniqReteStmt("'(?1 ?1 ?1)", false);
		_test_isUniqReteStmt("'(?0 ?0 ?0)", true);
		_test_isUniqReteStmt("'(?0 ?1 ?0)", true);
		_test_isUniqReteStmt("'(?1 ?0 ?1)", false);
		_test_isUniqReteStmt("'(?0 a ?2)", false);
		_test_isUniqReteStmt("'(?0 a ?1)", true);
		_test_isUniqReteStmt("'(a a ?1)", false);
		_test_isUniqReteStmt("'(a a ?0)", true);
	}

	@Test
	void test_matchReteStatus() {

		_setup();

		assertFalse(ReteUtil.matchReteStatus(DEFINE, 0));
		assertFalse(ReteUtil.matchReteStatus(REASON, 0));
		assertFalse(ReteUtil.matchReteStatus(ASSUME, 0));
		assertFalse(ReteUtil.matchReteStatus(REMOVE, 0));
		assertFalse(ReteUtil.matchReteStatus(FIXED_, 0));
		assertFalse(ReteUtil.matchReteStatus(TEMP__, 0));

		assertTrue(ReteUtil.matchReteStatus(DEFINE, 1));
		assertTrue(ReteUtil.matchReteStatus(REASON, 2));
		assertTrue(ReteUtil.matchReteStatus(ASSUME, 4));
		assertTrue(ReteUtil.matchReteStatus(REMOVE, 8));
		assertTrue(ReteUtil.matchReteStatus(FIXED_, 16));
		assertTrue(ReteUtil.matchReteStatus(TEMP__, 32));

		assertTrue(ReteUtil.matchReteStatus(DEFINE, 63));
		assertTrue(ReteUtil.matchReteStatus(REASON, 63));
		assertTrue(ReteUtil.matchReteStatus(ASSUME, 63));
		assertTrue(ReteUtil.matchReteStatus(REMOVE, 63));
		assertTrue(ReteUtil.matchReteStatus(FIXED_, 63));
		assertTrue(ReteUtil.matchReteStatus(TEMP__, 63));
	}

	@Test
	void test_matchUniqStmt() {
		_setup();
		_test_matchUniqStmt("'(a b c)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(a a ?0)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(a ?0 ?0)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(a ?0 ?1)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(?0 b c)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(a ?0 c)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(?0 ?1 ?2)", "'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("'(?0 ?1 ?2)", "'(a b c)", true);
		_test_matchUniqStmt("'(?0 ?1 ?2)", "'(a ?0 c)", true);
		_test_matchUniqStmt("'(?0 ?1 ?2)", "'(a b ?0)", true);
		_test_matchUniqStmt("'(?0 ?1 ?2)", "'(?0 ?1 c)", true);
		_test_matchUniqStmt("'(a b c)", "'(?0 ?1 c)", true);
		_test_matchUniqStmt("'(?0 ?0 a)", "'(?0 b ?0)", true);
		_test_matchUniqStmt("'(?0 ?0 ?c)", "'(a b ?0)", false);

		_test_matchUniqStmt("name1:'(a b c)", "name1:'(?0 ?1 ?2)", true);
		_test_matchUniqStmt("name1:'(a b c)", "'(?0 ?1 ?2)", false);
	}

	@Test
	void test_tree_uniqname() {
		_setup();
		_test_tree_uniqname("'('(?p1 p2 c) '(?x1 ?p1 ?x2))", "'('(?0 p2 c) '(?1 ?0 ?2))");

		_test_tree_uniqname("'('('(?y ?p2 ?z) '('(a ?p1 b) '('(?x ?p1 b) '(?x ?p2 b)))))",
				"'('('(?0 ?1 ?2) '('(a ?3 b) '('(?4 ?3 b) '(?4 ?1 b)))))");

		_test_tree_uniqname("'('(?p1 ?p2 ?p3))", "'('(?0 ?1 ?2))");

		_test_tree_uniqname("'('(?x ?p2 ?x))", "'('(?0 ?1 ?0))");

		_test_tree_uniqname("'('(?x ?x ?x))", "'('(?0 ?0 ?0))");

		_test_tree_uniqname("'('(?x ?x ?x ?x))", "'('(?0 ?0 ?0 ?0))");
	}

//	@Test
//	void test_tree_uniqName_stmt() {
//
//		_test_tree_uniqname_stmt("'(a b c)", 0, "a b c");
//		_test_tree_uniqname_stmt("'(a b c)", 1, "b a c");
//		_test_tree_uniqname_stmt("'(a b c)", 2, "c a b");
//	}

	@Test
	void test_tree_uniqname_with_expr() {

		_setup();

		_test_tree_uniqname("'('(?p1 p2 c) (not-equal ?p1 a))", "'('(?0 p2 c) (not-equal ?0 a))");
	}

	@Test
	void test_tree_uniqname_with_external_var() {

		_setup();

		_test_tree_uniqname("'('(?a ?b ?c) (> ?c ?x))", "'('(?0 ?1 ?2) (> ?2 ?x))");
		_test_tree_uniqname("'('('(?a1 p1 ?b1) (!= ?a1 ?x2)) (!= ?a1 ?x1))",
				"'('('(?0 p1 ?1) (!= ?0 ?x2)) (!= ?0 ?x1))");

	}

	@Test
	void test_tree_uniqname_with_var_expression() {
		_setup();
		_test_tree_uniqname("'('(?p1 p2 ?v) (var-changed ?s1 v1 ?v))", "'('(?0 p2 ?1) (var-changed ?s1 v1 ?s1.new))");
	}

	@Test
	void test_tree_uniqVarList() {
		_setup();

	}

	@Test
	void test_updateMask() {
		_setup();
		assertEquals(1, ReteUtil.updateMask(DEFINE, 0));
		assertEquals(2, ReteUtil.updateMask(REASON, 0));
		assertEquals(4, ReteUtil.updateMask(ASSUME, 0));
		assertEquals(8, ReteUtil.updateMask(REMOVE, 0));
	}

}
