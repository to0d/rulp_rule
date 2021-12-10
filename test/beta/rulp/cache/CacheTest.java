package beta.rulp.cache;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;

public class CacheTest extends RuleTestBase {

	@Test
	void test_1_model_cache_1() {

		_setup();

		new File("result/cache/CacheTest/cache_1/.3.mc").delete();
		new File("result/cache/CacheTest/cache_1/name1.3.mc").delete();

		_test("(new model m)");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_1\")");
		_test("(save-model m)", "2");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_1.txt");
	}

	@Test
	void test_1_model_cache_1b() {

		_setup();

		new File("result/cache/CacheTest/cache_1/.3.mc").delete();
		new File("result/cache/CacheTest/cache_1/name1.3.mc").delete();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_1\")");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(save-model m)", "2");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_1b.txt");
	}

	@Test
	void test_1_model_cache_2() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_2\")");
		_test("(save-model m)", "0");
		_test("(list-stmt m)", "'('(a b c) name2:'(x y z))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_2.txt");
	}

	@Test
	void test_1_model_cache_3() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_3\")");
		_test("(save-model m)", "0");
		_test("(list-stmt m from name2:'(?x ?y ?z))", "'(name2:'(x y z))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_3.txt");
	}

	@Test
	void test_1_model_cache_4a() {

		_setup();

		// XRModel.TRACE_RETE = true;
		new File("result/cache/CacheTest/cache_4/name3.3.mc").delete();

		_test("(new model m)");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name3:'(?x ?y ?z)) )");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_4\")");
		_test("(start m)");
		_test("(list-stmt m from name3:'(?x ?y ?z))", "'(name3:'(a b c))");
		_test("(save-model m)", "1");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_4a.txt");
		_dumpEntryTable("m", "result/cache/CacheTest/test_1_model_cache_4a.dump.txt");

		new File("result/cache/CacheTest/cache_4/name3.3.mc").delete();
	}

	@Test
	void test_1_model_cache_4b() {

		_setup();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_4\")");
		_test("(add-rule m if name1:'(?x ?y ?z) do (-> name3:'(?x ?y ?z)) )");
		_test("(query-stmt m '(?x ?y ?z) from name3:'(?x ?y ?z))", "'('(a b c))");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_test("(save-model m)", "1");
		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_4b.txt");

		new File("result/cache/CacheTest/cache_4/name3.3.mc").delete();
	}

	@Test
	void test_1_model_cache_5() {

		_setup();

		new File("result/cache/CacheTest/cache_5/.3.mc").delete();

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_5\")");
		_test("(add-stmt m '(x y z))");
		_test("(save-model m)", "1");
		_test("(delete m)");

		_test("(new model m)");
		_test("(set-model-cache-path m \"result/cache/CacheTest/cache_5\")");
		_test("(remove-stmt m '(x y z))");
		_test("(save-model m)", "0");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_1_model_cache_5.txt");
	}

	@Test
	void test_2_node_cache_1() {

		_setup();
		_test("(new model m)");

		try {

			IRModel model = _model("m");
			IRReteNode node = model.findNode((IRList) RuleUtil.toCondList("n1:'(?)"));
//			model.setNodeCache(node, (key) -> {
//				return RuleUtil.toStmtList("'('(a) '(b))");
//			}, null, null);

			model.setNodeLoader(node, (stmtListener) -> {
				IRIterator<? extends IRList> it = RuleUtil.toStmtList("'('(a) '(b))");
				while (it.hasNext()) {
					stmtListener.doAction(it.next());
				}
			});

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

//		_test("(update-stmt m ('(?n typeof node)))", "7");
		_test("(list-stmt m from n1:'(?))", "'(n1:'(a) n1:'(b))");

		_mCount(1, "m");
		_eCount(1, "m");
		_mStatus(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_2_node_cache_1.txt");
	}

	@Test
	void test_2_node_cache_2() {

		_setup();

		new File("result/cache/CacheTest/cache_n_1/name1.3.mc").delete();
		new File("result/cache/CacheTest/cache_n_2/name2.3.mc").delete();

		_test("(new model m)");
		_test("(add-stmt m '(x y z))");
		_test("(add-stmt m name1:'(a b c))");
		_test("(add-stmt m name2:'(x y z))");
		_test("(set-node-cache-path m name1:'(?...) \"result/cache/CacheTest/cache_n_1\")");
		_test("(set-node-cache-path m name2:'(?...) \"result/cache/CacheTest/cache_n_2\")");
		_test("(save-model m)", "2");

		_mStatus(1, "m");
		_mCount(1, "m");
		_eCount(1, "m");
		_saveTest();

		_statsInfo("m", "result/cache/CacheTest/test_2_node_cache_2.txt");

	}
}
