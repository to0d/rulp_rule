package beta.test.utils;

import static org.junit.Assert.assertEquals;

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

	String _modifiter_list(String inputList) throws RException, IOException {

		List<IRObject> list = RulpFactory.createParser().parse(inputList);
		assertEquals(1, list.size());
		assertEquals(RType.LIST, list.get(0).getType());

		IRList modifiterList = (IRList) list.get(0);

		List<Modifier> ml = ModifiterUtil.parseModifiterList(modifiterList.iterator(),
				_getInterpreter().getMainFrame());

		return ml.toString();

	}

	@Test
	void test_parse_modifiter_list_1() {

		_setup();

		_test((input) -> {
			return _modifiter_list(input);
		});

	}

	@Test
	void test_parse_modifiter_list_2() {

		_setup();

		_test((input) -> {
			return _modifiter_list(input);
		});

	}
}
