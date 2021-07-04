package beta.rulp.optimize;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.RuleTestBase;

public class OptimizeTest1 extends RuleTestBase {

	@Test
	void test_alpha_match_expr_1() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) '(?p p2 c) (> c 9) do (-> '(?a p3 ?b)))");
		_test("(add-rule m if '(?a p1 ?b) '(?a ?p ?b) '(?p p2 c) (> c 9) (< c 10) do (remove-stmt ?0))");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");

//		System.out.println(OptimizeUtil.printStatsInfo(_model("m")));
		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_bad_expr_1_bigger() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) '(?p p2 c) (> c 10) do (-> '(?a p3 ?b)))");
		_test("(add-rule m if '(?a p1 ?b) '(?a ?p ?b) '(?p p2 c) (> c 10) (< c 9) do (remove-stmt ?0))");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");

//		System.out.println(OptimizeUtil.printStatsInfo(_model("m")));
		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_bad_expr_2_equal_1() {

		_setup();
		_test_script("result/optimize/OptimizeTest1/test_bad_expr_2_equal_1.rulp");
	}

	@Test
	void test_beta_join_1() {

		_setup();
		_test("(new model m)");
		_test("(add-rule \"C7\" m if '(?p nm:propertyOf nm:nonCircleProperty) '(?a ?p ?b) '(?b ?p ?a) (not (equal ?a ?b)) do (error '(\"Circle found\" ?1 ?2)))");
		_test("(add-stmt m '(a1 p1 b1))");
		_test("(add-stmt m '(a2 p2 b2))");
		_test("(add-stmt m '(a3 p2 b3))");
		_test("(add-stmt m '(p1 nm:propertyOf nm:nonCircleProperty))");
		_test("(add-stmt m '(p2 nm:propertyOf nm:nonCircleProperty))");
		_test("(start m)", "8");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)",
				"'('(a1 p1 b1) '(a2 p2 b2) '(a3 p2 b3) '(p1 nm:propertyOf nm:nonCircleProperty) '(p2 nm:propertyOf nm:nonCircleProperty))");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_equal_1() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule \"T7\" m if '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (equal nm:hasAutoRelatedTag ?p) (equal nm:hasRelation ?p) do (-> '(?a nm:hasRelation ?b)))",
				"T7");
		_test("(add-rule \"RR5\" m if '(?a nm:hasAutoRelatedTag ?b) '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (equal nm:hasAutoRelatedTag ?p) (equal nm:hasRelation ?p) do (remove-stmt ?0))",
				"RR5");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_equal_1.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_equal_2() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) (equal p1 ?p) (equal p2 ?p) do (remove-stmt ?0))", "RU000");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_equal_2.txt");
	}

	@Test
	void test_equal_3_named() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if n1:'(?a ?p ?b) (equal p1 ?p) (equal p2 ?p) do (remove-stmt ?0))", "RU000");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_equal_3_named.txt");
	}

	@Test
	void test_not_equal_1_orignal() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule \"T7\" m if '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not (equal nm:hasAutoRelatedTag ?p)) (not (equal nm:hasRelation ?p)) do (-> '(?a nm:hasRelation ?b)))");
		_test("(add-rule \"RR5\" m if '(?a nm:hasAutoRelatedTag ?b) '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not (equal nm:hasAutoRelatedTag ?p)) (not (equal nm:hasRelation ?p)) do (remove-stmt ?0))");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_1_orignal.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();

		// System.out.println(OptimizeUtil.printStatsInfo(_model("m")));
	}

	@Test
	void test_not_equal_2() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule \"T7\" m if '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not-equal nm:hasAutoRelatedTag ?p) (not-equal nm:hasRelation ?p) do (-> '(?a nm:hasRelation ?b)))");
		_test("(add-rule \"RR5\" m if '(?a nm:hasAutoRelatedTag ?b) '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not-equal nm:hasAutoRelatedTag ?p) (not-equal nm:hasRelation ?p) do (remove-stmt ?0))");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_2.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();

	}

	@Test
	void test_not_equal_3() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule \"T7\" m if '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not-equal nm:hasAutoRelatedTag ?p) (not-equal nm:hasRelation ?p) do (-> '(?a nm:hasRelation ?b)))");
		_test("(add-rule \"RR5\" m if '(?a nm:hasAutoRelatedTag ?b) '(?a ?p ?b) '(?p nm:propertyOf nm:tagRelation) (not (equal nm:hasAutoRelatedTag ?p)) (not (equal nm:hasRelation ?p)) do (remove-stmt ?0))");
		_test("(start m)", "0");
		_test("(state-of m)", "completed");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_3.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_not_equal_4() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) '(?p of tr) (not (equal p1 ?p)) (not (equal p2 ?p)) do (-> '(?a p4 ?b)))");
		_test("(add-rule m if '(?a2 pauto ?b2) '(?a2 ?p2 ?b2) '(?p2 of tr) (not (equal p1 ?p2)) (not (equal p2 ?p2)) do (-> '(?a2 p5 ?b2)))");
		_test("(add-stmt m '(a p1 b))");
		_test("(add-stmt m '(a pauto b))");
		_test("(add-stmt m '(px of tr))");
		_test("(start m)", "8");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a p1 b) '(a pauto b) '(px of tr))");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_4.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_not_equal_5() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) (not (equal p1 ?p)) do (-> '(?a p2 ?b)))");
		_test("(add-rule m if '(?a2 ?p2 ?b2) '(?a2 p2 ?b2) (not (equal p1 ?p2)) do (-> '(?a2 p4 ?b2)))");
		_test("(add-stmt m '(a3 p3 b3))");
		_test("(start m)", "20");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a3 p3 b3) '(a3 p2 b3) '(a3 p4 b3))");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_5.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

	@Test
	void test_not_equal_6() {

		_setup();
		_test("(new model m)", "m");
		_test("(add-rule m if '(?a ?p ?b) (not (equal ?a ?b)) do (-> '(?b p2 ?a)))");
		_test("(add-rule m if '(?a2 ?p2 ?b2) '(?b2 ?p2 ?a2) (not (equal ?a2 ?b2)) do (-> '(?a2 p4 ?b2)))");
		_test("(add-stmt m '(a2 p2 b2))");
		_test("(start m)", "20");
		_test("(state-of m)", "completed");
		_test("(list-stmt m)", "'('(a2 p2 b2) '(b2 p2 a2) '(a2 p4 b2) '(b2 p4 a2))");
		_statsInfo("m", "result/optimize/OptimizeTest1/test_not_equal_6.txt");

		_mStatus(1, "m");
		_oStatus(1, "m");
		_saveTest();
	}

}
