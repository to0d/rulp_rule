package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestConstraint extends RuleTestBase {

	@Test
	void test_constraint_1() {

		_setup();
		_run_script();
		_statsInfo("m");
	}

}
