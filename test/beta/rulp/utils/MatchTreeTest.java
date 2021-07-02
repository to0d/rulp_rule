package beta.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class MatchTreeTest extends RuleTestBase {

	protected void _test_buildMatchTree(String matchList, String expectTree) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(matchList)) {
				stmtList.add(ReteUtil.asReteStmt(obj));
			}

			IRList tree = MatchTree.build(stmtList);
			assertTrue(matchList, ReteUtil.isReteTree(tree));

			String treeOutput = RulpUtil.toString(tree);
			assertEquals(String.format("input=%s", matchList), expectTree, treeOutput);

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}

	protected void _test_buildMatchTree_error(String matchList, String expectException) {

		try {

			LinkedList<IRList> stmtList = new LinkedList<>();
			for (IRObject obj : RulpFactory.createParser().parse(matchList)) {
				stmtList.add(ReteUtil.asReteStmt(obj));
			}

			MatchTree.build(stmtList);
			fail("should fail");

		} catch (RException e) {
			assertEquals(String.format("input=%s", matchList), expectException, e.getMessage());
		}

	}

	protected void _test_buildMatchTree_From_File(String inputFile, String outputFile) {

		try {

			ArrayList<String> outLines = new ArrayList<>();

			for (String inputStmt : FileUtil.openTxtFile(inputFile, "utf-8")) {

				if (inputStmt.trim().isEmpty() || inputStmt.trim().startsWith(";")) {
					outLines.add(inputStmt);
					continue;
				}

				LinkedList<IRList> stmtList = new LinkedList<>();
				for (IRObject obj : RulpFactory.createParser().parse(inputStmt)) {
					stmtList.add(ReteUtil.asReteStmt(obj));
				}

				IRList tree = MatchTree.build(stmtList);
//				assertTrue(inputStmt, ReteUtility.isReteTree(tree));

				String treeOutput = RulpUtil.toString(tree);
				outLines.add(inputStmt + " ; input");
				outLines.add(treeOutput + " ; output");
				outLines.add(";expr: ");

				ArrayList<String> formatLines = new ArrayList<>();
				formatLines.add(treeOutput);

				outLines.addAll(FormatUtil.format(formatLines));
				outLines.add("\r\n");

			}

			FileUtil.saveTxtFile(outputFile, outLines, "utf-8");

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}

	}

	@Test
	void test_1() {

		_setup();

		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_1.in.rulp",
				"result/utils/MatchTreeTest/test_1.out.rulp");

	}

	@Test
	void test_2_beta3() {
		
		_setup();
		
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_2_beta3.in.rulp",
				"result/utils/MatchTreeTest/test_2_beta3.out.rulp");

	}

	@Test
	void test_3_error() {
		
		_setup();
		
		_test_buildMatchTree_error("'(?x ?y ?z) '(?a ?b ?c) (!= ?0 ?1)",
				"Invalid index var<?0> found in: ['(?x ?y ?z), '(?a ?b ?c), (!= ?0 ?1)]");

	}

	@Test
	void test_4_order() {
		
		_setup();
		
		_test_buildMatchTree("'(?a p1 ?b) '(?a ?p ?b) '(?p p2 c)", "'('('(?a ?p ?b) '(?p p2 c)) '(?a p1 ?b))");
		_test_buildMatchTree("'(?a p1 ?b) '(?p p2 c) '(?a ?p ?b)", "'('('(?a ?p ?b) '(?p p2 c)) '(?a p1 ?b))");
	}

	@Test
	void test_5_const_stmt() {
		
		_setup();
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_5_const_stmt.in.rulp",
				"result/utils/MatchTreeTest/test_5_const_stmt.out.rulp");
	}

	@Test
	void test_6_expr() {
		
		_setup();
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_6_expr.in.rulp",
				"result/utils/MatchTreeTest/test_6_expr.out.rulp");
	}

	@Test
	void test_7_var_expr() {
		_setup();
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_7_var_expr.in.rulp",
				"result/utils/MatchTreeTest/test_7_var_expr.out.rulp");
	}

	@Test
	void test_8_var_changed() {
		_setup();
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_8_var_changed.in.rulp",
				"result/utils/MatchTreeTest/test_8_var_changed.out.rulp");
	}

	@Test
	void test_9_stmt_changed() {
		_setup();
		_test_buildMatchTree_From_File("result/utils/MatchTreeTest/test_9_stmt_changed.in.rulp",
				"result/utils/MatchTreeTest/test_9_stmt_changed.out.rulp");
	}

}
