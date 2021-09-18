package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestConstraintRule extends RuleTestBase {
	@Test
	public void test_type_match_1() {

		_setup();
		// _enableTrace();
		_test_script("result/constraint/TestConstraintRule/test_type_match_1.rulp");
	}
}
