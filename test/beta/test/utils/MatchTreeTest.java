package beta.test.utils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.FormatUtil;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class MatchTreeTest extends RuleTestBase {

	protected void _test_buildMatchTree() {
		_test_buildMatchTree(getCachePath() + ".in.rulp", getCachePath() + ".out.rulp");
	}

	protected void _test_buildMatchTree(String inputFile, String outputFile) {

		try {

			IRInterpreter interpreter = _getInterpreter();
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

				outLines.add(inputStmt + " ; input");

				try {
					IRList tree = MatchTree.build(stmtList, interpreter, interpreter.getMainFrame());
					String treeOutput = RulpUtil.toString(tree);
					outLines.add(treeOutput + " ; output");
					outLines.add(";expr: ");
					ArrayList<String> formatLines = new ArrayList<>();
					formatLines.add(treeOutput);
					outLines.addAll(FormatUtil.format(formatLines));
				} catch (RException e) {
					outLines.add(";err:" + e.getMessage());
				}

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
		_test_buildMatchTree();
	}

	@Test
	void test_2_beta3() {

		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_3_error() {

		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_4_order() {

		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_5_const_stmt() {

		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_6_expr() {

		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_7_var_expr() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_8_var_changed() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_9_stmt_changed() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_a_force_node() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_b_optimize() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_c_external_var() {
		_setup();
		_test_buildMatchTree();
	}

	@Test
	void test_d_special() {
		_setup();
		_test_buildMatchTree();
	}

}
