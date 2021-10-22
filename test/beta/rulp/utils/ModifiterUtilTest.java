package beta.rulp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.utils.ModifiterUtil;
import alpha.rulp.utils.ModifiterUtil.Modifier;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpTestBase;

class ModifiterUtilTest extends RulpTestBase {

	void _test_modifiter_list(String inputList, String expectResult) {

		try {

			List<IRObject> list = RulpFactory.createParser().parse(inputList);
			assertEquals(1, list.size());
			assertEquals(RType.LIST, list.get(0).getType());

			IRList modifiterList = (IRList) list.get(0);

			List<Modifier> ml = ModifiterUtil.parseModifiterList(modifiterList.iterator(),
					_getInterpreter().getMainFrame());

//			String asString = modifiterData.asString();

			assertEquals(expectResult, ml.toString());

		} catch (RException | IOException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

//	void _test_modifiter_list(String inputList) {
//		_test_modifiter_list(inputList, inputList);
//	}

	@Test
	void test_1() {
		_test_modifiter_list("'(type atom)", "[(type atom)]");
		_test_modifiter_list("'(from '(a b))", "[(from '('(a b)))]");
		_test_modifiter_list("'(limit 1)", "[(limit 1)]");
		_test_modifiter_list("'(state defined)", "[(state 1)]");
		_test_modifiter_list("'(do (a b))", "[(do '((a b)))]");
	}

	@Test
	void test_2() {
		_test_modifiter_list("'(limit 1 type atom state defined)", "[(limit 1), (type atom), (state 1)]");
	}
}
