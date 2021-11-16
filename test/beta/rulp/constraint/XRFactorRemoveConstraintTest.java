package beta.rulp.constraint;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

class XRFactorRemoveConstraintTest extends RuleTestBase {

	@Test
	void test_any_1() {

		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ? ?) '(uniq on '(?x)))", "1");
		_test("(add-constraint m name1:'(?x ? ?) '(type int on ?x))", "1");
		_test("(remove-constraint m name1:'(3) '(? on '(?0)))", "'('(type int on ?0) '(uniq on ?0))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_any_2() {

		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ?y ?) '(uniq on '(?y)))", "1");
		_test("(add-constraint m name1:'(?x ? ?) '(type int on ?x))", "1");
		_test("(remove-constraint m name1:'(3) '(? on ?))", "'('(type int on ?0) '(uniq on ?1))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_type_1() {

		_setup();
		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ? ?) '(type int on ?x))", "1");
		_test("(remove-constraint m name1:'(?x ?...) '(type ? on ?x))", "'('(type int on ?0))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_type_2() {

		_setup();
		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ? ?) '(type int on ?x))", "1");
		_test("(add-constraint m name1:'(? ?y ?) '(type int on ?y))", "1");
		_test("(remove-constraint m name1:'(3) '(type ? on ?))", "'('(type int on ?0) '(type int on ?1))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_type_3() {

		_setup();
		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(3) '(type int on ?2))", "1");
		_test("(remove-constraint m name1:'(3) '(type ? on ?))", "'('(type int on ?2))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_type_4() {

		_setup();
		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ? ?) '(type int on ?x))", "1");
		_test("(remove-constraint m name1:'(3) '(type ? on 0))", "'('(type int on ?0))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_uniq_1() {

		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ?y ?) '(uniq on '(?x ?y)))", "1");
		_test("(remove-constraint m name1:'(?x ?y ?) '(uniq on '(?x ?y)))", "'('(uniq on '(?0 ?1)))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_uniq_2() {

		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(3) '(uniq on '(?0 ?1)))", "1");
		_test("(remove-constraint m name1:'(?x ?y ?) '(uniq on '(?x ?y)))", "'('(uniq on '(?0 ?1)))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_uniq_3() {
		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ?y ?) '(uniq on '(?y ?x)))", "1");
		_test("(remove-constraint m name1:'(3) '(uniq on '(?0 ?1)))", "'('(uniq on '(?0 ?1)))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

	@Test
	void test_uniq_4() {

		_setup();

		_test("(new model m)");
		_test("(add-node m name1:'(3))");
		_test("(add-constraint m name1:'(?x ?y ?) '(uniq on '(?x ?y)))", "1");
		_test("(add-constraint m name1:'(3) '(uniq on '(?2 ?1)))", "1");
		_test("(remove-constraint m name1:'(3) '(uniq on '(? ?1)))", "'('(uniq on '(?0 ?1)) '(uniq on '(?1 ?2)))");
		_test("(list-constraint m name1:'(?...))", "'()");
		_test("(list-stmt m from $cst_node$:'(?...))", "'()");
	}

}
