package beta.rulp.manners;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class MannerModelTest extends RuleTestBase {

	@Test
	void test_can_sit_forward_1() {

		_setup();

		_test_script("benchmark/beta/rulp/manners/manner_can_sit_forward_1.rulp");

		_mStatus(1, "mm");
		_oStatus(1, "mm");
		_saveTest();
		_statsInfo("mm", "benchmark/beta/rulp/manners/manner_can_sit_forward_1.txt");
	}

	@Test
	void test_can_sit_backward_2() {

		_setup();

		_test_script("benchmark/beta/rulp/manners/manner_can_sit_backward_2.rulp");
		_test("(canSitTogether guess-1 guess-2)");

		_mStatus(1, "mm");
		_oStatus(1, "mm");
		_saveTest();
		_statsInfo("mm", "benchmark/beta/rulp/manners/manner_can_sit_backward_2.txt");
	}
}
