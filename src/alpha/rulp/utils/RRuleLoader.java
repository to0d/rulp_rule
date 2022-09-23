package alpha.rulp.utils;

import static alpha.rulp.lang.Constant.O_False;
import static alpha.rulp.rule.Constant.A_BS_TRACE;
import static alpha.rulp.rule.Constant.A_BackSearch;
import static alpha.rulp.rule.Constant.A_MODEL;
import static alpha.rulp.rule.Constant.F_ADD_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_ADD_INDEX;
import static alpha.rulp.rule.Constant.F_ADD_LAZY_STMT;
import static alpha.rulp.rule.Constant.F_ADD_NODE;
import static alpha.rulp.rule.Constant.F_ADD_RULE;
import static alpha.rulp.rule.Constant.F_ADD_STMT;
import static alpha.rulp.rule.Constant.F_ASSUME_STMT;
import static alpha.rulp.rule.Constant.F_DUMP_ENTRY_INFO;
import static alpha.rulp.rule.Constant.F_DUMP_NODE_INFO;
import static alpha.rulp.rule.Constant.F_DUMP_STATUS;
import static alpha.rulp.rule.Constant.F_EXEC_RULE;
import static alpha.rulp.rule.Constant.F_FIX_STMT;
import static alpha.rulp.rule.Constant.F_GC_MODEL;
import static alpha.rulp.rule.Constant.F_GET_RETE_ENTRY;
import static alpha.rulp.rule.Constant.F_HAS_STMT;
import static alpha.rulp.rule.Constant.F_LIST_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_LIST_SOURCE_NODE;
import static alpha.rulp.rule.Constant.F_LIST_STMT;
import static alpha.rulp.rule.Constant.F_LIST_STMT_ITERATOR;
import static alpha.rulp.rule.Constant.F_LIST_SUBGRAPH_FOR_QUERY;
import static alpha.rulp.rule.Constant.F_LOAD_STMT;
import static alpha.rulp.rule.Constant.F_MAKE_REF_TREE;
import static alpha.rulp.rule.Constant.F_MODEL_OF;
import static alpha.rulp.rule.Constant.F_OPT_MODEL;
import static alpha.rulp.rule.Constant.F_PRINT_MODEL_STATUS;
import static alpha.rulp.rule.Constant.F_PRINT_RUNNABLE_COUNTER;
import static alpha.rulp.rule.Constant.F_PRINT_TREE;
import static alpha.rulp.rule.Constant.F_PRIORITY_OF;
import static alpha.rulp.rule.Constant.F_QUERY_STMT;
import static alpha.rulp.rule.Constant.F_QUERY_STMT_ITERATOR;
import static alpha.rulp.rule.Constant.F_REMOVE_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_REMOVE_STMT;
import static alpha.rulp.rule.Constant.F_RETE_ENTRY_COUNT_OF;
import static alpha.rulp.rule.Constant.F_RETE_NODE_OF;
import static alpha.rulp.rule.Constant.F_RETE_QUEUE_TYPE_OF;
import static alpha.rulp.rule.Constant.F_SAVE_MODEL;
import static alpha.rulp.rule.Constant.F_SET_DEFAULT_MODEL;
import static alpha.rulp.rule.Constant.F_SET_MODEL_CACHE_PATH;
import static alpha.rulp.rule.Constant.F_SET_NODE_CACHE_PATH;
import static alpha.rulp.rule.Constant.F_SET_NODE_QUEUE_TYPE;
import static alpha.rulp.rule.Constant.F_SET_PRIORITY;
import static alpha.rulp.rule.Constant.F_SIZE_OF_MODEL;
import static alpha.rulp.rule.Constant.F_START;
import static alpha.rulp.rule.Constant.F_STATE_OF;
import static alpha.rulp.rule.Constant.F_TRACE_RULE;
import static alpha.rulp.rule.Constant.F_TRY_ADD_STMT;
import static alpha.rulp.rule.Constant.O_Assumed;
import static alpha.rulp.rule.Constant.O_Completed;
import static alpha.rulp.rule.Constant.O_Defined;
import static alpha.rulp.rule.Constant.O_Failed;
import static alpha.rulp.rule.Constant.O_Halting;
import static alpha.rulp.rule.Constant.O_Limit;
import static alpha.rulp.rule.Constant.O_On;
import static alpha.rulp.rule.Constant.O_Partial;
import static alpha.rulp.rule.Constant.O_Priority;
import static alpha.rulp.rule.Constant.O_Reasoned;
import static alpha.rulp.rule.Constant.O_Removed;
import static alpha.rulp.rule.Constant.O_Runnable;
import static alpha.rulp.rule.Constant.O_Running;
import static alpha.rulp.rule.Constant.O_State;
import static alpha.rulp.rule.Constant.O_Type;
import static alpha.rulp.rule.Constant.O_Where;

import java.io.IOException;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRObjectLoader;
import alpha.rulp.ximpl.bs.XRFactorBackSearch;
import alpha.rulp.ximpl.constraint.XRFactorAddConstraint;
import alpha.rulp.ximpl.constraint.XRFactorRemoveConstraint;
import alpha.rulp.ximpl.factor.XRFactorAddIndex;
import alpha.rulp.ximpl.factor.XRFactorAddLazyStmt;
import alpha.rulp.ximpl.factor.XRFactorAddNode;
import alpha.rulp.ximpl.factor.XRFactorAddRule;
import alpha.rulp.ximpl.factor.XRFactorAddStmt;
import alpha.rulp.ximpl.factor.XRFactorAssumeStmt;
import alpha.rulp.ximpl.factor.XRFactorExecuteRule;
import alpha.rulp.ximpl.factor.XRFactorFixStmt;
import alpha.rulp.ximpl.factor.XRFactorGcModel;
import alpha.rulp.ximpl.factor.XRFactorGetReteEntry;
import alpha.rulp.ximpl.factor.XRFactorHasStmt;
import alpha.rulp.ximpl.factor.XRFactorListConstraint;
import alpha.rulp.ximpl.factor.XRFactorListSourceNode;
import alpha.rulp.ximpl.factor.XRFactorListStmt;
import alpha.rulp.ximpl.factor.XRFactorListStmtIterator;
import alpha.rulp.ximpl.factor.XRFactorListSubGraphForQuery;
import alpha.rulp.ximpl.factor.XRFactorLoadStmt;
import alpha.rulp.ximpl.factor.XRFactorMakeRefTree;
import alpha.rulp.ximpl.factor.XRFactorModelOf;
import alpha.rulp.ximpl.factor.XRFactorOptModel;
import alpha.rulp.ximpl.factor.XRFactorPrintTree;
import alpha.rulp.ximpl.factor.XRFactorPriorityOf;
import alpha.rulp.ximpl.factor.XRFactorRemoveStmt;
import alpha.rulp.ximpl.factor.XRFactorReteEntryCountOf;
import alpha.rulp.ximpl.factor.XRFactorReteNodeOf;
import alpha.rulp.ximpl.factor.XRFactorReteQueueTypeOf;
import alpha.rulp.ximpl.factor.XRFactorSaveModel;
import alpha.rulp.ximpl.factor.XRFactorSetDefaultModel;
import alpha.rulp.ximpl.factor.XRFactorSetModelCachePath;
import alpha.rulp.ximpl.factor.XRFactorSetNodeCachePath;
import alpha.rulp.ximpl.factor.XRFactorSetNodeQueueType;
import alpha.rulp.ximpl.factor.XRFactorSetPriority;
import alpha.rulp.ximpl.factor.XRFactorSizeOfModel;
import alpha.rulp.ximpl.factor.XRFactorStart;
import alpha.rulp.ximpl.factor.XRFactorStateOf;
import alpha.rulp.ximpl.factor.XRFactorTraceRule;
import alpha.rulp.ximpl.factor.XRFactorTryAddStmt;
import alpha.rulp.ximpl.model.XRModelClass;
import alpha.rulp.ximpl.query.XRFactorQueryStmt;
import alpha.rulp.ximpl.query.XRFactorQueryStmtIterator;
import alpha.rulp.ximpl.status.XRFactorDumpEntryInfo;
import alpha.rulp.ximpl.status.XRFactorDumpNodeInfo;
import alpha.rulp.ximpl.status.XRFactorDumpStatus;
import alpha.rulp.ximpl.status.XRFactorPrintModelStatus;
import alpha.rulp.ximpl.status.XRFactorPrintRunnableCounter;

public class RRuleLoader implements IRObjectLoader {

	@Override
	public void load(IRInterpreter interpreter, IRFrame frame) throws RException, IOException {

		// RunStatus
		RulpUtil.addFrameObject(frame, O_Completed);
		RulpUtil.addFrameObject(frame, O_Runnable);
		RulpUtil.addFrameObject(frame, O_Running);
		RulpUtil.addFrameObject(frame, O_Halting);
		RulpUtil.addFrameObject(frame, O_Failed);
		RulpUtil.addFrameObject(frame, O_Partial);

		// ReteStatus
		RulpUtil.addFrameObject(frame, O_Defined);
		RulpUtil.addFrameObject(frame, O_Reasoned);
		RulpUtil.addFrameObject(frame, O_Assumed);
		RulpUtil.addFrameObject(frame, O_Removed);

		// Other symbol
		RulpUtil.addFrameObject(frame, O_Limit);
		RulpUtil.addFrameObject(frame, O_Type);
		RulpUtil.addFrameObject(frame, O_State);
		RulpUtil.addFrameObject(frame, O_Priority);
		RulpUtil.addFrameObject(frame, O_On);
		RulpUtil.addFrameObject(frame, O_Where);

		RulpUtil.addFrameObject(frame, new XRModelClass(A_MODEL, frame));

		RuleUtil.init(frame);

		RulpUtil.addFrameObject(frame, new XRFactorStateOf(F_STATE_OF));
		RulpUtil.addFrameObject(frame, new XRFactorStart(F_START));
		RulpUtil.addFrameObject(frame, new XRFactorAddStmt(F_ADD_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorAddRule(F_ADD_RULE));
		RulpUtil.addFrameObject(frame, new XRFactorRemoveStmt(F_REMOVE_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorListStmt(F_LIST_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorQueryStmt(F_QUERY_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorAddLazyStmt(F_ADD_LAZY_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorTraceRule(F_TRACE_RULE));
		RulpUtil.addFrameObject(frame, new XRFactorAddNode(F_ADD_NODE));
		RulpUtil.addFrameObject(frame, new XRFactorFixStmt(F_FIX_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorLoadStmt(F_LOAD_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorHasStmt(F_HAS_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorGcModel(F_GC_MODEL));
		RulpUtil.addFrameObject(frame, new XRFactorPrintModelStatus(F_PRINT_MODEL_STATUS));
		RulpUtil.addFrameObject(frame, new XRFactorPrintRunnableCounter(F_PRINT_RUNNABLE_COUNTER));
		RulpUtil.addFrameObject(frame, new XRFactorOptModel(F_OPT_MODEL));
		RulpUtil.addFrameObject(frame, new XRFactorMakeRefTree(F_MAKE_REF_TREE));
		RulpUtil.addFrameObject(frame, new XRFactorSizeOfModel(F_SIZE_OF_MODEL));
//		RulpUtil.addFrameObject(frame, new XRFactorGetRule(F_GET_RULE));
		RulpUtil.addFrameObject(frame, new XRFactorPriorityOf(F_PRIORITY_OF));
		RulpUtil.addFrameObject(frame, new XRFactorSetPriority(F_SET_PRIORITY));
		RulpUtil.addFrameObject(frame, new XRFactorListSourceNode(F_LIST_SOURCE_NODE));
		RulpUtil.addFrameObject(frame, new XRFactorSetModelCachePath(F_SET_MODEL_CACHE_PATH));
		RulpUtil.addFrameObject(frame, new XRFactorSetNodeCachePath(F_SET_NODE_CACHE_PATH));
		RulpUtil.addFrameObject(frame, new XRFactorSaveModel(F_SAVE_MODEL));
		RulpUtil.addFrameObject(frame, new XRFactorSetDefaultModel(F_SET_DEFAULT_MODEL));
		RulpUtil.addFrameObject(frame, new XRFactorReteNodeOf(F_RETE_NODE_OF));
		RulpUtil.addFrameObject(frame, new XRFactorReteEntryCountOf(F_RETE_ENTRY_COUNT_OF));
		RulpUtil.addFrameObject(frame, new XRFactorGetReteEntry(F_GET_RETE_ENTRY));
		RulpUtil.addFrameObject(frame, new XRFactorTryAddStmt(F_TRY_ADD_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorModelOf(F_MODEL_OF));
		RulpUtil.addFrameObject(frame, new XRFactorReteQueueTypeOf(F_RETE_QUEUE_TYPE_OF));
		RulpUtil.addFrameObject(frame, new XRFactorSetNodeQueueType(F_SET_NODE_QUEUE_TYPE));
		RulpUtil.addFrameObject(frame, new XRFactorAddIndex(F_ADD_INDEX));
		RulpUtil.addFrameObject(frame, new XRFactorBackSearch(A_BackSearch));
		RulpUtil.addFrameObject(frame, new XRFactorListSubGraphForQuery(F_LIST_SUBGRAPH_FOR_QUERY));
		RulpUtil.addFrameObject(frame, new XRFactorPrintTree(F_PRINT_TREE));
		RulpUtil.addFrameObject(frame, new XRFactorExecuteRule(F_EXEC_RULE));
		RulpUtil.addFrameObject(frame, new XRFactorQueryStmtIterator(F_QUERY_STMT_ITERATOR));
		RulpUtil.addFrameObject(frame, new XRFactorListStmtIterator(F_LIST_STMT_ITERATOR));

		// Constraint
		RulpUtil.addFrameObject(frame, new XRFactorAddConstraint(F_ADD_CONSTRAINT));
		RulpUtil.addFrameObject(frame, new XRFactorRemoveConstraint(F_REMOVE_CONSTRAINT));
		RulpUtil.addFrameObject(frame, new XRFactorListConstraint(F_LIST_CONSTRAINT));

		// Assume
		RulpUtil.addFrameObject(frame, new XRFactorAssumeStmt(F_ASSUME_STMT));
		RulpUtil.addFrameObject(frame, new XRFactorDumpStatus(F_DUMP_STATUS));
		RulpUtil.addFrameObject(frame, new XRFactorDumpNodeInfo(F_DUMP_NODE_INFO));
		RulpUtil.addFrameObject(frame, new XRFactorDumpEntryInfo(F_DUMP_ENTRY_INFO));

		// Variables
		RulpUtil.setLocalVar(frame, A_BS_TRACE, O_False);

		// Load rule library
		LoadUtil.loadRulpFromJar(interpreter, frame, "alpha/resource/rule.res", "utf-8");

//		// Native Class Initialization
//		SearchFactory.initScopeClass(frame);
	}

}
