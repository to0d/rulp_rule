package beta.test.worker;

import static org.junit.Assert.fail;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;

public class WorkerTest extends RuleTestBase {

	@Test
	void test_load_1_model_interface() {

		_setup();
		_test("(new model m)");

		try {

			_model("m").getNodeGraph().bindNode(_model("m").getNodeGraph().createWorkNode(null, (model) -> {
				RuleUtil.addStatements(model, RuleUtil.toStmtList("'('(a typeof node) '(b typeof node))"));
				return true;
			}), _model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(a b)");
		_statsInfo("m");
	}

	@Test
	void test_load_2_always_has_data_no_duplicate() {

		_setup();
		_test("(new model m)");

		try {

			_model("m").getNodeGraph().bindNode(_model("m").getNodeGraph().createWorkNode(null, new IRWorker() {

				int index = 0;

				@Override
				public boolean work(IRModel model) throws RException {
					RuleUtil.addStatements(model, RuleUtil.toStmtList(String.format("'('(n%d typeof node))", index++)));
					return false;
				}

			}), _model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0)");
		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0 n1)");
		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0 n1 n2)");

		_statsInfo("m");
	}

	@Test
	void test_load_3_always_has_data_has_duplicate() {

		_setup();
		_test("(new model m)");

		try {

			_model("m").getNodeGraph().bindNode(_model("m").getNodeGraph().createWorkNode(null, (model) -> {
				RuleUtil.addStatements(model, RuleUtil.toStmtList(String.format("'('(n%d typeof node))", 0)));
				return false;
			}), _model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0)");
		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0)");
		_statsInfo("m");
	}

	@Test
	void test_load_4_data_not_match_filter() {

		_setup();
		_test("(new model m)");

		try {

			RuleUtil.addWorker(_model("m"), RuleUtil.toCondList("'(?n typeof node)"), (model) -> {
				RuleUtil.addStatements(model, RuleUtil.toStmtList("'('(a typeof node) '(b has c))"));
				return true;
			});

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(a)");
		_statsInfo("m");
	}

	@Test
	void test_load_5_multi_bind_2() {

		_setup();
		_test("(new model m)");

		try {

			IRReteNode worker = _model("m").getNodeGraph().createWorkNode(null, new IRWorker() {
				int index = 0;

				@Override
				public boolean work(IRModel model) throws RException {
					RuleUtil.addStatements(model, RuleUtil.toStmtList(String.format("'('(n%d typeof node))", index++)));
					return false;
				}
			});

			_model("m").getNodeGraph().bindNode(worker,
					_model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));
			_model("m").getNodeGraph().bindNode(worker,
					_model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

			// _statsInfo("m", "test/beta/rulp/partial/test_load_5_multi_bind_2.txt");

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(n0)");
		_statsInfo("m");
	}

	@Test
	void test_load_5_multi_bind_3() {

		_setup();
		_test("(new model m)");

		try {

			_model("m").getNodeGraph().bindNode(_model("m").getNodeGraph().createWorkNode(null, new IRWorker() {
				int index = 0;

				@Override
				public boolean work(IRModel model) throws RException {
					RuleUtil.addStatements(model, RuleUtil.toStmtList(String.format("'('(x%d typeof node))", index++)));
					return false;
				}
			}), _model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

			_model("m").getNodeGraph().bindNode(_model("m").getNodeGraph().createWorkNode(null, new IRWorker() {
				int index = 0;

				@Override
				public boolean work(IRModel model) throws RException {
					RuleUtil.addStatements(model, RuleUtil.toStmtList(String.format("'('(y%d typeof node))", index++)));
					return false;
				}
			}), _model("m").findNode((IRList) RuleUtil.toCondList("'(?n typeof node)")));

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		_test("(query-stmt m ?n from '(?n typeof node))", "'(x0 y0)");
		_statsInfo("m");
	}
}
