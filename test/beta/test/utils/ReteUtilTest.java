package beta.test.utils;

import static alpha.rulp.rule.RReteStatus.ASSUME;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.FIXED_;
import static alpha.rulp.rule.RReteStatus.REASON;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

	protected String _test_buildVarList(String inputTree) throws RException {

		LinkedList<IRList> treeList = new LinkedList<>();
		for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
			treeList.add(RulpUtil.asList(obj));
		}

		assertEquals(inputTree, 1, treeList.size());

		return ReteUtil.buildVarList(treeList.get(0)).toString();

	}

	protected String _test_entry_uniqname(String inputTree) throws RException {

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

		return ReteUtil.uniqName(entry);

	}

	protected String _test_isReteStmt(String input) throws RException {

		List<IRObject> stmtList = _getParser().parse(input);
		assertEquals(1, stmtList.size());
		return "" + ReteUtil.isReteStmt(RulpUtil.asList(stmtList.get(0)));
	}

	protected String _test_isUniqReteStmt(String inputStmt) throws RException {

		LinkedList<IRList> stmtList = new LinkedList<>();
		for (IRObject obj : this._getParser().parse(inputStmt)) {
			stmtList.add(RulpUtil.asList(obj));
		}

		assertEquals(1, stmtList.size());
		IRList stmt = stmtList.get(0);

		return "" + ReteUtil.isUniqReteStmt(stmt);
	}

	protected String _test_matchUniqStmt(String input) throws RException {

		IRList srcStmt = null;
		IRList dstStmt = null;

		LinkedList<IRList> stmtList = new LinkedList<>();
		for (IRObject obj : this._getParser().parse(input)) {
			stmtList.add(RulpUtil.asList(obj));
		}

		assertEquals(1, stmtList.size());

		IRList tree = stmtList.get(0);
		assertEquals(2, tree.size());

		srcStmt = RulpUtil.asList(tree.get(0));
		dstStmt = RulpUtil.asList(tree.get(1));

		return "" + ReteUtil.matchUniqStmt(srcStmt, dstStmt);

	}

	String _test_tree_uniqname(String inputTree) throws RException {

		LinkedList<IRList> stmtList = new LinkedList<>();
		for (IRObject obj : RulpFactory.createParser().parse(inputTree)) {
			stmtList.add(RulpUtil.asList(obj));
		}

		assertEquals(1, stmtList.size());
		IRList tree = stmtList.get(0);

		return ReteUtil.uniqName(tree);
	}

	@Test
	void test_buildVarList() {

		_setup();

		_test((input) -> {
			return _test_buildVarList(input);
		});

	}

	@Test
	void test_entry_uniqname() {

		_setup();

		_test((input) -> {
			return _test_entry_uniqname(input);
		});

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

		_test((input) -> {
			return _test_isReteStmt(input);
		});

	}

	@Test
	void test_isUniqReteStmt() {

		_setup();

		_test((input) -> {
			return _test_isUniqReteStmt(input);
		});

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
	void test_match_uniq_stmt_1() {

		_setup();

		_test((input) -> {
			return _test_matchUniqStmt(input);
		});

	}

	@Test
	void test_match_uniq_stmt_2_named() {

		_setup();

		_test((input) -> {
			return _test_matchUniqStmt(input);
		});

	}

	@Test
	void test_tree_uniqname_1() {

		_setup();

		_test((input) -> {
			return _test_tree_uniqname(input);
		});

	}

	@Test
	void test_tree_uniqname_2_expr() {

		_setup();

		_test((input) -> {
			return _test_tree_uniqname(input);
		});

	}

	@Test
	void test_tree_uniqname_3_external_var() {

		_setup();

		_test((input) -> {
			return _test_tree_uniqname(input);
		});

	}

	@Test
	void test_tree_uniqname_4_var_expression() {

		_setup();

		_test((input) -> {
			return _test_tree_uniqname(input);
		});

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
