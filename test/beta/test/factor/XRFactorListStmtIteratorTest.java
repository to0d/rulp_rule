package beta.test.factor;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorListStmtIteratorTest extends RuleTestBase {

	@Test
	void test_list_stmt_iterator_1() {

		_setup();
		_run_script();
		_statsInfo("m");

	}

}
