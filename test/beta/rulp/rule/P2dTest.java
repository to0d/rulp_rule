package beta.rulp.rule;

import org.junit.Test;

import alpha.rulp.utils.RuleTestBase;

public class P2dTest extends RuleTestBase {

	@Test
	public void test_domain_beParentDomainOf_Global() {

		_setup();
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(domain1 nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(domain2 nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(domainx nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(domain1 nm:beParentDomainOf domain2))");

		_test("(start p2d)", "427");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");
		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");

		_test("(list-stmt p2d from '(?x nm:beParentDomainOf ?y))",
				"'('(domain1 nm:beParentDomainOf domain2) '(nd:Global nm:beParentDomainOf domain1) '(nd:Global nm:beParentDomainOf domainx))");
		_mStatus(2, "p2d");
		_mCount(2, "p2d");
		_eCount(2, "p2d");

		_saveTest();
	}

	@Test
	public void test_fun_get_field_name() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(get-field-name abc)", "\"abc\"");
		_test("(get-field-name nt:abc)", "\"nt:abc\"");
		_test("(get-field-name mn:abc)", "\"mn:abc\"");
		_test("(get-field-name nt:abc-xyz)", "\"xyz\"");
		_test("(get-field-name nt:abc-xyz-123)", "\"123\"");

		_test("(defvar ?c ta3)(defvar ?f f3)");
		_test("(strcat (to-string ?c) \"-\" (get-field-name (get-uri-name ?f)))", "\"ta3-f3\"");
	}

	@Test
	public void test_fun_get_uri_name() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(get-uri-name abc)", "\"abc\"");
		_test("(get-uri-name nt:abc)", "\"abc\"");
		_test("(get-uri-name mn:abc)", "\"abc\"");
	}

	@Test
	public void test_query_1() {

		_setup();
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:XXXX nt:t1))");
		_test("(add-stmt p2d '(nm:XXXX nm:propertyOf nm:tagRelation))");
		_test("(add-stmt p2d '(nt:t2 nm:defineTag nt:t1))");
		_test("(add-stmt p2d '(nd:Global nm:hasTag nt:t1))");
		_test("(add-stmt p2d '(nd:Global nm:hasTag nt:t2))");
		_test("(query-stmt p2d '(nt:ProgrammingLanguage ?t2 ?t1 ?p1 nm:defineTag) from '(nt:ProgrammingLanguage ?p1 ?t1) '(?p1 nm:propertyOf nm:tagRelation) (not (equal ?p1 nm:hasFieldTag)) (not (equal ?p1 nm:beClassTagOf)) (not (equal ?p1 nm:beParentTagOf)) '(?t2 nm:defineTag ?t1) '(nd:Global nm:hasTag ?t1) '(nd:Global nm:hasTag ?t2))",
				"'('(nt:ProgrammingLanguage nt:t2 nt:t1 nm:XXXX nm:defineTag))");
		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_alias_1_full() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tc))");

		_test("(start p2d)", "302");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasAliasTag ?y))", "'('(ta nm:hasAliasTag tb) '(ta nm:hasAliasTag tc))");
		_test("(list-stmt p2d from '(?x nm:beAliasTo ?y))", "'('(tb nm:beAliasTo ta) '(tc nm:beAliasTo ta))");
		_test("(list-stmt p2d from '(?x nm:typeOf nm:tag))",
				"'('(ta nm:typeOf nm:tag) '(tb nm:typeOf nm:tag) '(tc nm:typeOf nm:tag))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();

		_statsInfo("p2d", "result/rule/P2dModelTest/p2d_alias_1_full.txt");
		_dumpEntryTable("p2d", "result/rule/P2dModelTest/p2d_alias_1.entry.dump.txt");

	}

	@Test
	public void test_tag_alias_1_partial() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tb))");
		_test("(add-stmt p2d '(ta nm:hasAliasTag tc))");

		_test("(query-stmt p2d '(?x ?y) from '(?x nm:hasAliasTag ?y))", "'('(ta tb) '(ta tc))");
		_test("(query-stmt p2d '(?x ?y) from '(?x nm:beAliasTo ?y))", "'('(tb ta) '(tc ta))");
		_test("(query-stmt p2d '(?x) from '(?x nm:typeOf nm:tag))", "'('(ta) '(tb) '(tc))");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
		// System.out.println(OptimizeUtil.printStatsInfo(_model("p2d")));
	}

	@Test
	public void test_tag_alias_2_full() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:beAliasTo tb))");
		_test("(add-stmt p2d '(ta nm:beAliasTo tc))");

		_test("(start p2d)", "303");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'(C4)");

	}

	@Test
	public void test_tag_alias_3_full() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:beAliasTo tb))");
		_test("(add-stmt p2d '(tb nm:beAliasTo tc))");

		_test("(start p2d)", "303");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'(T1)");

	}

	@Test
	public void test_tag_alias_4_full() {

		_setup();
		// XRModel.TRACE_RETE = true;

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:beParentTagOf tb))");
		_test("(add-stmt p2d '(ta nm:hasRelatedTag tc))");
		_test("(add-stmt p2d '(tax nm:beAliasTo ta))");
		_test("(add-stmt p2d '(tax nm:beParentTagOf td))");
		_test("(add-stmt p2d '(tax nm:hasRelatedTag te))");

		_test("(start p2d)", "447");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:typeOf nm:tag))",
				"'('(ta nm:typeOf nm:tag) '(nt:$TGROUP nm:typeOf nm:tag) '(tax nm:typeOf nm:tag) '(tc nm:typeOf nm:tag) '(te nm:typeOf nm:tag) '(td nm:typeOf nm:tag) '(tb nm:typeOf nm:tag))");

		_test("(list-stmt p2d from '(ta nm:beParentTagOf ?x))",
				"'('(ta nm:beParentTagOf tb) '(ta nm:beParentTagOf td))");
		_test("(list-stmt p2d from '(td nm:beChildTagOf ?x))", "'('(td nm:beChildTagOf tax) '(td nm:beChildTagOf ta))");
		_test("(list-stmt p2d from '(ta nm:hasRelatedTag ?x))",
				"'('(ta nm:hasRelatedTag tc) '(ta nm:hasRelatedTag te) '(ta nm:hasRelatedTag nt:$TGROUP))");
		_test("(list-stmt p2d from '(tc nm:hasRelatedTag ?x))", "'('(tc nm:hasRelatedTag ta))");
		_test("(list-stmt p2d from '(te nm:hasRelatedTag ?x))",
				"'('(te nm:hasRelatedTag tax) '(te nm:hasRelatedTag ta))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_beginner() {

		_setup();

		// XRModel.TRACE_RETE = false;
		_test("(load \"result/p2d.rulp\")");

		_test("(add-stmt p2d '(nt:ta nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:ta-Beginner nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:tb-Beginner nm:typeOf nm:tag))");

		_test("(start p2d)", "560");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:beChildTagOf nt:$BEGINNER))",
				"'('(nt:ta-Beginner nm:beChildTagOf nt:$BEGINNER) '(nt:tb-Beginner nm:beChildTagOf nt:$BEGINNER))");

		_test("(list-stmt p2d from '(?x nm:beDefinedByTag ?y))",
				"'('(nt:ta-Beginner nm:beDefinedByTag nt:$TOPIC) '(nt:ta-Beginner nm:beDefinedByTag nt:ta) '(nt:tb-Beginner nm:beDefinedByTag nt:$TOPIC))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_code_tag() {

		_setup();

		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:beClassTagOf nt:SQL))");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:hasFieldTag nt:Code))");
		_test("(add-stmt p2d '(nt:SQL-Code nm:typeOf nm:tag))");

		// _test("(trace-rule (get-rule p2d \"AC4\"))");

		_test("(start p2d)", "735");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");
		_test("(list-stmt p2d from '(nt:Code nm:beParentTagOf ?x))", "'('(nt:Code nm:beParentTagOf nt:SQL-Code))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_code_tag_2() {

		_setup();
//		XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:beClassTagOf nt:SQL))");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:hasFieldTag nt:Code))");
		_test("(add-stmt p2d '(nt:SQL-Code nm:typeOf nm:tag))");

		_test("(add-stmt p2d '(nd:XXX nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:Code))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:ProgrammingLanguage))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL-Code))");

		_test("(start p2d)", "743");
		_test("(state-of p2d)", "completed");

		_test("(uniq (query-stmt p2d '(?t1 ?t2 ?t3) from '(nt:ProgrammingLanguage nm:beClassTagOf ?t1) '(nt:ProgrammingLanguage nm:hasFieldTag ?t2) '(?t1 ?p1 ?t3) '(?p1 nm:propertyOf nm:tagRelation) '(?t2 ?p2 ?t3) '(?p2 nm:propertyOf nm:tagRelation) '(nd:XXX nm:hasTag ?t3) (not (equal ?t3 nt:ProgrammingLanguage))))",
				"'('(nt:SQL nt:Code nt:SQL-Code))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_code_tag_3() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:beClassTagOf nt:SQL))");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:hasFieldTag nt:Code))");
		_test("(add-stmt p2d '(nt:SQL-Code nm:typeOf nm:tag))");

		_test("(add-stmt p2d '(nd:XXX nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:Code))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:ProgrammingLanguage))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL-Code))");

		_test("(start p2d)", "743");
		_test("(state-of p2d)", "completed");

		_test("(uniq (query-stmt p2d '(?t1 ?t2 ?t3) from '(nt:ProgrammingLanguage nm:beClassTagOf ?t1) '(nt:ProgrammingLanguage nm:hasFieldTag ?t2) '(?t1 ?p1 ?t3) '(?p1 nm:propertyOf nm:tagRelation) '(?t2 ?p2 ?t3) '(?p2 nm:propertyOf nm:tagRelation) '(nd:XXX nm:hasTag ?t3) (not (equal ?t3 nt:ProgrammingLanguage)) (not (equal ?t3 nt:$TGROUP))))",
				"'('(nt:SQL nt:Code nt:SQL-Code))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_code_tag_4() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:beClassTagOf nt:SQL))");
		_test("(add-stmt p2d '(nt:ProgrammingLanguage nm:hasFieldTag nt:Code))");
		_test("(add-stmt p2d '(nt:SQL-Code nm:typeOf nm:tag))");

		_test("(add-stmt p2d '(nd:XXX nm:typeOf nm:domain))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:Code))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:ProgrammingLanguage))");
		_test("(add-stmt p2d '(nd:XXX nm:hasTag nt:SQL-Code))");

		_test("(start p2d)", "743");
		_test("(state-of p2d)", "completed");

		_test("(uniq (query-stmt p2d '(?t1 ?t2 ?t3 ?p1 ?p2) from '(?tag nm:beClassTagOf ?t1) '(?tag nm:hasFieldTag ?t2) '(?t1 ?p1 ?t3) '(?p1 nm:propertyOf nm:tagRelation) '(?t2 ?p2 ?t3) '(?p2 nm:propertyOf nm:tagRelation) '(?domain nm:hasTag ?t3) (not (equal ?t3 ?tag)) (not (equal ?t3 nt:$TGROUP)) (not (equal ?p2 nm:hasDescendant))))",
				"'('(nt:SQL nt:Code nt:SQL-Code nm:hasFieldTag nm:beParentTagOf) '(nt:SQL nt:Code nt:SQL-Code nm:hasFieldTag nm:hasGroupChild))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_domain_common_tag() {

		_test("(load \"result/p2d.rulp\")");

		_test("(add-stmt p2d '(nt:AS400-Concept nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:Concept nm:hasTagAttr nm:commonTag))");
		_test("(add-stmt p2d '(nd:AS400 nm:typeOf nm:domain))");

		_test("(start p2d)", "409");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(nt:Concept nm:hasRelatedTag ?x))",
				"'('(nt:Concept nm:hasRelatedTag nt:AS400-Concept))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();

	}

	@Test
	public void test_tag_auto_group_tag() {

		_test("(load \"result/p2d.rulp\")");

		_test("(add-stmt p2d '(nt:List-OJ nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:OJ nm:hasTagAttr nm:groupTag))");

		_test("(start p2d)", "494");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(nt:OJ nm:hasGroupChild ?x))", "'('(nt:OJ nm:hasGroupChild nt:List-OJ))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_auto_item_tag() {

		_test("(load \"result/p2d.rulp\")");

		_test("(add-stmt p2d '(nt:RFE-88111 nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:RFE nm:hasTagAttr nm:itemTag))");

		_test("(start p2d)", "553");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(nt:RFE nm:beParentTagOf ?x))", "'('(nt:RFE nm:beParentTagOf nt:RFE-88111))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_autorelated_1_full() {

		_setup();

		// XRModel.TRACE_RETE = true;

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(ta-xx nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(ta-yy nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(ta-zz nm:typeOf nm:tag))");

		// _test("(trace-rule (get-rule p2d \"AC2\"))");

		_test("(start p2d)", "305");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasAutoRelatedTag ?y))",
				"'('(ta-xx nm:hasAutoRelatedTag ta) '(ta-yy nm:hasAutoRelatedTag ta) '(ta-zz nm:hasAutoRelatedTag ta) '(ta nm:hasAutoRelatedTag ta-xx) '(ta nm:hasAutoRelatedTag ta-yy) '(ta nm:hasAutoRelatedTag ta-zz))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_child_1_full() {

		_setup();

		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:beParentTagOf tb))");

		_test("(start p2d)", "411");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:beParentTagOf ?y))", "'('(ta nm:beParentTagOf tb))");
		_test("(list-stmt p2d from '(?x nm:beChildTagOf ?y))", "'('(tb nm:beChildTagOf ta))");
		_test("(list-stmt p2d from '(?x nm:typeOf nm:tag))",
				"'('(nt:$TGROUP nm:typeOf nm:tag) '(ta nm:typeOf nm:tag) '(tb nm:typeOf nm:tag))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_class() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:tc nm:beClassTagOf nt:t1))");
		_test("(add-stmt p2d '(nt:tc nm:hasFieldTag nt:te))");
		_test("(add-stmt p2d '(nt:t1-te nm:typeOf nm:tag))");

		_test("(start p2d)", "735");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:beClassTagOf ?y))", "'('(nt:tc nm:beClassTagOf nt:t1))");
		_test("(list-stmt p2d from '(?x nm:beInstanceTagOf ?y))", "'('(nt:t1 nm:beInstanceTagOf nt:tc))");
		_test("(list-stmt p2d from '(?x nm:hasFieldTag ?y))",
				"'('(nt:tc nm:hasFieldTag nt:te) '(nt:t1 nm:hasFieldTag nt:t1-te))");
		_test("(list-stmt p2d from '(?x nm:beFieldTagOf ?y))",
				"'('(nt:te nm:beFieldTagOf nt:tc) '(nt:t1-te nm:beFieldTagOf nt:t1))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_related_1_full() {

		_setup();
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:hasRelatedTag tb))");
		_test("(add-stmt p2d '(ta nm:hasRelatedTag tc))");

		_test("(start p2d)", "259");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasRelatedTag ?y))",
				"'('(ta nm:hasRelatedTag tb) '(ta nm:hasRelatedTag tc) '(tb nm:hasRelatedTag ta) '(tc nm:hasRelatedTag ta))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_remove_descendan_1_full() {

		_setup();
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta nm:beParentTagOf tb))");
		_test("(add-stmt p2d '(ta nm:beParentTagOf tc))");
		_test("(add-stmt p2d '(tb nm:beParentTagOf tc))");

		_test("(start p2d)", "413");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:beParentTagOf ?y))",
				"'('(ta nm:beParentTagOf tb) '(tb nm:beParentTagOf tc))");
		_test("(list-stmt p2d from '(?x nm:beChildTagOf ?y))", "'('(tb nm:beChildTagOf ta) '(tc nm:beChildTagOf tb))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_subcollection_1_full() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(ta1 nm:hasFieldTag f1))");
		_test("(add-stmt p2d '(ta1 nm:beParentTagOf c1))");

		_test("(add-stmt p2d '(ta2 nm:hasFieldTag f2-xx))");
		_test("(add-stmt p2d '(ta2 nm:beParentTagOf c2))");

		_test("(add-stmt p2d '(ta3 nm:hasFieldTag f3))");
		_test("(add-stmt p2d '(ta3 nm:beParentTagOf c3))");
		_test("(add-stmt p2d '(c3-f3 nm:typeOf nm:tag))");

		_test("(add-stmt p2d '(ta4 nm:hasFieldTag f4-xx))");
		_test("(add-stmt p2d '(ta4 nm:beParentTagOf c4))");
		_test("(add-stmt p2d '(c4-xx nm:typeOf nm:tag))");

		_test("(start p2d)", "528");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasSubCollection ?y))",
				"'('(f3 nm:hasSubCollection c3-f3) '(f4-xx nm:hasSubCollection c4-xx))");

		_test("(list-stmt p2d from '(?x nm:beFieldTagOf ?y))",
				"'('(f1 nm:beFieldTagOf ta1) '(f2-xx nm:beFieldTagOf ta2) '(f3 nm:beFieldTagOf ta3) '(f4-xx nm:beFieldTagOf ta4) '(c3-f3 nm:beFieldTagOf c3) '(c4-xx nm:beFieldTagOf c4))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();

	}

	@Test
	public void test_tag_subcollection_2_full() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:ta1 nm:hasFieldTag nt:f1))");
		_test("(add-stmt p2d '(nt:ta1 nm:beParentTagOf nt:c1))");

		_test("(add-stmt p2d '(nt:ta2 nm:hasFieldTag nt:f2-xx))");
		_test("(add-stmt p2d '(nt:ta2 nm:beParentTagOf nt:c2))");

		_test("(add-stmt p2d '(nt:ta3 nm:hasFieldTag nt:f3))");
		_test("(add-stmt p2d '(nt:ta3 nm:beParentTagOf nt:c3))");
		_test("(add-stmt p2d '(nt:c3-f3 nm:typeOf nm:tag))");

		_test("(add-stmt p2d '(nt:ta4 nm:hasFieldTag nt:f4-xx))");
		_test("(add-stmt p2d '(nt:ta4 nm:beParentTagOf nt:c4))");
		_test("(add-stmt p2d '(nt:c4-xx nm:typeOf nm:tag))");

		_test("(start p2d)", "528");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasSubCollection ?y))",
				"'('(nt:f3 nm:hasSubCollection nt:c3-f3) '(nt:f4-xx nm:hasSubCollection nt:c4-xx))");

		_test("(list-stmt p2d from '(?x nm:beFieldTagOf ?y))",
				"'('(nt:f1 nm:beFieldTagOf nt:ta1) '(nt:f2-xx nm:beFieldTagOf nt:ta2) '(nt:f3 nm:beFieldTagOf nt:ta3) '(nt:f4-xx nm:beFieldTagOf nt:ta4) '(nt:c3-f3 nm:beFieldTagOf nt:c3) '(nt:c4-xx nm:beFieldTagOf nt:c4))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}

	@Test
	public void test_tag_subcollection_3_full() {

		_setup();
		// XRModel.TRACE_RETE = true;
		_test("(load \"result/p2d.rulp\")");
		_test("(add-stmt p2d '(nt:OS nm:hasFieldTag nt:Command))");
		_test("(add-stmt p2d '(nt:OS nm:beParentTagOf nt:AS400))");
		_test("(add-stmt p2d '(nt:AS400 nm:beParentTagOf nt:TES))");
		_test("(add-stmt p2d '(nt:AS400-Command nm:typeOf nm:tag))");
		_test("(add-stmt p2d '(nt:TES-Command nm:typeOf nm:tag))");

//		_test("(trace-rule (get-rule p2d \"AC3\"))");

		_test("(start p2d)", "637");
		_test("(state-of p2d)", "completed");
		_test("(list-with-state (list-rule p2d) failed)", "'()");

		_test("(list-stmt p2d from '(?x nm:hasSubCollection ?y))",
				"'('(nt:Command nm:hasSubCollection nt:AS400-Command) '(nt:AS400-Command nm:hasSubCollection nt:TES-Command))");
		_test("(list-stmt p2d from '(?x nm:beFieldTagOf ?y))",
				"'('(nt:Command nm:beFieldTagOf nt:OS) '(nt:AS400-Command nm:beFieldTagOf nt:AS400) '(nt:TES-Command nm:beFieldTagOf nt:TES))");

		_mStatus(1, "p2d");
		_mCount(1, "p2d");
		_eCount(1, "p2d");
		_saveTest();
	}
}