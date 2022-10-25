package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XRModelCount {

	public static final String CK_ADD_CONSTRAINT = "model-addConstraint";

	public static final String CK_ADD_LOAD_NODE_LIS = "model-addLoadNodeListener";

	public static final String CK_ADD_RULE = "model-addRule";

	public static final String CK_ADD_RULE_EXEC_LIS = "model-addRuleExecutedListener";

	public static final String CK_ADD_RULE_FAIL_LIS = "model-addRuleFailedListener";

	public static final String CK_ADD_SAVE_NODE_LIS = "model-addSaveNodeListener";

	public static final String CK_ADD_STMT = "model-addStatement";

	public static final String CK_ADD_STMT_LIS = "model-addStatementListener";

	public static final String CK_ADD_UPDATE_NODE = "model-addUpdateNode";

	public static final String CK_ASSUME_STMT = "model-assumeStatement";

	public static final String CK_DO_GC = "model-doGC";

	public static final String CK_EXEC = "model-execute";

	public static final String CK_FIND_RETE_ENTRY_1 = "model-findReteEntry-1";

	public static final String CK_FIND_RETE_ENTRY_2 = "model-findReteEntry-2";

	public static final String CK_FIX_STMT = "model-fixStatement";

	public static final String CK_GC_COUNT = "model-gc-count";

	public static final String CK_GC_TRIGGER = "model-gc-trigger";

	public static final String CK_GET_VAR = "model-getVar";

	public static final String CK_HALT = "model-halt";

	public static final String CK_HAS_STMT_CACHE = "model-has-stmt-cache";

	public static final String CK_HAS_STMT_HIT = "model-has-stmt-hit";

	public static final String CK_LIST_STMT = "model-listStatements";

	public static final String CK_MAX_STACK_EXECUTE = "model-maxStackExecute";

	public static final String CK_MAX_STACK_NODE_CONTEXT = "model-maxStackNodeContext";

	public static final String CK_QUERY = "model-query";

	public static final String CK_QUERY_ITERATOR = "model-query-iterator";

	public static final String CK_RMV_CONSTRAINT = "model-removeConstraint";

	public static final String CK_RMV_STMT = "model-removeStatement";

	public static final String CK_SAVE = "model-save";

	public static final String CK_SET_CACHE_PATH = "model-setModelCachePath";

	public static final String CK_SET_NODE_LOADER = "model-setNodeLoader";

	public static final String CK_SET_NODE_SAVER = "model-setNodeSaver";

	public static final String CK_START = "model-start";

	public static final String CK_TRY_ADD_STMT = "model-tryAddStatement";

	static List<String> modelCountKeyList = new ArrayList<>();

	static {
		modelCountKeyList.add(CK_GC_TRIGGER);
		modelCountKeyList.add(CK_GC_COUNT);
		modelCountKeyList.add(CK_HAS_STMT_CACHE);
		modelCountKeyList.add(CK_HAS_STMT_HIT);
		modelCountKeyList.add(CK_ADD_CONSTRAINT);
		modelCountKeyList.add(CK_ADD_LOAD_NODE_LIS);
		modelCountKeyList.add(CK_ADD_RULE);
		modelCountKeyList.add(CK_ADD_RULE_EXEC_LIS);
		modelCountKeyList.add(CK_ADD_RULE_FAIL_LIS);
		modelCountKeyList.add(CK_ADD_SAVE_NODE_LIS);
		modelCountKeyList.add(CK_ADD_STMT);
		modelCountKeyList.add(CK_ADD_STMT_LIS);
		modelCountKeyList.add(CK_ADD_UPDATE_NODE);
		modelCountKeyList.add(CK_ASSUME_STMT);
		modelCountKeyList.add(CK_DO_GC);
		modelCountKeyList.add(CK_EXEC);
		modelCountKeyList.add(CK_GET_VAR);
		modelCountKeyList.add(CK_HALT);
		modelCountKeyList.add(CK_FIND_RETE_ENTRY_1);
		modelCountKeyList.add(CK_FIND_RETE_ENTRY_2);
		modelCountKeyList.add(CK_LIST_STMT);
		modelCountKeyList.add(CK_QUERY);
		modelCountKeyList.add(CK_QUERY_ITERATOR);
		modelCountKeyList.add(CK_RMV_CONSTRAINT);
		modelCountKeyList.add(CK_RMV_STMT);
		modelCountKeyList.add(CK_SAVE);
		modelCountKeyList.add(CK_SET_CACHE_PATH);
		modelCountKeyList.add(CK_SET_NODE_LOADER);
		modelCountKeyList.add(CK_SET_NODE_SAVER);
		modelCountKeyList.add(CK_START);
		modelCountKeyList.add(CK_TRY_ADD_STMT);
		modelCountKeyList.add(CK_MAX_STACK_NODE_CONTEXT);
		modelCountKeyList.add(CK_MAX_STACK_EXECUTE);

		modelCountKeyList = Collections.unmodifiableList(modelCountKeyList);
	}

	public static List<String> getCounterKeyList() {
		return modelCountKeyList;
	}

	public long gcCount = 0;

	public long gcTrigger = 0;

	public int hasStmtHitCount = 0;

	public long mcAddConstraint = 0;

	public long mcAddLoadNodeListener = 0;

	public long mcAddRule = 0;

	public long mcAddRuleExecutedListener = 0;

	public long mcAddRuleFailedListener = 0;

	public long mcAddSaveNodeListener = 0;

	public long mcAddStatement = 0;

	public long mcAddStatementListener = 0;

	public long mcAddUpdateNode = 0;

	public long mcAssumeStatement = 0;

	public long mcDoGC = 0;

	public long mcExecute = 0;

	public long mcFindReteEntry1 = 0;

	public long mcFindReteEntry2 = 0;

	public long mcFixStatement = 0;

	public long mcGetVar = 0;

	public long mcHalt = 0;

	public long mcListStatements = 0;

	public long mcMaxStackDeepExecute = 0;

	public long mcMaxStackDeepNodeContext = 0;

	public long mcQuery = 0;

	public long mcQueryIterator = 0;

	public long mcRemoveConstraint = 0;

	public long mcRemoveStatement = 0;

	public long mcSave = 0;

	public long mcSetModelCachePath = 0;

	public long mcSetNodeLoader = 0;

	public long mcSetNodeSaver = 0;

	public long mcStart = 0;

	public long mcTryAddStatement = 0;

	public long getCounterValue(String countkey) {

		switch (countkey) {
		case CK_GC_TRIGGER:
			return gcTrigger;

		case CK_GC_COUNT:
			return gcCount;

		case CK_HAS_STMT_HIT:
			return hasStmtHitCount;

		case CK_ADD_CONSTRAINT:
			return mcAddConstraint;

		case CK_ADD_LOAD_NODE_LIS:
			return mcAddLoadNodeListener;

		case CK_ADD_RULE:
			return mcAddRule;

		case CK_ADD_RULE_EXEC_LIS:
			return mcAddRuleExecutedListener;

		case CK_ADD_RULE_FAIL_LIS:
			return mcAddRuleFailedListener;

		case CK_ADD_SAVE_NODE_LIS:
			return mcAddSaveNodeListener;

		case CK_ADD_STMT:
			return mcAddStatement;

		case CK_ADD_STMT_LIS:
			return mcAddStatementListener;

		case CK_ADD_UPDATE_NODE:
			return mcAddUpdateNode;

		case CK_ASSUME_STMT:
			return mcAssumeStatement;

		case CK_DO_GC:
			return mcDoGC;

		case CK_EXEC:
			return mcExecute;

		case CK_GET_VAR:
			return mcGetVar;

		case CK_HALT:
			return mcHalt;

		case CK_FIND_RETE_ENTRY_1:
			return mcFindReteEntry1;

		case CK_FIND_RETE_ENTRY_2:
			return mcFindReteEntry2;

		case CK_LIST_STMT:
			return mcListStatements;

		case CK_QUERY:
			return mcQuery;

		case CK_QUERY_ITERATOR:
			return mcQueryIterator;

		case CK_RMV_CONSTRAINT:
			return mcRemoveConstraint;

		case CK_RMV_STMT:
			return mcRemoveStatement;

		case CK_SAVE:
			return mcSave;

		case CK_SET_CACHE_PATH:
			return mcSetModelCachePath;

		case CK_SET_NODE_LOADER:
			return mcSetNodeLoader;

		case CK_SET_NODE_SAVER:
			return mcSetNodeSaver;

		case CK_START:
			return mcStart;

		case CK_TRY_ADD_STMT:
			return mcTryAddStatement;

		case CK_MAX_STACK_NODE_CONTEXT:
			return mcMaxStackDeepNodeContext;

		case CK_MAX_STACK_EXECUTE:
			return mcMaxStackDeepExecute;

		}

		return 0;
	}
}
