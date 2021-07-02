package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ReasonTest extends RuleTestBase {

	@Test
	public void test_1() {

		_setup();
//		TraceUtil.setTrace(true);
		_test_script("result/rule/ReasonTest/test_1.rulp");
	}
}
