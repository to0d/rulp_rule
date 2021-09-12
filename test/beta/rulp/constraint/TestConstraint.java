package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestConstraint extends RuleTestBase {
	@Test
	public void test_constraint_1() {

		_setup();
		// _enableTrace();
		_test_script("result/constraint/TestConstraint/test_constraint_1.rulp");
		_statsInfo("m", "result/constraint/TestConstraint/test_constraint_1.txt");
	}
}
