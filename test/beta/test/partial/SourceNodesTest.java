package beta.test.partial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;

class SourceNodesTest extends RuleTestBase {

	protected void _expectSourceNames(String inputExpr, String expectNames) {

		try {

			List<IRObject> objs = this._getParser().parse(inputExpr);
			assertEquals(objs.size(), 1);

			IRExpr expr = RulpUtil.asExpression(objs.get(0));

			List<String> uniqNames = new ArrayList<>();

			for (IRReteNode node : RuleUtil.listSourceNodes(_model("m"), expr)) {
				uniqNames.add(node.getUniqName());
			}

			Collections.sort(uniqNames);

			assertEquals(expectNames, "" + uniqNames);

		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_listSourceNodes_1() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x typeof c1) do (-> m '(?x typeof c2)) )");
		_test("(add-rule \"R2\" m if '(?x p ?y) '(?y p ?z) do (-> m '(?x p ?z)) )");

		_expectSourceNames("('(?x typeof c1))", "[]");
		_expectSourceNames("('(?x typeof c2))", "[R1]");
		_expectSourceNames("('(?x typeof ?y))", "[R1]");
		_expectSourceNames("('(?a ?b ?c))", "[R1, R2]");
	}

	@Test
	void test_listSourceNodes_2() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x ?y ?z) do (-> '(?z ?y ?x)) )");

		_expectSourceNames("('(?a ?b ?c))", "[R1]");

	}

	@Test
	void test_listSourceNodes_3() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if '(?x ?y ?z) do (-> '(?z ?y ?x 1)) )");

		_expectSourceNames("('(?a ?b ?c ?d))", "[R1]");
		_expectSourceNames("('(?a ?b ?c))", "[]");
	}

	@Test
	void test_listSourceNodes_4() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if name1:'(?x ?y ?z) do (-> name1:'(?z ?y ?x)) )");

		_expectSourceNames("('(?a ?b ?c))", "[]");
		_expectSourceNames("(name1:'(?a ?b ?c))", "[R1]");
	}

	@Test
	void test_listSourceNodes_5() {

		_setup();

		_test("(new model m)", "m");
		_test("(add-rule \"R1\" m if name1:'(?x p ?z) do (-> name1:'(?z p ?x)) )");

		_expectSourceNames("('(?a p ?c))", "[]");
		_expectSourceNames("(name1:'(?a p ?c))", "[R1]");
	}
}