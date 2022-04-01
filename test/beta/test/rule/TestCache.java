package beta.test.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import alpha.common.file.FileUtil;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.cache.IRStmtLoader;

public class TestCache extends RuleTestBase {

	@Test
	void test_cache_clean_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_has_stmt_1() {

		_setup();
		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_has_stmt_1\")");
		_test("(has-stmt m n2:'(x y ?z))", "true");
		_statsInfo("m");
	}

	@Test
	void test_cache_model_1a() {

		_setup();

		_clean_model_cache();
		_test("(new model m)");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_save_model_cache("m");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_1b() {

		_setup();

		_clean_model_cache();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_1b\")");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(save-model m)", "2");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_2() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_2\")");
		_test("(save-model m)", "0");
		_test("(list-stmt m)", "'('(a b c) name2:'(x y z))");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_3() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_3\")");
		_test("(save-model m)", "0");
		_test("(list-stmt m from name2:'(?x ?y ?z))", "'(name2:'(x y z))");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_4a() {

		_setup();

		// XRModel.TRACE_RETE = true;
		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();

		_test("(new model m)");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name3:'(?x ?y ?z)) )");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_4\")");
		_test("(start m)");
		_test("(list-stmt m from name3:'(?x ?y ?z))", "'(name3:'(a b c))");
		_test("(save-model m)", "1");

		_statsInfo("m");
		_dumpEntryTable("m");

		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();
	}

	@Test
	void test_cache_model_4b() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_4\")");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name3:'(?x ?y ?z)) )");
		_test("(query-stmt m '(?x ?y ?z) from name3:'(?x ?y ?z))", "'('(a b c))");

		_test("(save-model m)", "1");
		_statsInfo("m");

		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();
	}

	@Test
	void test_cache_model_5() {

		_setup();

		new File("result/cache/CacheTest/test_cache_model_5/.3.mc").delete();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_5\")");
		_test("(add-stmt m '(x y z))");
		_test("(save-model m)", "1");
		_test("(delete m)");

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_5\")");
		_test("(remove-stmt m '(x y z))");
		_test("(save-model m)", "0");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_6_list() {

		_setup();

		_clean_model_cache();
		_test("(new model m)");
		_test("(add-stmt m n1:'(a '(b c)))");
		_save_model_cache("m");

		_statsInfo("m");
	}

	@Test
	void test_cache_model_7_pre_auto_encode() throws IOException {

		_setup();

		// Copy folder
		String testFolder = "result/rule/TestCache/test_7";
		FileUtil.deleteFile(new File(testFolder));
		new File(testFolder).mkdirs();
		FileUtil.copyFolder(new File("result/rule/TestCache/test_cache_model_7_pre"), new File(testFolder));

		_test("(new model m)");
		_test("(set-model-cache-path m \"" + testFolder + "\")");
		_test("(list-stmt m from n1:'(?x ?y ?z))",
				"'(n1:'(\"2022-02-06\" \"KKK\" https://bj.ke.com/ershoufang/101113237601.html))");
		_test("(save-model m)", "2");

		try {

			assertEquals(
					";@: '(pre \"BK2##\" \"https://bj.ke.com/ershoufang/\")\n"
							+ "\"2022-02-06\" \"KKK\" BK2##101113237601.html",
					RulpUtil.toOneLine(FileUtil.openTxtFile(testFolder + "/n1.3.mc", "utf-8")));
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		FileUtil.deleteFile(new File(testFolder));
	}

	@Test
	void test_cache_model_8_pre() throws IOException {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/rule/TestCache/test_cache_model_8_pre\")");
		_test("(list-stmt m from n1:'(?x ?y ?z))",
				"'(n1:'(\"2022-02-06\" \"KKK\" https://bj.ke.com/ershoufang/101113237601.html))");

	}

	@Test
	void test_cache_node_1() {

		_setup();
		_test("(new model m)");

		try {

			IRModel model = _model("m");
			IRReteNode node = model.findNode((IRList) RuleUtil.toCondList("n1:'(?)"));
//			model.setNodeCache(node, (key) -> {
//				return RuleUtil.toStmtList("'('(a) '(b))");
//			}, null, null);

			model.setNodeLoader(node, new IRStmtLoader() {

				@Override
				public int getReadLines() {
					return 0;
				}

				@Override
				public void load(IRReteNode node, IRListener1<IRList> stmtListener) throws RException, IOException {
					IRIterator<? extends IRList> it = RuleUtil.toStmtList("'(n1:'(a) n1:'(b))");
					while (it.hasNext()) {
						stmtListener.doAction(it.next());
					}
				}
			});

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

//		_test("(update-stmt m ('(?n typeof node)))", "7");
		_test("(list-stmt m from n1:'(?))", "'(n1:'(a) n1:'(b))");

		_statsInfo("m");
	}

	@Test
	void test_cache_node_2() {

		_setup();

		new File("result/rule/TestCache/test_cache_node_2a/name1.3.mc").delete();
		new File("result/rule/TestCache/test_cache_node_2b/name2.3.mc").delete();

		_test("(new model m)");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(add-stmt m name2:'(x y z))");
		_test("(set-node-cache-path m name1:'(?...) \"result/rule/TestCache/test_cache_node_2a\")");
		_test("(set-node-cache-path m name2:'(?...) \"result/rule/TestCache/test_cache_node_2b\")");
		_test("(save-model m)", "2");

		_statsInfo("m");

	}

	@Test
	void test_cache_node_3_constrain_order_by() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_node_4_fix_attribute() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_node_5_remove_all() {

		_setup();
		_run_script();
		_statsInfo("m");
	}
}
