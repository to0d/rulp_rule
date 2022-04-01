package beta.test.eton;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class TestBeike extends RuleTestBase {

	@Test
	void test_beike_1_model_cache() {

		_setup();
		_run_script();
//		_statsInfo("em");
	}

	@Test
	void test_beike_2_copy_stmt() {

		_setup();
		_run_script();
//		_statsInfo("m");
	}

	@Test
	void test_beike_3() {

		_setup();
		_run_script();
	}
}
