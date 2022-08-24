package beta.test.manners;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class MannerModelTest extends RuleTestBase {

	@Test
	void test_can_sit_forward_1() {

		_setup();
		_run_script();
		_statsInfo("mm");
	}

	@Test
	void test_can_sit_backward_2() {

		// Should fail

		_setup();

		_run_script();
		_test("(canSitTogether guess-1 guess-2)");
		_statsInfo("mm");
	}
}
