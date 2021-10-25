package alpha.rulp.utils;

import static alpha.rulp.rule.Constant.A_MODEL;
import static alpha.rulp.rule.Constant.F_ADD_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_ADD_LAZY_STMT;
import static alpha.rulp.rule.Constant.F_ADD_NODE;
import static alpha.rulp.rule.Constant.F_ADD_RULE;
import static alpha.rulp.rule.Constant.F_ADD_STMT;
import static alpha.rulp.rule.Constant.F_ASSUME_STMT;
import static alpha.rulp.rule.Constant.F_DUMP_STATUS;
import static alpha.rulp.rule.Constant.F_FIX_STMT;
import static alpha.rulp.rule.Constant.F_GC_MODEL;
import static alpha.rulp.rule.Constant.F_GET_RULE;
import static alpha.rulp.rule.Constant.F_HAS_STMT;
import static alpha.rulp.rule.Constant.F_LIST_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_LIST_RULE;
import static alpha.rulp.rule.Constant.F_LIST_SOURCE_NODE;
import static alpha.rulp.rule.Constant.F_LIST_STMT;
import static alpha.rulp.rule.Constant.F_OPT_MODEL;
import static alpha.rulp.rule.Constant.F_PRINT_MODEL_STATUS;
import static alpha.rulp.rule.Constant.F_PRINT_RUNNABLE_COUNTER;
import static alpha.rulp.rule.Constant.F_PRIORITY_OF;
import static alpha.rulp.rule.Constant.F_PROVE_STMT;
import static alpha.rulp.rule.Constant.F_QUERY_STMT;
import static alpha.rulp.rule.Constant.F_REMOVE_CONSTRAINT;
import static alpha.rulp.rule.Constant.F_REMOVE_STMT;
import static alpha.rulp.rule.Constant.F_SAVE_MODEL;
import static alpha.rulp.rule.Constant.F_SEARCH_STMT;
import static alpha.rulp.rule.Constant.F_SET_DEFAULT_MODEL;
import static alpha.rulp.rule.Constant.F_SET_MODEL_CACHE_PATH;
import static alpha.rulp.rule.Constant.F_SET_NODE_CACHE_PATH;
import static alpha.rulp.rule.Constant.F_SET_RULE_PRIORITY;
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
import static alpha.rulp.ximpl.mts.Constant.MTS_NS;
import static alpha.rulp.ximpl.rbs.Constant.RBS_NS;

import java.io.IOException;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRObjectLoader;
import alpha.rulp.ximpl.constraint.XRFactorAddConstraint;
import alpha.rulp.ximpl.constraint.XRFactorListConstraint;
import alpha.rulp.ximpl.constraint.XRFactorRemoveConstraint;
import alpha.rulp.ximpl.factor.XRFactorAddLazyStmt;
import alpha.rulp.ximpl.factor.XRFactorAddNode;
import alpha.rulp.ximpl.factor.XRFactorAddRule;
import alpha.rulp.ximpl.factor.XRFactorAddStmt;
import alpha.rulp.ximpl.factor.XRFactorAssumeStmt;
import alpha.rulp.ximpl.factor.XRFactorDumpStatus;
import alpha.rulp.ximpl.factor.XRFactorFixStmt;
import alpha.rulp.ximpl.factor.XRFactorGcModel;
import alpha.rulp.ximpl.factor.XRFactorGetRule;
import alpha.rulp.ximpl.factor.XRFactorHasStmt;
import alpha.rulp.ximpl.factor.XRFactorListRule;
import alpha.rulp.ximpl.factor.XRFactorListSourceNodes;
import alpha.rulp.ximpl.factor.XRFactorListStmt;
import alpha.rulp.ximpl.factor.XRFactorOptModel;
import alpha.rulp.ximpl.factor.XRFactorPrintModelStatus;
import alpha.rulp.ximpl.factor.XRFactorPrintRunnableCounter;
import alpha.rulp.ximpl.factor.XRFactorPriorityOf;
import alpha.rulp.ximpl.factor.XRFactorProveStmt;
import alpha.rulp.ximpl.factor.XRFactorQueryStmt;
import alpha.rulp.ximpl.factor.XRFactorRemoveStmt;
import alpha.rulp.ximpl.factor.XRFactorSaveModel;
import alpha.rulp.ximpl.factor.XRFactorSetDefaultModel;
import alpha.rulp.ximpl.factor.XRFactorSetModelCachePath;
import alpha.rulp.ximpl.factor.XRFactorSetNodeCachePath;
import alpha.rulp.ximpl.factor.XRFactorSetRulePriority;
import alpha.rulp.ximpl.factor.XRFactorSizeOfModel;
import alpha.rulp.ximpl.factor.XRFactorStart;
import alpha.rulp.ximpl.factor.XRFactorStateOf;
import alpha.rulp.ximpl.factor.XRFactorTraceRule;
import alpha.rulp.ximpl.factor.XRFactorTryAddStmt;
import alpha.rulp.ximpl.model.XRModelClass;
import alpha.rulp.ximpl.mts.MTSUtil;
import alpha.rulp.ximpl.mts.XRFactorSearchStmt;
import alpha.rulp.ximpl.rbs.RBSUtil;
import alpha.rulp.ximpl.scope.ScopeFactory;

public class RRuleLoader implements IRObjectLoader {

	@Override
	public void load(IRInterpreter interpreter, IRFrame systemFrame) throws RException, IOException {

		// RunStatus
		RulpUtil.addFrameObject(systemFrame, O_Completed);
		RulpUtil.addFrameObject(systemFrame, O_Runnable);
		RulpUtil.addFrameObject(systemFrame, O_Running);
		RulpUtil.addFrameObject(systemFrame, O_Halting);
		RulpUtil.addFrameObject(systemFrame, O_Failed);
		RulpUtil.addFrameObject(systemFrame, O_Partial);

		// ReteStatus
		RulpUtil.addFrameObject(systemFrame, O_Defined);
		RulpUtil.addFrameObject(systemFrame, O_Reasoned);
		RulpUtil.addFrameObject(systemFrame, O_Assumed);
		RulpUtil.addFrameObject(systemFrame, O_Removed);

		// Other symbol
		RulpUtil.addFrameObject(systemFrame, O_Limit);
		RulpUtil.addFrameObject(systemFrame, O_Type);
		RulpUtil.addFrameObject(systemFrame, O_State);
		RulpUtil.addFrameObject(systemFrame, O_Priority);
		RulpUtil.addFrameObject(systemFrame, O_On);
		RulpUtil.addFrameObject(systemFrame, O_Where);

		RulpUtil.addFrameObject(systemFrame, new XRModelClass(A_MODEL, systemFrame));

		RuleUtil.init(systemFrame);

		RulpUtil.addFrameObject(systemFrame, new XRFactorStateOf(F_STATE_OF));
		RulpUtil.addFrameObject(systemFrame, new XRFactorStart(F_START));
		RulpUtil.addFrameObject(systemFrame, new XRFactorAddStmt(F_ADD_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorAddRule(F_ADD_RULE));
		RulpUtil.addFrameObject(systemFrame, new XRFactorRemoveStmt(F_REMOVE_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorListStmt(F_LIST_STMT));
//		RulpUtil.addFrameObject(systemFrame, new XRFactorUpdateStmt(F_UPDATE_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorQueryStmt(F_QUERY_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorAddLazyStmt(F_ADD_LAZY_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorTraceRule(F_TRACE_RULE));
		RulpUtil.addFrameObject(systemFrame, new XRFactorAddNode(F_ADD_NODE));
		RulpUtil.addFrameObject(systemFrame, new XRFactorFixStmt(F_FIX_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSearchStmt(F_SEARCH_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorTryAddStmt(F_TRY_ADD_STMT));

//		RulpUtil.setMember(modelClass, F_ADD_LAZY_STMT, new XRFactorAddLazyStmt(F_ADD_LAZY_STMT));

		RulpUtil.addFrameObject(systemFrame, new XRFactorHasStmt(F_HAS_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorListRule(F_LIST_RULE));
//		RulpUtil.addFrameObject(systemFrame, new XRFactorListObj(F_LIST_OBJ));
		RulpUtil.addFrameObject(systemFrame, new XRFactorGcModel(F_GC_MODEL));
		RulpUtil.addFrameObject(systemFrame, new XRFactorPrintModelStatus(F_PRINT_MODEL_STATUS));
		RulpUtil.addFrameObject(systemFrame, new XRFactorPrintRunnableCounter(F_PRINT_RUNNABLE_COUNTER));
		RulpUtil.addFrameObject(systemFrame, new XRFactorOptModel(F_OPT_MODEL));
		RulpUtil.addFrameObject(systemFrame, new XRFactorProveStmt(F_PROVE_STMT));

		RulpUtil.addFrameObject(systemFrame, new XRFactorSizeOfModel(F_SIZE_OF_MODEL));
		RulpUtil.addFrameObject(systemFrame, new XRFactorGetRule(F_GET_RULE));
		RulpUtil.addFrameObject(systemFrame, new XRFactorPriorityOf(F_PRIORITY_OF));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSetRulePriority(F_SET_RULE_PRIORITY));
		RulpUtil.addFrameObject(systemFrame, new XRFactorListSourceNodes(F_LIST_SOURCE_NODE));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSetModelCachePath(F_SET_MODEL_CACHE_PATH));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSetNodeCachePath(F_SET_NODE_CACHE_PATH));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSaveModel(F_SAVE_MODEL));
		RulpUtil.addFrameObject(systemFrame, new XRFactorSetDefaultModel(F_SET_DEFAULT_MODEL));

		// Constraint
		RulpUtil.addFrameObject(systemFrame, new XRFactorAddConstraint(F_ADD_CONSTRAINT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorRemoveConstraint(F_REMOVE_CONSTRAINT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorListConstraint(F_LIST_CONSTRAINT));

		// Assume
		RulpUtil.addFrameObject(systemFrame, new XRFactorAssumeStmt(F_ASSUME_STMT));
		RulpUtil.addFrameObject(systemFrame, new XRFactorDumpStatus(F_DUMP_STATUS));

		// Load rule library
		LoadUtil.loadRulpFromJar(interpreter, systemFrame, "alpha/resource/rule.rulp", "utf-8");

		// RBS init
		RulpUtil.registerNameSpaceLoader(interpreter, interpreter.getMainFrame(), RBS_NS, (inp, frame) -> {
			RBSUtil.init(inp, frame);
		});

		// MTS init
		RulpUtil.registerNameSpaceLoader(interpreter, interpreter.getMainFrame(), MTS_NS, (inp, frame) -> {
			MTSUtil.init(inp, frame);
		});

		// Native Class Initialization
		ScopeFactory.initScopeClass(systemFrame);

	}

}
