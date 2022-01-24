package alpha.rulp.rule;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.utils.RulpFactory;

public interface Constant {

	String A_RETE_TYPE = "rete-type";

	String A_Asc = "asc";

	String A_Assumed = "assumed";

	String A_By = "by";

	String A_CMP_ENTRY_INDEX = "cmp-entry-index";

	String A_CMP_ENTRY_VALUE = "cmp-entry-value";

	String A_CMP_ENTRY_VAR = "cmp-entry-var";

	String A_CMP_VAR_VALUE = "cmp-var-value";

	String A_CMP_VAR_VAR = "cmp-var-var";

	String A_Completed = "completed";

	String A_DEFAULT_MODEL = "default_model";

	String A_Defined = "defined";

	String A_Desc = "desc";

	String A_ENTRY = "entry";

	String A_ENTRY_ORDER = "entry-order";

	String A_Failed = "failed";

	String A_Halting = "halting";

	String A_Limit = "limit";

	String A_M_TRACE = "?model-trace";

	String A_Max = "max";

	String A_Min = "min";

	String A_MODEL = "model";

	String A_Name = "name";

	String A_NOT_NULL = "not-null";

	String A_On = "on";

	String A_One_Of = "one-of";

	String A_Order = "order";

	String A_Order_by = "order by";

	String A_Partial = "partial";

	String A_Priority = "priority";

	String A_Reasoned = "reasoned";

	String A_Removed = "removed";

	String A_RULE = "rule";

	String A_Runnable = "runnable";

	String A_Running = "running";

	String A_SCHEMA = "schema";

	String A_SCOPE = "scope";

	String A_SELECT = "select";

	String A_SINGLE = "single";

	String A_State = "state";

	String A_TABLE = "table";

	String A_Type = "type";

	String A_Uniq = "uniq";

	String A_VIEW = "view";

	String A_Where = "where";

	IRAtom C_ENTRY = RulpFactory.createAtom(A_ENTRY);

	IRAtom C_RULE = RulpFactory.createAtom(A_RULE);

	String F_ADD_CONSTRAINT = "add-constraint";

	String F_ADD_LAZY_STMT = "add-lazy-stmt";

	String F_ADD_NODE = "add-node";

	String F_ADD_RULE = "add-rule";

	String F_ADD_STMT = "add-stmt";

	String F_ASSUME_STMT = "assume-stmt";

	String F_CREATE = "create";

	String F_DEF_QUERY_COUNTER = "def-query-counter";

	String F_DEFR = "defr";

	String F_DEFS = "defs";

	String F_DEFS_S = "->";

	String F_DUMP_STATUS = "dump-status";

	String F_FIX_STMT = "fix-stmt";

	String F_GC_MODEL = "gc-model";

	String F_GET_RULE = "get-rule";

	String F_HAS_STMT = "has-stmt";

	String F_LIST_CONSTRAINT = "list-constraint";

	String F_LIST_SOURCE_NODE = "list-source-node";

	String F_LIST_STMT = "list-stmt";

	String F_LOAD_STMT = "load-stmt";

	String F_MBR_RULE_GROUP_NAMES = "$RG-names$";

	String F_MBR_RULE_GROUP_PRE = "$RG$";

	String F_NOT_EQUAL = "not-equal";

	String F_OPT_MODEL = "opt-model";

	String F_PRINT_MODEL_STATUS = "print-model-status";

	String F_PRINT_RUNNABLE_COUNTER = "print-runnable-counter";

	String F_PRIORITY_OF = "priority-of";

	String F_PROVE_STMT = "prove-stmt";

	String F_QUERY_STMT = "query-stmt";

	String F_REMOVE_CONSTRAINT = "remove-constraint";

	String F_REMOVE_STMT = "remove-stmt";

	String F_RETE_NODE_OF = "rete-node-of";

	String F_SAVE_MODEL = "save-model";

	String F_SEARCH = "search";

	String F_SET = "set";

	String F_SET_DEFAULT_MODEL = "set-default-model";

	String F_SET_MODEL_CACHE_PATH = "set-model-cache-path";

	String F_SET_NODE_CACHE_PATH = "set-node-cache-path";

	String F_SET_RULE_PRIORITY = "set-rule-priority";

	String F_SIZE_OF_MODEL = "size-of-model";

	String F_START = "start";

	String F_STATE_OF = "state-of";

//	String F_UPDATE_STMT = "update-stmt";

	String F_TRACE_RULE = "trace-rule";

//	String F_TRY_ADD_STMT = "try-add-stmt";

	String F_VAR_CHANGED = "var-changed";

	IRAtom O_Assumed = RulpFactory.createAtom(A_Assumed);

	IRAtom O_Completed = RulpFactory.createAtom(A_Completed);

	IRAtom O_Defined = RulpFactory.createAtom(A_Defined);

	IRAtom O_Failed = RulpFactory.createAtom(A_Failed);

	IRAtom O_Halting = RulpFactory.createAtom(A_Halting);

	IRAtom O_Limit = RulpFactory.createAtom(A_Limit);

	IRAtom O_On = RulpFactory.createAtom(A_On);

	IRAtom O_Partial = RulpFactory.createAtom(A_Partial);

	IRAtom O_Priority = RulpFactory.createAtom(A_Priority);

	IRAtom O_Reasoned = RulpFactory.createAtom(A_Reasoned);

	IRAtom O_Removed = RulpFactory.createAtom(A_Removed);

	IRAtom O_Runnable = RulpFactory.createAtom(A_Runnable);

	IRAtom O_Running = RulpFactory.createAtom(A_Running);

	IRAtom O_State = RulpFactory.createAtom(A_State);

	IRAtom O_Type = RulpFactory.createAtom(A_Type);

	IRAtom O_Where = RulpFactory.createAtom(A_Where);

	int RETE_PRIORITY_ASSUMED = 9;

	int RETE_PRIORITY_DEAD = -99;

	int RETE_PRIORITY_DEFAULT = 99;

	int RETE_PRIORITY_DISABLED = -9;

	int RETE_PRIORITY_INACTIVE = 0;

	int RETE_PRIORITY_MAXIMUM = 499; // maximum

//	int RETE_PRIORITY_GROUP_MAX = 699;
//
//	int RETE_PRIORITY_GROUP_MIN = 600;

	int RETE_PRIORITY_PARTIAL_MAX = 599;

	int RETE_PRIORITY_PARTIAL_MIN = 500;

	int RETE_PRIORITY_ROOT = 900;

	int RETE_PRIORITY_SYSTEM = 999;

	int STMT_MAX_LEN = 99;

	int STMT_MIN_LEN = 1;

	String V_M_CST_INIT = "?cst-init";

	String V_M_STATE = "?model-state";
}
