package beta.rulp.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorProveStmtTest extends RuleTestBase {

	@Test
	public void test_1() {

		_setup();
		_test_script("result/factor/XRFactorProveStmtTest/test_1.rulp");
	}

}
