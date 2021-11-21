package beta.rulp.manners;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class MannerModelTest extends RuleTestBase {

	@Test
	void test_can_sit_forward_1() {

		_setup();

		_test_script();

		_mStatus(1, "mm");
		_oStatus(1, "mm");
		_saveTest();
		_statsInfo("mm");
	}

	@Test
	void test_can_sit_backward_2() {

		_setup();

		_test_script();
		_test("(canSitTogether guess-1 guess-2)");

		_mStatus(1, "mm");
		_oStatus(1, "mm");
		_saveTest();
		_statsInfo("mm");
	}
}
