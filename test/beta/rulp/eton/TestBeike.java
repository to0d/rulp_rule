package beta.rulp.eton;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestBeike extends RuleTestBase {

	@Test
	void test_beike_1_model_cache() {

		_setup();
		_run_script();
		_statsInfo("em");
	}

}