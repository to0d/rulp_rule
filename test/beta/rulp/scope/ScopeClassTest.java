package beta.rulp.scope;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class ScopeClassTest extends RuleTestBase {

	@Test
	void test_0_new_scope_1() {

		_setup();

		_test("(new scope s)", "s");
		_test("(type-of s)", "instance");
		_test("(class-of s)", "scope");

		_test("(ls-print scope)", "nil", _load("test/beta/rulp/scope/ls_scope_class.txt") + "\n");
		_test("(ls-print s)", "nil", _load("test/beta/rulp/scope/ls_scope_instance.txt") + "\n");

		_test("(s::get-model)", "s");
		_test("(delete s)", "s");
		_test("(type-of s)", "atom");

	}

	@Test
	void test_0_new_scope_2() {

		_setup();
		_test("(new model m)", "m");
		_test("(new scope s '(m))", "s");
		_test("(s::get-model)", "s");
		_test("(delete s)", "s");
		_test("(type-of s)", "atom");

	}
}
