package beta.rulp.rule;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ReasonTest extends RuleTestBase {

	@Test
	public void test_1() {

		_setup();
		_test_script("result/rule/ReasonTest/test_1.rulp");
	}

	@Test
	public void test_2_auto_delete() {

		_setup();
		_test_script("result/rule/ReasonTest/test_2_auto_delete.rulp");
		_statsInfo("m", "result/rule/ReasonTest/test_2_auto_delete.txt");
		_dumpEntryTable("m", "result/rule/ReasonTest/test_2_auto_delete.dump.txt");
	}
}
