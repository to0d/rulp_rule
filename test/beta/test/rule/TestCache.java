package beta.test.rule;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;
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
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_model_1a() {

		_setup();
		_clean_model_cache();
		_run_script();
		_save_model_cache("m");
		_statsInfo("m");
	}

	@Test
	void test_cache_model_1b() {

		_setup();
		_clean_model_cache();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_model_2() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_model_3() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_model_4a() {

		_setup();
		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();
		_run_script();
		_statsInfo("m");
		_dumpEntryTable("m");
		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();
	}

	@Test
	void test_cache_model_4b() {

		_setup();
		_run_script();
		_statsInfo("m");
		new File("result/rule/TestCache/test_cache_model_4/name3.3.mc").delete();
	}

	@Test
	void test_cache_model_5() {

		_setup();
		new File("result/cache/CacheTest/test_cache_model_5/.3.mc").delete();
		_run_script();
		_statsInfo("m");
	}

	@Test
	void test_cache_model_6_list() {

		_setup();
		_clean_model_cache();
		_run_script();
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

		_run_script();

		FileUtil.deleteFile(new File(testFolder));
	}

	@Test
	void test_cache_model_8_pre() throws IOException {

		_setup();
		_run_script();
	}

	@Test
	void test_cache_node_1() {

		_setup();
		_test("(new model m)");

		try {

			IRModel model = _model("m");
			IRReteNode node = model.findNode((IRList) RuleUtil.toCondList("n1:'(?)"));

			model.setNodeLoader(node, new IRStmtLoader() {

				@Override
				public int getReadLines() {
					return 0;
				}

				@Override
				public void load(IRListener1<IRList> stmtListener) throws RException, IOException {
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

		_test("(list-stmt m from n1:'(?))", "'(n1:'(a) n1:'(b))");

		_statsInfo("m");
	}

	@Test
	void test_cache_node_2() {

		_setup();

		new File("result/rule/TestCache/test_cache_node_2a/name1.3.mc").delete();
		new File("result/rule/TestCache/test_cache_node_2b/name2.3.mc").delete();

		_run_script();
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
