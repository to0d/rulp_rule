package beta.rulp.utils;

import static alpha.rulp.rule.RReteStatus.ASSUMED;
import static alpha.rulp.rule.RReteStatus.DEFINED;
import static alpha.rulp.rule.RReteStatus.REASONED;
import static alpha.rulp.rule.RReteStatus.REMOVED;
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

class ReteUtilityTest extends RuleTestBase {

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

	protected void _test_tree_uniqVarList(String inputTree, String expectVarList) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
				stmtList.add(RulpUtil.asList(obj));
			}

			assertEquals(1, stmtList.size());
			IRList tree = stmtList.get(0);

			assertTrue(inputTree, ReteUtil.isReteTree(tree));

			String varList = ReteUtil.uniqVarList(tree).toString();
			assertEquals(inputTree, expectVarList, varList);

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_buildVarList() {
		_test_buildVarList("'(a ?x b)", "[?x]");
		_test_buildVarList("'(a ?x ?y)", "[?x, ?y]");
		_test_buildVarList("'(?y ?x ?y)", "[?y, ?x]");
		_test_buildVarList("'(? ?y ?)", "[?y]");
	}

	@Test
	void test_entry_uniqname() {

		_test_entry_uniqname("'(?p1 p2 c)", "?0 p2 c");
		_test_entry_uniqname("'(?x ?x z)", "?0 ?0 z");
		_test_entry_uniqname("'(?x ?y z)", "?0 ?1 z");
	}

	@Test
	void test_getReteStatus() {

		assertEquals(DEFINED, ReteUtil.getReteStatus(DEFINED, DEFINED));
		assertEquals(DEFINED, ReteUtil.getReteStatus(DEFINED, REASONED));
		assertEquals(DEFINED, ReteUtil.getReteStatus(DEFINED, ASSUMED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(DEFINED, REMOVED));

		assertEquals(DEFINED, ReteUtil.getReteStatus(REASONED, DEFINED));
		assertEquals(REASONED, ReteUtil.getReteStatus(REASONED, REASONED));
		assertEquals(REASONED, ReteUtil.getReteStatus(REASONED, ASSUMED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(REASONED, REMOVED));

		assertEquals(DEFINED, ReteUtil.getReteStatus(ASSUMED, DEFINED));
		assertEquals(REASONED, ReteUtil.getReteStatus(ASSUMED, REASONED));
		assertEquals(ASSUMED, ReteUtil.getReteStatus(ASSUMED, ASSUMED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(ASSUMED, REMOVED));

		assertEquals(REMOVED, ReteUtil.getReteStatus(REMOVED, DEFINED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(REMOVED, REASONED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(REMOVED, ASSUMED));
		assertEquals(REMOVED, ReteUtil.getReteStatus(REMOVED, REMOVED));

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

		assertFalse(ReteUtil.matchReteStatus(DEFINED, 0));
		assertFalse(ReteUtil.matchReteStatus(REASONED, 0));
		assertFalse(ReteUtil.matchReteStatus(ASSUMED, 0));
		assertFalse(ReteUtil.matchReteStatus(REMOVED, 0));

		assertTrue(ReteUtil.matchReteStatus(DEFINED, 1));
		assertTrue(ReteUtil.matchReteStatus(REASONED, 2));
		assertTrue(ReteUtil.matchReteStatus(ASSUMED, 4));
		assertTrue(ReteUtil.matchReteStatus(REMOVED, 8));

		assertTrue(ReteUtil.matchReteStatus(DEFINED, 15));
		assertTrue(ReteUtil.matchReteStatus(REASONED, 15));
		assertTrue(ReteUtil.matchReteStatus(ASSUMED, 15));
		assertTrue(ReteUtil.matchReteStatus(REMOVED, 15));
	}

	@Test
	void test_matchUniqStmt() {

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

		_test_tree_uniqname("'('(?p1 p2 c) (not-equal ?p1 a))", "'('(?0 p2 c) (not-equal ?0 a))");
	}

	@Test
	void test_tree_uniqname_with_var_expression() {

		_test_tree_uniqname("'('(?p1 p2 ?v) (var-changed ?s1 v1 ?v))", "'('(?0 p2 ?1) (var-changed ?s1 v1 ?1))");
	}

	@Test
	void test_tree_uniqVarList() {

		_test_tree_uniqVarList("'('(?p1 p2 c) '(?x1 ?p1 ?x2))", "[?p1, ?x1, ?x2]");
		_test_tree_uniqVarList("'('('(?y ?p2 ?z) '('(a ?p1 b) '('(?x ?p1 b) '(?x ?p2 b)))))", "[?y, ?p2, ?z, ?p1, ?x]");
		_test_tree_uniqVarList("'('(?p1 ?p2 ?p3))", "[?p1, ?p2, ?p3]");
		_test_tree_uniqVarList("'('(?x ?p2 ?x))", "[?x, ?p2]");
	}

	@Test
	void test_updateMask() {
		assertEquals(1, ReteUtil.updateMask(DEFINED, 0));
		assertEquals(2, ReteUtil.updateMask(REASONED, 0));
		assertEquals(4, ReteUtil.updateMask(ASSUMED, 0));
		assertEquals(8, ReteUtil.updateMask(REMOVED, 0));
	}

}
