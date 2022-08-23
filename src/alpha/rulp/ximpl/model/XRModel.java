package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_MODEL;
import static alpha.rulp.rule.Constant.DEF_GC_CAPACITY;
import static alpha.rulp.rule.Constant.DEF_GC_INACTIVE_LEAF;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.V_M_CST_INIT;
import static alpha.rulp.rule.Constant.V_M_GC_CAPACITY;
import static alpha.rulp.rule.Constant.V_M_GC_INACTIVE_LEAF;
import static alpha.rulp.rule.Constant.V_M_GC_INTERVAL;
import static alpha.rulp.rule.Constant.V_M_GC_MAX_CACHE_NODE;
import static alpha.rulp.rule.Constant.V_M_STATE;
import static alpha.rulp.rule.RReteStatus.DEFINE;
import static alpha.rulp.rule.RReteStatus.REMOVE;
import static alpha.rulp.rule.RReteStatus.TEMP__;
import static alpha.rulp.rule.RRunState.Completed;
import static alpha.rulp.rule.RRunState.Failed;
import static alpha.rulp.rule.RRunState.Halting;
import static alpha.rulp.rule.RRunState.Partial;
import static alpha.rulp.rule.RRunState.Runnable;
import static alpha.rulp.rule.RRunState.Running;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModelCounter;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRTransaction;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRCacheWorker.CacheStatus;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.cache.XRStmtFileDefaultCacher;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.RConstraintConflict;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.entry.REntryQueueType;
import alpha.rulp.ximpl.entry.XREntryQueueOrder;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRNodeGraph.IRNodeSubGraph;
import alpha.rulp.ximpl.node.RReteStage;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.SourceNode;
import alpha.rulp.ximpl.node.XRNodeGraph;
import alpha.rulp.ximpl.node.XTempVarBuilder;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRModel extends AbsRInstance implements IRModel {

	static enum RUpdateResult {

		CHANGE, INVALID, NEW, NOCHANGE;

		public static boolean isValidUpdate(RUpdateResult rst) {

			if (rst == null) {
				return false;
			}
			switch (rst) {
			case CHANGE:
			case NEW:
				return true;
			default:
				return false;
			}
		}
	}

	static class XCount {
		public int count = 0;
	}

//	public static boolean RuleUtility.isModelTrace() = false;

	static class XRBackSearcher {

		static class BackNode {

			public IAction action;

			public ArrayList<BackNode> childNodes;

			public int curChildIndex = -1;

			public BackNode parentNode;

			public boolean rst;

			public SourceNode sourceNode;

			public BSStats status;

			public IRList stmt;

			public BSType type;

			public void addChild(BackNode child) {

				if (this.childNodes == null) {
					this.childNodes = new ArrayList<>();
				}

				this.childNodes.add(child);
				child.parentNode = this;
			}

			public int getChildCount() {
				return this.childNodes == null ? 0 : this.childNodes.size();
			}

			public String toString() {
				return String.format("type=%s, stmt=%s", "" + type, "" + stmt);
			}
		}

		static enum BSStats {
			COMPLETED, EXPAND, INIT, PROCESS
		}

		static enum BSType {
			AND, OR
		}

		static BackNode _new(IRList stmt, BSType type) {

			BackNode node = new BackNode();
			node.stmt = stmt;
			node.type = type;
			node.status = BSStats.INIT;

			return node;
		}

		IRNodeGraph graph;

		XRModel model;

		Map<String, BackNode> nodeMap = new HashMap<>();

		ArrayList<BackNode> queryStack = new ArrayList<>();

		BackNode rootNode;

		public XRBackSearcher(XRModel model) {
			super();
			this.model = model;
			this.graph = model.getNodeGraph();
		}

		void _expandAndNode(BackNode node) throws RException {

			IActionSimpleStmt addAction = (IActionSimpleStmt) node.action;
			int[] inheritIndexs = addAction.getInheritIndexs();
			int inheritSize = inheritIndexs.length;
			if (inheritSize != node.stmt.size()) {
				throw new RException("invalid action: " + addAction);
			}

			Map<String, IRObject> varValueMap = new HashMap<>();

			for (int i = 0; i < inheritSize; ++i) {
				int inheritIndex = inheritIndexs[i];
				if (inheritIndex != -1) {

					IRObject var = node.sourceNode.rule.getVarEntry()[inheritIndex];
					if (var == null) {
						throw new RException("invalid inheritIndex: " + inheritIndex);
					}

					varValueMap.put(RulpUtil.toString(var), node.stmt.get(i));
				}
			}

			for (IRList list : node.sourceNode.rule.getMatchStmtList()) {
				if (ReteUtil.isAlphaMatchTree(list)) {

					IRList newStmt = (IRList) RuntimeUtil.rebuild(list, varValueMap);
					if (!ReteUtil.isReteStmtNoVar(newStmt)) {
						throw new RException("can't prove stmt: " + newStmt);
					}

					node.addChild(_new(newStmt, BSType.OR));
				}
			}
		}

		void _expandOrNode(BackNode node) throws RException {

			for (SourceNode sn : graph.listSourceNodes(node.stmt)) {
				for (IAction action : sn.actionList) {

					if (action.getActionType() != RActionType.ADD) {
						continue;
					}

					BackNode child = _new(node.stmt, BSType.AND);
					child.sourceNode = sn;
					child.action = action;

					node.addChild(child);
				}
			}
		}

		void _processAndNode(BackNode node) throws RException {

			// need trigger all related rete-node

			ArrayList<IRReteNode> rootNodes = new ArrayList<>();
			ArrayList<Set<String>> preStmtUniqList = new ArrayList<>();

			for (BackNode childNode : node.childNodes) {

				IRList stmt = childNode.stmt;

				IRReteNode rootNode = graph.findRootNode(stmt.getNamedName(), stmt.size());
				Set<String> uniqSet = null;

				int pos = rootNodes.indexOf(rootNode);
				if (pos == -1) {
					rootNodes.add(rootNode);
					uniqSet = new HashSet<>();
					preStmtUniqList.add(uniqSet);
				} else {
					uniqSet = preStmtUniqList.get(pos);
				}

				uniqSet.add(ReteUtil.uniqName(stmt));
			}

			int rootNodeCount = rootNodes.size();

			for (int i = 0; i < rootNodeCount; ++i) {

				IRReteNode rootNode = rootNodes.get(i);
				int rootEntrySize = rootNode.getEntryQueue().size();

				for (IRReteNode childNode : rootNode.getChildNodes()) {
					for (int j = 0; j < 2; ++j) {
						if (childNode.getParentNodes()[j] == rootNode) {
							// dd
						}
					}
				}

			}

		}

		public boolean search(IRList stmt) throws RException {

			rootNode = _new(stmt, BSType.OR);
			BackNode curNode = rootNode;

			int update = 0;
			int lastUpdate = -1;

			BS_LOOP: while (rootNode.status != BSStats.COMPLETED) {

				if (lastUpdate == update) {
					throw new RException("dead loop found" + curNode);
				}

				lastUpdate = update;

				BSStats oldStatus = curNode.status;

				switch (curNode.type) {

				case OR:

					switch (oldStatus) {

					case INIT:

						// has statement
						if (model._findRootEntry(curNode.stmt, 0) != null) {
							curNode.status = BSStats.COMPLETED;
							curNode.rst = true;
							++update;
							continue BS_LOOP;
						}

						_expandOrNode(curNode);
						curNode.status = BSStats.EXPAND;
						++update;

						break;

					case EXPAND:
						break;

					case PROCESS:
						break;

					case COMPLETED:

						BackNode parentNode = curNode.parentNode;

						// (and false xx xx) ==> false
						if (!curNode.rst) {
							parentNode.status = BSStats.PROCESS;
							parentNode.rst = false;
						}
						// Scan next if have more brother node
						else if ((++parentNode.curChildIndex) < parentNode.getChildCount()) {
							curNode = parentNode.childNodes.get(parentNode.curChildIndex);
						}
						// No child need update, mark the parent's result is true
						else {
							parentNode.status = BSStats.PROCESS;
							parentNode.rst = true;
							curNode = parentNode;
						}

						++update;
						continue BS_LOOP;

					default:
						throw new RException("unknown status: " + curNode.status);

					}

					break;

				case AND:

					switch (oldStatus) {

					case INIT:
						_expandAndNode(curNode);
						curNode.status = BSStats.EXPAND;

						break;

					case EXPAND:
						break;

					case PROCESS:
						if (curNode.rst) {
							_processAndNode(curNode);
						}
						curNode.status = BSStats.COMPLETED;
						break;

					case COMPLETED:
						break;

					default:
						throw new RException("unknown status: " + curNode.status);

					}

					break;

				default:
					throw new RException("unknown type: " + curNode.type);
				}

				// first time process
				if (oldStatus == BSStats.INIT && curNode.status == BSStats.EXPAND && curNode.getChildCount() > 0) {
					curNode.curChildIndex = 0;
					curNode = curNode.childNodes.get(0);
					++update;
					continue BS_LOOP;
				}

			}

			return false;

		}

	}

	class XRCacheWorker implements IRCacheWorker {

		private int cacheCacheStmtCount = 0;

		private int cacheLastEntryId = 0;

		private int loadCount = 0;

		private IRStmtLoader loader;

		private IRReteNode node;

		private int saveCount = 0;

		private IRStmtSaver saver;

		private CacheStatus status = CacheStatus.UNLOAD;

		private int writeLines = 0;

		public XRCacheWorker(IRReteNode node) {
			super();
			this.node = node;
		}

		@Override
		public void cleanCache() throws RException {

			if (this.isDirty()) {
				throw new RException("Can't clean dirty cache:" + this.getNode());
			}

			this.cacheLastEntryId = 0;
			this.cacheCacheStmtCount = 0;
			this.status = CacheStatus.CLEAN;
		}

		@Override
		public int getCacheLastEntryId() {
			return cacheLastEntryId;
		}

		@Override
		public int getLoadCount() {
			return loadCount;
		}

		public IRStmtLoader getLoader() {
			return loader;
		}

		@Override
		public IRReteNode getNode() {
			return node;
		}

		@Override
		public int getReadCount() {
			return loader.getReadLines();
		}

		@Override
		public int getSaveCount() {
			return saveCount;
		}

		public IRStmtSaver getSaver() {
			return saver;
		}

		@Override
		public CacheStatus getStatus() {
			return status;
		}

		@Override
		public int getStmtCount() {
			return cacheCacheStmtCount;
		}

		@Override
		public int getWriteCount() {
			return writeLines;
		}

		public boolean isDirty() throws RException {

			if (saver == null) {
				return false;
			}

			switch (status) {
			case LOADED:

				if (saver.needSave()) {
					return true;
				}

				if (node.getEntryQueue().size() != cacheCacheStmtCount) {
					return true;
				}

				IRReteEntry lastEntry = ReteUtil.getLastEntry(node.getEntryQueue());
				int lastEntryId = lastEntry == null ? -1 : lastEntry.getEntryId();
				if (cacheLastEntryId != lastEntryId) {
					return true;
				}

				return false;

			case LOADING:
				return true;

			case UNLOAD:
			case CLEAN:
				return node.getEntryQueue().size() > 0;

			default:
				throw new RException("invalid status: " + status);
			}

		}

		public int load() throws RException {

			if (loader == null) {
				return 0;
			}

			if (status == CacheStatus.LOADING) {
				throw new RException("invalid status: " + status);
			}

			this.status = CacheStatus.LOADING;

			_fireLoadNodeAction(node);

			IREntryQueue entryQueue = node.getEntryQueue();

			boolean pushEmptyNode = (entryQueue.size() == 0);
			int oldCacheStmtCount = this.cacheCacheStmtCount;

			try {

				loader.load((stmt) -> {

					if (!ReteUtil.isValidNodeStmt(node, stmt)) {
						throw new RException(String.format("Invalid stmt for node<%s>: %s", "" + node, "" + stmt));
					}

					if (RUpdateResult.isValidUpdate(_addStmt(node, stmt, DEFINE))) {
						XRModel.this.cacheUpdateCount++;
						this.cacheCacheStmtCount++;
					}
				});

			} catch (IOException e) {

				if (RuleUtil.isModelTrace()) {
					e.printStackTrace();
				}

				throw new RException(e.toString());
			}

			if (pushEmptyNode && (oldCacheStmtCount != this.cacheCacheStmtCount)) {
				cacheLastEntryId = entryQueue.getEntryAt(entryQueue.size() - 1).getEntryId();
			} else {
				cacheLastEntryId = -1;
			}

			this.loadCount++;
			this.status = CacheStatus.LOADED;

			return cacheCacheStmtCount;
		}

		public int save() throws RException, IOException {

			if (status == CacheStatus.LOADING) {
				throw new RException("invalid status: " + status);
			}

			IREntryQueue queue = node.getEntryQueue();
			List<IRReteEntry> entries = ReteUtil.getAllEntries(queue);
			IRReteEntry lastEntry = ReteUtil.getLastEntry(node.getEntryQueue());
			int lastEntryId = lastEntry == null ? -1 : lastEntry.getEntryId();

			_fireSaveNodeAction(node);

			int saveLineCount = saver.save(entries);
			this.cacheLastEntryId = lastEntryId;
			this.cacheCacheStmtCount = queue.size();
			this.saveCount++;
			this.writeLines += saveLineCount;
			this.status = CacheStatus.LOADED;

			return saveLineCount;
		}

		public void setLoader(IRStmtLoader loader) {
			this.loader = loader;
		}

		public void setSaver(IRStmtSaver saver) {
			this.saver = saver;
		}

	}

	class XRModelTransaction implements IRTransaction {

		public XRModelTransaction(XRModel model) {

		}

		@Override
		public void commit() throws RException {

		}

		@Override
		public void rollback() throws RException {
		}

	}

	static final String CK_ADD_CONSTRAINT = "model-addConstraint";

	static final String CK_ADD_LOAD_NODE_LIS = "model-addLoadNodeListener";

	static final String CK_ADD_RULE = "model-addRule";

	static final String CK_ADD_RULE_EXEC_LIS = "model-addRuleExecutedListener";

	static final String CK_ADD_RULE_FAIL_LIS = "model-addRuleFailedListener";

	static final String CK_ADD_SAVE_NODE_LIS = "model-addSaveNodeListener";

	static final String CK_ADD_STMT = "model-addStatement";

	static final String CK_ADD_STMT_LIS = "model-addStatementListener";

	static final String CK_ADD_UPDATE_NODE = "model-addUpdateNode";

	static final String CK_ASSUME_STMT = "model-assumeStatement";

	static final String CK_BACK_SEARCH = "model-backSearch";

	static final String CK_DO_GC = "model-doGC";

	static final String CK_EXEC = "model-execute";

	static final String CK_FIX_STMT = "model-fixStatement";

	static final String CK_GC_COUNT = "model-gc-count";

	static final String CK_GC_TRIGGER = "model-gc-trigger";

	static final String CK_GET_VAR = "model-getVar";

	static final String CK_HALT = "model-halt";

	static final String CK_HAS_STMT_1 = "model-hasStatement-1";

	static final String CK_HAS_STMT_2 = "model-hasStatement-2";

	static final String CK_HAS_STMT_CACHE = "model-has-stmt-cache";

	static final String CK_HAS_STMT_HIT = "model-has-stmt-hit";

	static final String CK_LIST_STMT = "model-listStatements";

	static final String CK_QUERY = "model-query";

	static final String CK_RMV_CONSTRAINT = "model-removeConstraint";

	static final String CK_RMV_STMT = "model-removeStatement";

	static final String CK_SAVE = "model-save";

	static final String CK_SET_CACHE_PATH = "model-setModelCachePath";

	static final String CK_SET_NODE_LOADER = "model-setNodeLoader";

	static final String CK_SET_NODE_SAVER = "model-setNodeSaver";

	static final String CK_START = "model-start";

	static final String CK_TRY_ADD_STMT = "model-tryAddStatement";

	static List<IRReteNode> EMPTY_NODES = Collections.emptyList();

	static final String MODEL_CACHE_SUFFIX = ".mc";

	static final RRunState MODEL_SSTATE[][] = {

			// Completed(0), Runnable(1), Running(2), Halting(3), Failed(4), Partial(5)
			{ Completed, Completed, Completed, Halting, Failed, Completed }, // Completed
			{ Runnable, Runnable, Running, Halting, Failed, Runnable }, // Runnable
			{ Running, Running, Running, Halting, Failed, Running }, // Running
			{ Halting, Halting, Halting, Halting, Halting, Halting }, // Halting
			{ Failed, Failed, Failed, Failed, Failed, Failed }, // Failed
			{ Partial, Partial, Partial, Halting, Failed, Partial }, // Partial
	};

	static List<String> modelCountKeyList = new ArrayList<>();

	protected static int nodeExecId = 0;

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
		modelCountKeyList.add(CK_HAS_STMT_1);
		modelCountKeyList.add(CK_HAS_STMT_2);
		modelCountKeyList.add(CK_LIST_STMT);
		modelCountKeyList.add(CK_QUERY);
		modelCountKeyList.add(CK_RMV_CONSTRAINT);
		modelCountKeyList.add(CK_RMV_STMT);
		modelCountKeyList.add(CK_SAVE);
		modelCountKeyList.add(CK_SET_CACHE_PATH);
		modelCountKeyList.add(CK_SET_NODE_LOADER);
		modelCountKeyList.add(CK_SET_NODE_SAVER);
		modelCountKeyList.add(CK_START);
		modelCountKeyList.add(CK_TRY_ADD_STMT);
		modelCountKeyList.add(CK_BACK_SEARCH);

		modelCountKeyList = Collections.unmodifiableList(modelCountKeyList);
	}

	protected List<IRReteNode> activeQueue = new LinkedList<>();

	protected int activeUpdate = 0;

	protected int assuemeStatmentLevel = 0;

	protected boolean cacheEnable = false;

	protected int cacheUpdateCount = 0;

	protected final LinkedList<XRCacheWorker> cacheWorkerList = new LinkedList<>();

	protected final ModelConstraintUtil constraintUtil;

	protected final IREntryTable entryTable = new XREntryTable();

	protected long gcCapacity = DEF_GC_CAPACITY;

	protected long gcCount = 0;

	protected long gcInterval = -1;

	protected long gcLastGcTime = -1;

	protected long gcTrigger = 0;

	protected Map<String, IRReteEntry> hasEntryCacheMap = new HashMap<>();

	protected int hasStmtHitCount = 0;

	protected IRInterpreter interpreter;

	protected XRRListener1Adapter<IRReteNode> loadNodeListener = null;

	protected long mcAddConstraint = 0;

	protected long mcAddLoadNodeListener = 0;

	protected long mcAddRule = 0;

	protected long mcAddRuleExecutedListener = 0;

	protected long mcAddRuleFailedListener = 0;

	protected long mcAddSaveNodeListener = 0;

	protected long mcAddStatement = 0;

	protected long mcAddStatementListener = 0;

	protected long mcAddUpdateNode = 0;

	protected long mcAssumeStatement = 0;

	protected long mcBackSearch = 0;

	protected long mcDoGC = 0;

	protected long mcExecute = 0;

	protected long mcFixStatement = 0;

	protected long mcGetVar = 0;

	protected long mcHalt = 0;

	protected long mcHasStatement1 = 0;

	protected long mcHasStatement2 = 0;

	protected long mcListStatements = 0;

	protected long mcQuery = 0;

	protected long mcRemoveConstraint = 0;

	protected long mcRemoveStatement = 0;

	protected long mcSave = 0;

	protected long mcSetModelCachePath = 0;

	protected long mcSetNodeLoader = 0;

	protected long mcSetNodeSaver = 0;

	protected long mcStart = 0;

	protected long mcTryAddStatement = 0;

	protected String modelCachePath = null;

	protected final XRRModelCounter modelCounter;

	protected IRFrame modelFrame;

	protected int modelPriority = RETE_PRIORITY_DEFAULT;

	protected final XRRListener1Adapter<IRRule> modelRuleExecutedListenerDispatcher = new XRRListener1Adapter<>();

	protected final XRRListener1Adapter<IRRule> modelRuleFailedListenerDispatcher = new XRRListener1Adapter<>();

	protected RRunState modelRunState = RRunState.Completed; // default

	protected IRVar modelStatsVar;

	protected boolean needRestart = false;

	protected RNodeContext nodeContext = null;

	protected final IRNodeGraph nodeGraph;

	protected int processingLevel = 0;

	protected final LinkedList<IRReteNode> restartingNodeList = new LinkedList<>();

	protected XRRListener1Adapter<IRReteNode> saveNodeListener = null;

	protected final XRStmtListenUpdater stmtListenUpdater = new XRStmtListenUpdater();

	protected int stmtMaxIndex = 0;

	protected IRTransaction transaction = null;

	protected int tryAddConstraintLevel = 0;

	protected int tryRemoveConstraintLevel = 0;

	protected final XRUpdateQueue updateQueue = new XRUpdateQueue();

	public XRModel(String modelName, IRClass rclass, IRFrame frame) throws RException {
		super(rclass, modelName, frame);
		this.nodeGraph = new XRNodeGraph(this, entryTable);
		this.modelCounter = new XRRModelCounter(this);
		this.constraintUtil = new ModelConstraintUtil(this);
	}

	protected void _addCacheStatement(IRReteEntry entry) throws RException {
		hasEntryCacheMap.put(ReteUtil.uniqName(entry), entry);
	}

	protected RUpdateResult _addReteEntry(IRList stmt, RReteStatus toStatus) throws RException {

		if (!ReteUtil.isReteStmtNoVar(stmt)) {
			throw new RException("not support stmt: " + stmt);
		}

		IRReteNode rootNode = _findRootNode(stmt.getNamedName(), stmt.size());
		_checkCache(rootNode);

		int oldSize = 0;
		if (this.nodeContext != null) {
			this.nodeContext.tryAddStmt++;
			oldSize = rootNode.getEntryQueue().size();
		}

		RUpdateResult rst = _addStmt(rootNode, stmt, toStatus);

		// new stmt added
		if (rst == RUpdateResult.NEW && this.nodeContext != null) {
			int newSize = rootNode.getEntryQueue().size();
			if (newSize > oldSize) {
				this.nodeContext.actualAddStmt++;
			}
		}

		// there is any update
		if (RUpdateResult.isValidUpdate(rst)) {
			cacheUpdateCount++;
			addUpdateNode(rootNode);
		}

		return rst;
	}

	protected RUpdateResult _addStmt(IRReteNode rootNode, IRList stmt, RReteStatus newStatus) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out
					.println(String.format("%s: addEntry(%s, %s)", rootNode.getNodeName(), "" + stmt, "" + newStatus));
		}

		IREntryQueue entryQueue = rootNode.getEntryQueue();

		IRReteEntry oldEntry = null;
		if (entryQueue.getQueueType() == REntryQueueType.UNIQ) {
			oldEntry = ReteUtil.getStmt(rootNode, stmt);
		}

		/*******************************************************/
		// Insert entry
		// - Entry not exist
		// - or marked as "drop" (removed by entry table automatically)
		/*******************************************************/
		if (ReteUtil.isRemovedEntry(oldEntry)) {

			int stmtLen = stmt.size();

			IRObject[] newElements = new IRObject[stmtLen];
			for (int i = 0; i < stmtLen; ++i) {

				IRObject obj = stmt.get(i);
				if (obj == null) {
					obj = O_Nil;
				}

				newElements[i] = obj;
			}

			IRReteEntry newEntry = entryTable.createEntry(stmt.getNamedName(), newElements, newStatus, true);
			rootNode.incEntryCreateCount();

			if (!rootNode.addReteEntry(newEntry)) {
				entryTable.deleteEntry(newEntry);
				rootNode.incEntryDeleteCount();
				IRConstraint1 failedConstraint1 = rootNode.getLastFailedConstraint1();
				if (failedConstraint1 != null) {
					if (nodeContext == null || this.assuemeStatmentLevel > 0) {
						throw new RConstraintConflict(String.format("Unable to add entry<%s> due to constraint<%s>",
								newEntry, failedConstraint1), rootNode, newEntry, failedConstraint1);
					}
				}

				return RUpdateResult.INVALID;
			}

			/*******************************************************/
			// Add reference
			/*******************************************************/
			if (nodeContext != null) {
				entryTable.addReference(newEntry, nodeContext.currentNode, nodeContext.currentEntry);
			} else {
				entryTable.addReference(newEntry, rootNode);
			}

			return RUpdateResult.NEW;
		}

		/*******************************************************/
		// Entry needs be updated
		/*******************************************************/
		RReteStatus oldStatus = oldEntry.getStatus();
		RReteStatus finalStatus = ReteUtil.getReteStatus(oldStatus, newStatus);
		if (finalStatus == null) {
			throw new RException(String.format("Invalid status convert: from=%s, to=%s", oldStatus, newStatus));
		}

		// status not changed
		if (finalStatus == oldStatus) {

			entryQueue.incEntryRedundant();

			/*******************************************************/
			// Add reference
			/*******************************************************/
			if (finalStatus != REMOVE && finalStatus != TEMP__ && nodeContext != null) {
				entryTable.addReference(oldEntry, nodeContext.currentNode, nodeContext.currentEntry);
			}

			return RUpdateResult.NOCHANGE;
		}

		switch (finalStatus) {
		case ASSUME:
		case DEFINE:
		case REASON:
		case FIXED_:
			entryTable.setEntryStatus(oldEntry, finalStatus);
			break;

		case TEMP__:
			entryTable.setEntryStatus(oldEntry, finalStatus);
			break;

		case REMOVE:
			entryTable.deleteEntryReference(oldEntry, rootNode);
			break;

		default:
			throw new RException("Unknown status: " + finalStatus);
		}

		// Add this in this branch
		entryQueue.incNodeUpdateCount();

		/*******************************************************/
		// Add reference
		/*******************************************************/
		if (finalStatus != REMOVE && finalStatus != TEMP__ && nodeContext != null) {
			entryTable.addReference(oldEntry, nodeContext.currentNode, nodeContext.currentEntry);
		}

		return RUpdateResult.CHANGE;
	}

	protected void _checkActiveNode() throws RException {

		if (activeUpdate > 0) {

			Iterator<IRReteNode> it = activeQueue.iterator();
			while (it.hasNext()) {
				IRReteNode node = it.next();
				if (node.getPriority() >= modelPriority) {
					updateQueue.push(node);
					it.remove();
				}
			}

			activeUpdate = 0;
		}
	}

	protected void _checkCache(IRReteNode node) throws RException {

		if (!cacheEnable || node.getReteType() != RReteType.NAME0) {
			return;
		}

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {
			return;
		}

		if (cache.getStatus() == CacheStatus.LOADING || cache.getStatus() == CacheStatus.LOADED) {
			return;
		}

		int loadCount = cache.load();
		if (loadCount > 0) {
			addUpdateNode(node);
		}
	}

	protected void _checkConstraintConflict(IRReteNode rootNode) throws RException {

		/******************************************************/
		// Build subgraph
		/******************************************************/
		IRNodeSubGraph subGraph = this.nodeGraph.buildConstraintCheckSubGraph(rootNode);
		if (subGraph.isEmpty()) {
			return;
		}

		/********************************************/
		// Activate sub group
		/********************************************/
		subGraph.activate(this.getPriority());

		this.processingLevel++;

		try {
			_run(-1);
		} finally {
			this.processingLevel--;
			this._setRunState(RRunState.Partial);
			subGraph.rollback();
		}
	}

	@Override
	protected void _delete() throws RException {

		for (IRReteNode node : this.getNodeGraph().getNodeMatrix().getAllNodes()) {
			node.delete(interpreter, this.modelFrame);
		}

		if (this.modelFrame != null) {
			RulpUtil.decRef(this.modelFrame);
			this.modelFrame = null;
		}

		super._delete();
	}

	protected IRReteEntry _findIndexStatement(IRList stmt, List<OrderEntry> orderList) throws RException {

		List<IRObject> newStmtArr = RulpUtil.toArray(stmt);
		XTempVarBuilder varBuilder = new XTempVarBuilder("index");

		for (OrderEntry order : orderList) {

			int index = order.index;
			IRObject oldObj = newStmtArr.get(index);

			if (RulpUtil.isVarAtom(oldObj)) {
				throw new RException("invalid index index: " + index + ", stmt=" + stmt);
			}

			newStmtArr.set(index, varBuilder.next());
		}

		IRList newStmt = RulpUtil.toList(stmt.getNamedName(), newStmtArr);

		IRNodeGraph graph = getNodeGraph();
		IRReteNode indexNode = graph.buildIndex(graph.getNodeByTree(newStmt), orderList);

		// '(?a b ?c) ==> (a b ?c)
		XREntryQueueOrder orderQueue = (XREntryQueueOrder) indexNode.getEntryQueue();
		IRReteEntry entry = orderQueue.find(stmt);
		if (entry == null) {

			XCount xcount = new XCount();

			RuleUtil.travelReteParentNodeByPostorder(indexNode, (node) -> {
				if (execute(node) > 0) {
					xcount.count++;
				}
				return false;
			});

			if (xcount.count > 0) {
				entry = orderQueue.find(stmt);
			}
		}

		return entry;
	}

	protected IRReteEntry _findRootEntry(IRList filter, int statusMask) throws RException {

		IRReteNode rootNode = _findRootNode(filter.getNamedName(), filter.size());
		_checkCache(rootNode);

		IRReteEntry oldEntry = ReteUtil.getStmt(rootNode, filter);
		if (oldEntry == null || !ReteUtil.matchReteStatus(oldEntry, statusMask)) {
			return null;
		}

		return oldEntry;
	}

	protected IRReteNode _findRootNode(String namedName, int stmtLen) throws RException {
		return nodeGraph.getRootNode(namedName, stmtLen);
	}

	protected void _fireLoadNodeAction(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> loadNode: " + ", node=" + node);
		}

		if (loadNodeListener == null) {
			return;
		}

		loadNodeListener.doAction(node);
	}

	protected void _fireSaveNodeAction(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> saveNode: " + ", node=" + node);
		}

		if (saveNodeListener == null) {
			return;
		}

		saveNodeListener.doAction(node);
	}

	protected int _gc(boolean force) throws RException {

		/*****************************************************/
		// the function must be called at first level
		/*****************************************************/
		if (processingLevel != 0) {
			return 0;
		}

		/*****************************************************/
		// Never do GC
		/*****************************************************/
		if (!force && (gcInterval < 0 || gcCapacity < 0)) {
			return 0;
		}

		int totalGcCount = 0;
		int smtGcCount = 0;
		int alpahGcCount = 0;
		int betaGcCount = 0;
		int exprGcCount = 0;
		int ruleGcCount = 0;

		for (IRReteNode node : ReteUtil.getAllNodes(this.getNodeGraph())) {

			int gcCount = node.doGC();
			totalGcCount += gcCount;

			switch (node.getReteType()) {
			case ALPH0:
			case ALPH1:
				alpahGcCount += gcCount;
				break;

			case BETA0:
			case BETA1:
			case BETA2:
			case BETA3:
				betaGcCount += gcCount;
				break;

			case ROOT0:
				smtGcCount += gcCount;
				break;

			case RULE:
				ruleGcCount += gcCount;
				break;

			case EXPR0:
			case EXPR1:
			case EXPR2:
			case EXPR3:
				exprGcCount += gcCount;
				break;

			case VAR:
			case CONST:
				break;

			default:
				break;
			}
		}

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("==> GC: rs=%d, ra=%d, rb=%d, re=%d, rr=%d", smtGcCount, alpahGcCount,
					betaGcCount, exprGcCount, ruleGcCount));
		}

		this.gcCount++;
		this.nodeGraph.gc();
		this.entryTable.doGC();
		this.hasEntryCacheMap.clear();

		long curTime = System.currentTimeMillis();
		if (curTime < (gcLastGcTime + gcInterval)) {
			return totalGcCount;
		}

		gcTrigger++;

		try {

			long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
			if (used >= gcCapacity) {
				System.gc();
			}

			return totalGcCount;

		} finally {

			gcLastGcTime = curTime;
		}

	}

	protected XRCacheWorker _getCacheWorker(IRReteNode node) throws RException {

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {

			if (modelCachePath == null) {
				return null;
			}

			XRStmtFileDefaultCacher cacher = new XRStmtFileDefaultCacher(modelCachePath, node);
			cache = _setNodeCache(node, cacher, cacher);
		}

		return cache;
	}

	protected RReteStatus _getNewStmtStatus() throws RException {

		if (nodeContext == null) {
			return DEFINE;
		}

		return ReteUtil.getChildStatus(nodeContext.currentEntry);
	}

	protected boolean _hasAnyStatement() throws RException {

		for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
			_checkCache(rootNode);
			IREntryCounter rootEntryCounter = rootNode.getEntryQueue().getEntryCounter();
			int totalCount = rootEntryCounter.getEntryTotalCount();
			int nullCount = rootEntryCounter.getEntryNullCount();
			int dropCount = rootEntryCounter.getEntryCount(REMOVE);
			if (totalCount > nullCount - dropCount) {
				return true;
			}
		}

		return false;
	}

	protected boolean _hasCacheStatement(IRList filter) throws RException {

		String uniqName = ReteUtil.uniqName(filter);
		IRReteEntry cacheEntry = hasEntryCacheMap.get(uniqName);
		if (cacheEntry != null) {
			if (cacheEntry.isDroped()) {
				hasEntryCacheMap.remove(uniqName);
			} else {
				return true;
			}
		}

		return false;
	}

	protected boolean _hasUpdateNode() throws RException {
		_checkActiveNode();
		return updateQueue.hasNext();
	}

	protected boolean _isInClosingPhase() {
		return this.processingLevel > 0 && getRunState() == RRunState.Completed;
	}

	protected int _listAllStatements(int mask, int limit, boolean reverse, IREntryIteratorBuilder builder,
			IREntryAction action) throws RException {

		int size = 0;

		List<IRReteNode> nodes = RuleUtil.listNodes(nodeGraph, RReteType.ROOT0, RReteType.NAME0);
		if (reverse) {
			Collections.reverse(nodes);
		}

		for (IRReteNode rootNode : nodes) {

			_checkCache(rootNode);

			Iterator<IRReteEntry> it = builder.makeIterator(rootNode.getEntryQueue());

			while (it.hasNext()) {

				IRReteEntry entry = it.next();
				if (entry == null) {
					continue;
				}

				if (mask == 0) {
					if (entry.isDroped()) {
						continue;
					}
				} else {
					if (!ReteUtil.matchReteStatus(entry.getStatus(), mask)) {
						continue;
					}
				}

				if (!action.addEntry(entry)) {
					continue;
				}

				size++;
				if (limit > 0 && size >= limit) {
					return size;
				}
			}
		}

		return size;
	}

	protected int _listStatements(IRList filter, int statusMask, int limit, boolean reverse,
			IREntryIteratorBuilder builder, IREntryAction action) throws RException {

		if (filter == null) {
			return _listAllStatements(statusMask, limit, reverse, builder, action);
		}

		String namedName = filter.getNamedName();
		filter = _rebuild(filter);
		int size = 0;

		/******************************************************/
		// '(a ? ?)
		/******************************************************/
		{
			ArrayList<IRObject> filterObjs = null;
			XTempVarBuilder tmpVarBuilder = null;

			IRIterator<? extends IRObject> iter = filter.iterator();
			for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

				IRObject obj = iter.next();

				if (RulpUtil.isAnyVar(obj)) {

					if (filterObjs == null) {
						filterObjs = new ArrayList<>();
						for (int i = 0; i < stmtIndex; ++i) {
							filterObjs.add(filter.get(i));
						}
					}

					if (tmpVarBuilder == null) {
						tmpVarBuilder = new XTempVarBuilder("?_ag_");
					}

					filterObjs.add(tmpVarBuilder.next());

				} else {

					if (filterObjs != null) {
						filterObjs.add(obj);
					}
				}
			}

			if (filterObjs != null) {
				filter = RulpUtil.toList(namedName, filterObjs);
			}
		}

		/******************************************************/
		// '(?...) or '(?x ?...)
		// n:'(?...) or n:'(?x ?...)
		/******************************************************/
		int anyIndex = ReteUtil.indexOfVarArgStmt(filter);
		if (anyIndex != -1) {

			if (anyIndex != (filter.size() - 1)) {
				throw new RException(String.format("invalid filter: %s", filter));
			}

			ArrayList<IRObject> extendFilterObjs = new ArrayList<>();
			XTempVarBuilder tmpVarBuilder = new XTempVarBuilder("?_vg_");

			for (int i = 0; i < anyIndex; ++i) {
				extendFilterObjs.add(filter.get(i));
			}

			/******************************************************/
			// '(?...)
			/******************************************************/
			if (namedName == null) {

				int subLimit = limit;

				if (!reverse) {

					FIND_SUB: while (extendFilterObjs.size() <= nodeGraph.getMaxRootStmtLen()) {

						if (nodeGraph.findRootNode(null, extendFilterObjs.size()) != null) {

							IRList subFilter = RulpFactory.createNamedList(namedName, extendFilterObjs);
							int subSize = _listStatements(subFilter, statusMask, subLimit, reverse, builder, action);
							size += subSize;

							if (limit > 0) {
								subLimit = subLimit - subSize;
								if (subLimit <= 0) {
									break FIND_SUB;
								}
							}

						}

						extendFilterObjs.add(tmpVarBuilder.next());
					}

				} else {

					ArrayList<ArrayList<IRObject>> filterList = new ArrayList<>();
					while (extendFilterObjs.size() <= nodeGraph.getMaxRootStmtLen()) {
						if (nodeGraph.findRootNode(null, extendFilterObjs.size()) != null) {
							filterList.add(new ArrayList<>(extendFilterObjs));
						}
						extendFilterObjs.add(tmpVarBuilder.next());
					}

					Collections.reverse(filterList);

					FIND_SUB: for (ArrayList<IRObject> extendFilter : filterList) {

						IRList subFilter = RulpFactory.createNamedList(namedName, extendFilter);

						int subSize = _listStatements(subFilter, statusMask, subLimit, reverse, builder, action);
						size += subSize;

						if (limit > 0) {
							subLimit = subLimit - subSize;
							if (subLimit <= 0) {
								break FIND_SUB;
							}
						}
					}

				}

				return size;
			}

			IRReteNode namedNode = nodeGraph.findRootNode(namedName, -1);
			if (namedNode == null || extendFilterObjs.size() > namedNode.getEntryLength()) {
				return 0;
			}

			for (int i = anyIndex; i < namedNode.getEntryLength(); ++i) {
				extendFilterObjs.add(tmpVarBuilder.next());
			}

			filter = RulpFactory.createNamedList(namedName, extendFilterObjs);
		}

		/******************************************************/
		// Check named node
		/******************************************************/
		if (namedName != null) {
			IRReteNode namedNode = nodeGraph.findRootNode(namedName, -1);
			if (namedNode == null || namedNode.getEntryLength() != filter.size()) {
				return 0;
			}
		}

		/******************************************************/
		// Query uniq stmt
		/******************************************************/
		if (ReteUtil.getStmtVarCount(filter) == 0) {

			IRReteEntry oldEntry = _findRootEntry(filter, statusMask);
			if (oldEntry == null) {
				return 0;
			}

			if (!action.addEntry(oldEntry)) {
				return 0;
			}

			size++;

			return size;
		}

		IRReteNode matchedNode = nodeGraph.getNodeByTree(filter);
		if (!RReteType.isRootType(matchedNode.getReteType()) && matchedNode.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid list node: " + matchedNode);
		}

		_checkCache(matchedNode);

		/******************************************************/
		// Force update node chain if query reverse
		/******************************************************/
		if (reverse) {

			RuleUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node);
				if (update > 0) {
					modelCounter.incQueryMatchCount();
				}

				return false;
			});
		}

		/******************************************************/
		// Update all nodes if need rebuild order
		/******************************************************/
		boolean nodeUpdated = false;

		if (builder.rebuildOrder()) {

			RuleUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node);
				if (update > 0) {
					modelCounter.incQueryMatchCount();
				}

				return false;
			});

			nodeUpdated = true;
		}

		IREntryQueue matchedNodeQueue = matchedNode.getEntryQueue();
		Iterator<IRReteEntry> it = builder.makeIterator(matchedNodeQueue);

		boolean completed = false;

		/******************************************************/
		// Check cached entry
		/******************************************************/
		while (it.hasNext()) {

			IRReteEntry entry = it.next();
			if (entry == null || !ReteUtil.matchReteStatus(entry, statusMask)) {
				continue;
			}

			if (!action.addEntry(entry)) {
				continue;
			}

			size++;
			if (limit > 0 && size >= limit) {
				completed = true;
				break;
			}
		}

		/******************************************************/
		// Update all current node chain
		/******************************************************/
		if (!completed && !reverse && !nodeUpdated) {

			RuleUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node);
				if (update > 0) {
					modelCounter.incQueryMatchCount();
				}

				return false;
			});

			while (it.hasNext()) {

				IRReteEntry entry = it.next();

				if (entry == null || !ReteUtil.matchReteStatus(entry, statusMask)) {
					continue;
				}

				if (!action.addEntry(entry)) {
					continue;
				}
				size++;

				if (limit > 0 && size >= limit) {
					break;
				}
			}
		}

		return size;
	}

	protected IRReteNode _nextUpdateNode() throws RException {
		_checkActiveNode();
		return updateQueue.pop();
	}

	protected void _queryCond(IRResultQueue objQueue, IRReteNode queryNode, final int limit) throws RException {

		int queryEntryIndex = 0;

		/******************************************************/
		// Update cache
		/******************************************************/
		if (isCacheEnable()) {
			for (SourceNode sn : RuleUtil.listSource(this, queryNode)) {
				_checkCache(sn.rule);
			}
			_checkCache(queryNode);
		}

		/******************************************************/
		// Load results
		/******************************************************/
		{
			IREntryQueue queryNodeQueue = queryNode.getEntryQueue();

			while (queryEntryIndex < queryNodeQueue.size()) {

				IRReteEntry entry = queryNodeQueue.getEntryAt(queryEntryIndex++);
				if (!objQueue.addEntry(entry)) {
					continue;
				}

				// limit <= 0 means query all
				if (limit > 0 && objQueue.size() >= limit) {
					return;
				}
			}
		}

		/******************************************************/
		// Build subgraph
		/******************************************************/
		IRNodeSubGraph subGraph = this.nodeGraph.buildSourceSubGraph(queryNode);

		/******************************************************/
		// Invoke running
		/******************************************************/
		this.processingLevel++;
		this.activeUpdate++;
		this._setRunState(RRunState.Running);

		/********************************************/
		// Activate sub group
		/********************************************/
		subGraph.activate(this.getPriority());

		try {

			while (_hasUpdateNode()) {

				IRReteNode node = _nextUpdateNode();
				if (node != null) {

					switch (node.getRunState()) {
					case Completed:
					case Runnable:
					case Running:

						int update = execute(node);
						if (node == queryNode && update > 0) {

							IREntryQueue queue = queryNode.getEntryQueue();

							while (queryEntryIndex < queue.size()) {

								IRReteEntry entry = queue.getEntryAt(queryEntryIndex++);
								if (!objQueue.addEntry(entry)) {
									continue;
								}

								// limit <= 0 means query all
								if (limit > 0 && objQueue.size() >= limit) {
									return;
								}
							}
						}

						break;

					// don't process 'Failed' or 'Halting' node
					case Failed:
					case Halting:
						continue;

					default:
						throw new RException("unknown state: " + node.getRunState());
					}
				}

			}

			return;

		} finally {

			this.processingLevel--;
			this._setRunState(RRunState.Partial);
			subGraph.rollback();
		}
	}

	protected IRList _rebuild(IRList list) throws RException {

		/******************************************************/
		// '(a ? ?)
		// update constant
		/******************************************************/
		{
			ArrayList<IRObject> filterObjs = null;

			IRIterator<? extends IRObject> iter = list.iterator();
			for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

				IRObject obj = iter.next();
				boolean update = false;

				if (obj.getType() == RType.LIST) {

					IRList newObj = _rebuild((IRList) obj);
					if (newObj != obj) {
						obj = newObj;
						update = true;
					}

				} else {

					if (!update) {
						IRObject newObj = RulpUtil.lookup(obj, getInterpreter(), this.getFrame());
						if (newObj.getType() == RType.CONSTANT) {
							obj = RulpUtil.asConstant(newObj).getValue();
							update = true;
						}
					}
				}

				if (update) {

					if (filterObjs == null) {
						filterObjs = new ArrayList<>();
						for (int i = 0; i < stmtIndex; ++i) {
							filterObjs.add(list.get(i));
						}
					}

					filterObjs.add(obj);

				} else {
					if (filterObjs != null) {
						filterObjs.add(obj);
					}
				}
			}

			if (filterObjs != null) {
				list = RulpUtil.toList(list.getNamedName(), filterObjs);
			}
		}

		return list;
	}

	protected int _run(int maxStep) throws RException {

		this.activeUpdate++;

		int runTimes = 0;
		int execTimes = 0;

		RUN: for (; runTimes == 0 || _hasUpdateNode() || this.needRestart; ++runTimes) {

			RRunState state = this.getRunState();

			switch (state) {
			// Something wrong
			case Failed:
			case Halting:
				break RUN;

			case Completed: // closing status
			case Running: // working status
				break;

			case Runnable:
			case Partial:
				_setRunState(RRunState.Running);
				break;

			default:
				throw new RException("unknown state: " + state);
			}

			// no more working nodes & need restart
			if (!updateQueue.hasNext() && this.needRestart) {

				for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
					updateQueue.push(rootNode);
				}

				for (IRReteNode waitingNode : restartingNodeList) {
					updateQueue.push(waitingNode);
				}

				restartingNodeList.clear();
				this.needRestart = false;
				_setRunState(RRunState.Running);
				continue;
			}

			// need exit with runnable status
			if (maxStep > 0 && runTimes >= maxStep) {
				_setRunState(RRunState.Runnable, true);
				break RUN;
			}

			IRReteNode node = _nextUpdateNode();
			if (node != null) {

				switch (node.getRunState()) {
				case Completed:
				case Runnable:
				case Running:
					++execTimes;
					execute(node);
					break;

				// don't process 'Failed' or 'Halting' node
				case Failed:
				case Halting:
					continue;

				default:
					throw new RException("unknown state: " + node.getRunState());
				}
			}

			if (!updateQueue.hasNext()) {
				_setRunState(RRunState.Completed);
			}

		} // while

		return execTimes;

	}

	protected XRCacheWorker _setNodeCache(IRReteNode node, IRStmtLoader loader, IRStmtSaver saver) throws RException {

		if (loader == null && saver == null) {
			throw new RException("null loader and saver");
		}

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {
			cache = new XRCacheWorker(node);
			node.setCacheWorker(cache);
			cacheWorkerList.add(cache);
		}

		if (loader != null) {
			cache.setLoader(loader);
		}

		if (saver != null) {
			cache.setSaver(saver);
		}

		this.cacheEnable = true;
		return cache;
	}

	protected void _setRunState(RRunState state) throws RException {
		_setRunState(state, false);
	}

	protected void _setRunState(RRunState state, boolean force) throws RException {

		RRunState newState;

		if (force) {
			newState = state;
		} else {
			newState = MODEL_SSTATE[state.getIndex()][this.modelRunState.getIndex()];
		}

		if (modelRunState != newState) {

			if (RuleUtil.isModelTrace()) {
				System.out.println(String.format("Model State: %s ==> %s", modelRunState, newState));
			}

			this.modelRunState = newState;
			modelCounter.incStateChangeCount();

			if (modelStatsVar != null) {
				modelStatsVar.setValue(RRunState.toObject(newState));
			}
		}
	}

	protected int _update(IRReteNode updateNode) throws RException {

		IRFrame queryFrame = RulpFactory.createFrame(modelFrame, "UPDATE");
		RuleUtil.setDefaultModel(queryFrame, this);

		AtomicInteger count = new AtomicInteger();

		/******************************************************/
		// update loaders
		/******************************************************/
		RuleUtil.travelReteParentNodeByPostorder(updateNode, (node) -> {
			for (IRReteNode loader : nodeGraph.getBindFromNodes(node)) {
				if (loader.getPriority() >= 0) {
					int c = execute(loader);
					if (c > 0) {
						count.addAndGet(c);
					}
				}
			}
			return false;
		});

		/******************************************************/
		// update node
		/******************************************************/
		RuleUtil.travelReteParentNodeByPostorder(updateNode, (node) -> {
			int c = execute(node);
			if (c > 0) {
				count.addAndGet(c);
			}
			return false;
		});

		return count.get();
	}

	@Override
	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		mcAddConstraint++;

		if (this.tryAddConstraintLevel > 0) {
			return this.nodeGraph.addConstraint(node, constraint);
		}

		try {
			this.tryAddConstraintLevel++;
			return constraintUtil.addConstraint(node, constraint);
		} finally {
			this.tryAddConstraintLevel--;
		}
	}

	@Override
	public void addLoadNodeListener(IRListener1<IRReteNode> listener) {

		mcAddLoadNodeListener++;

		if (loadNodeListener == null) {
			loadNodeListener = new XRRListener1Adapter<>();
		}

		loadNodeListener.addListener(listener);
	}

	@Override
	public IRRule addRule(String ruleName, IRList condList, IRList actionList) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("==> addRule: %s, %s, %s", ruleName, condList, actionList));
		}

		mcAddRule++;

		/******************************************************/
		// update condition list
		/******************************************************/
		condList = _rebuild(condList);

		/******************************************************/
		// optimize action expression list
		/******************************************************/
		actionList = OptimizeUtil.optimizeRuleActionIndexVar(condList, actionList);

		/******************************************************/
		// optimize rule
		/******************************************************/
		if (OptimizeUtil.OPT_RULE_HAS_STMT) {
			Pair<IRList, IRList> rst = OptimizeUtil.optimizeRuleHasStmt(condList, actionList, this.getInterpreter(),
					this.getFrame());
			condList = rst.getKey();
			actionList = rst.getValue();
		}

		IRRule rule = nodeGraph.addRule(ruleName, condList, actionList, RETE_PRIORITY_DEFAULT);

		for (IRReteNode parentNode : rule.getParentNodes()) {

			RuleUtil.travelReteParentNodeByPostorder(parentNode, (node) -> {

				/******************************************************/
				// Check node update queue
				/******************************************************/
				if (node.getReteType() != RReteType.ROOT0 && node.getReteStage() != RReteStage.InQueue) {

					boolean needUpdate = false;

					if (node.getParentNodes() != null) {
						for (IRReteNode parent : node.getParentNodes()) {
							if (parent.getEntryQueue().size() > 0) {
								needUpdate = true;
								break;
							}
						}
					}

					if (needUpdate) {
						addUpdateNode(node);
					}
				}

				if (node.getReteType() == RReteType.ROOT0 && node.getEntryQueue().size() > 0) {
					addUpdateNode(node);
				}

				/******************************************************/
				// Check node cache
				/******************************************************/
				_checkCache(node);

				return false;
			});
		}

		return rule;
	}

	@Override
	public void addRuleExecutedListener(IRListener1<IRRule> listener) {
		mcAddRuleExecutedListener++;
		modelRuleExecutedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addRuleFailedListener(IRListener1<IRRule> listener) {
		mcAddRuleFailedListener++;
		modelRuleFailedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addSaveNodeListener(IRListener1<IRReteNode> listener) {

		mcAddSaveNodeListener++;

		if (saveNodeListener == null) {
			saveNodeListener = new XRRListener1Adapter<>();
		}

		saveNodeListener.addListener(listener);
	}

	@Override
	public boolean addStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStatement: " + stmt);
		}

		mcAddStatement++;

		RReteStatus status = _getNewStmtStatus();

		RUpdateResult rst = _addReteEntry(stmt, status);

		// stmt updated, active the listener
		if (RUpdateResult.isValidUpdate(rst)) {
			stmtListenUpdater.update(this);
			return true;
		}

		return false;
	}

	@Override
	public void addStatementListener(IRList condList, IRListener1<IRList> listener) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStmtListener: " + condList);
		}

		mcAddStatementListener++;

		stmtListenUpdater.addStatementListener(this.findNode(condList), listener);
	}

	@Override
	public void addUpdateNode(IRReteNode node) throws RException {

		mcAddUpdateNode++;

		if (node.getReteType() == RReteType.VAR) {

			updateQueue.push(node);

		} else {

			/***********************************************************/
			// If the statement is added in closing phase, do not update
			// the Rete tree until all working node completed
			/***********************************************************/
			if (_isInClosingPhase()) {
				needRestart = true;
			} else {
				updateQueue.push(node);
				_setRunState(RRunState.Runnable);
			}
		}
	}

	@Override
	public boolean assumeStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> tryAddStatement: " + stmt);
		}

		mcAssumeStatement++;

		if (stmt.getNamedName() == null) {
			throw new RException("Invalid stmt: " + stmt);
		}

		if (processingLevel > 0) {
			throw new RException("do not support assume in reason: " + stmt);
		}

		try {

			this.assuemeStatmentLevel++;

			RUpdateResult rst = _addReteEntry(stmt, RReteStatus.ASSUME);
			if (rst == RUpdateResult.NOCHANGE) {
				return true;
			}

			if (rst == RUpdateResult.INVALID) {
				return false;
			}

			// verify
			_checkConstraintConflict(_findRootNode(stmt.getNamedName(), stmt.size()));

			stmtListenUpdater.update(this);
			return true;

		} catch (RConstraintConflict e) {
			removeStatement(stmt);
			this.assuemeStatmentLevel--;
			return false;
		}
	}

	@Override
	public boolean backSearch(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> backSearch: " + filter);
		}

		mcBackSearch++;

		if (filter == null) {
			throw new RException("not support null");
		}

		if (!ReteUtil.isReteStmtNoVar(filter)) {
			throw new RException("not support stmt: " + filter);
		}

		// Check root node for root statement
		if (_findRootEntry(filter, 0) != null) {
			return true;
		}

//		return new XRBackSearcher(this).search(filter);
		return false;
	}

	@Override
	public void beginTransaction() throws RException {
		// TODO Auto-generated method stub

	}

	@Override
	public IRIterator<? extends IRList> buildStatementIterator(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> listStatements: " + filter);
		}

		if (filter == null) {
			throw new RException("null filter not support");
		}

		/******************************************************/
		// find matched node
		/******************************************************/
		IRReteNode matchedNode = nodeGraph.getNodeByTree(filter);
		if (!RReteType.isRootType(matchedNode.getReteType()) && matchedNode.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid match node: " + matchedNode);
		}

		return new XRStatementLazyIterator(this, matchedNode);
	}

	@Override
	public void clean() throws RException {

	}

	@Override
	public void delete(IRInterpreter interpreter, IRFrame frame) throws RException {

		if (modelFrame != null) {
			RuleUtil.removeDefaultModel(modelFrame);
		}

		if (this.isDeleted()) {
			return;
		}

		super.delete(interpreter, frame);
	}

	@Override
	public int doGC() throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> doGC: ");
		}

		mcDoGC++;

		return _gc(true);
	}

	@Override
	public int execute(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> " + node.toString());
		}

		mcExecute++;

		_checkCache(node);

		int oldQueryMatchCount = modelCounter.getQueryMatchCount();

		modelCounter.incNodeExecCount();

		int update = node.update();

		node.setReteStage(RReteStage.InActive);
		node.clean();

		if (node.getReteType() == RReteType.RULE) {

			if (node.getRunState() == RRunState.Failed) {
				this.modelRuleFailedListenerDispatcher.doAction((IRRule) node);
			} else {
				this.modelRuleExecutedListenerDispatcher.doAction((IRRule) node);
			}
		}

		if (update > 0) {

			node.incExecCount(modelCounter.getNodeExecuteCount());

			for (IRReteNode child : node.getChildNodes(true)) {

				switch (child.getReteStage()) {
				case Active:

					if (child.getPriority() >= modelPriority) {
						updateQueue.push(child);
						activeUpdate++;
					}

					break;

				case InQueue:
					// Do nothing
					break;

				case InActive:

					if (child.getPriority() >= modelPriority) {
						updateQueue.push(child);
					} else {
						activeQueue.add(child);
						child.setReteStage(RReteStage.Active);
						activeUpdate++;
					}

					break;
				case OutQueue:
				default:
					throw new RException("Invalid stage: " + child.getReteStage());
				}

			}

		} else {
			modelCounter.incNodeIdleCount();
		}

		int newQueryMatchCount = modelCounter.getQueryMatchCount();
		if (newQueryMatchCount > oldQueryMatchCount) {
			node.addQueryMatchCount(newQueryMatchCount - oldQueryMatchCount);
		}

		return update;
	}

	@Override
	public IRFrame findFrame() {
		return this.modelFrame;
	}

	@Override
	public IRReteNode findNode(IRList condList) throws RException {
		return nodeGraph.getNodeByTree(condList);
	}

	@Override
	public boolean fixStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> fixStatement: " + stmt);
		}

		mcFixStatement++;

		RUpdateResult rst = _addReteEntry(stmt, RReteStatus.FIXED_);

		// stmt updated, active the listener
		if (RUpdateResult.isValidUpdate(rst)) {
			stmtListenUpdater.update(this);
			return true;
		}

		return false;
	}

	@Override
	public String getCachePath() {
		return modelCachePath;
	}

	@Override
	public IRModelCounter getCounter() {
		return modelCounter;
	}

	@Override
	public long getCounterValue(String countkey) {

		switch (countkey) {
		case CK_GC_TRIGGER:
			return gcTrigger;

		case CK_GC_COUNT:
			return gcCount;

		case CK_HAS_STMT_CACHE:
			return hasEntryCacheMap.size();

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
		case CK_HAS_STMT_1:
			return mcHasStatement1;
		case CK_HAS_STMT_2:
			return mcHasStatement2;
		case CK_LIST_STMT:
			return mcListStatements;
		case CK_QUERY:
			return mcQuery;
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
		case CK_BACK_SEARCH:
			return mcBackSearch;
		}

		return 0;
	}

	@Override
	public List<String> getCounterKeyList() {
		return modelCountKeyList;
	}

	@Override
	public IREntryTable getEntryTable() {
		return entryTable;
	}

	@Override
	public IRFrame getFrame() {
		return this.modelFrame;
	}

	@Override
	public IRInterpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public IRModel getModel() {
		return this;
	}

	@Override
	public String getModelName() {
		return this.getInstanceName();
	}

	@Override
	public IRNodeGraph getNodeGraph() {
		return nodeGraph;
	}

	@Override
	public int getPriority() {
		return modelPriority;

	}

	@Override
	public RRunState getRunState() {
		return modelRunState;
	}

	@Override
	public void getTransaction() {
		// TODO Auto-generated method stub

	}

	@Override
	public IRVar getVar(String varName) throws RException {

		mcGetVar++;

		switch (varName) {
		case V_M_STATE:

			if (modelStatsVar == null) {
				modelStatsVar = RulpUtil.asVar(this.getMember(varName).getValue());
			}

			return modelStatsVar;

		default:

			IRMember mbr = this.getMember(varName);
			if (mbr != null && mbr.getValue().getType() == RType.VAR) {
				return (IRVar) mbr.getValue();
			}

			IRFrameEntry varEntry = this.getFrame().getEntry(varName);
			if (varEntry != null) {
				IRObject obj = varEntry.getObject();
				if (obj.getType() == RType.VAR) {
					return (IRVar) obj;
				}
			}
		}

		return null;
	}

	@Override
	public RRunState halt() throws RException {

		mcHalt++;

		RRunState _state = getRunState();
		switch (_state) {
		case Completed:
		case Runnable:
		case Running:
		case Partial:
			_setRunState(RRunState.Halting);
			return getRunState();

		case Halting:
			return RRunState.Halting;

		case Failed:
			return RRunState.Failed;

		default:
			throw new RException("unknown state: " + getRunState());
		}
	}

	@Override
	public boolean hasStatement(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> hasStatement: " + filter);
		}

		mcHasStatement1++;

		// Has any stmt
		if (filter == null) {
			return _hasAnyStatement();
		}

		// Check root node for root statement
		if (ReteUtil.isReteStmtNoVar(filter)) {
			return _findRootEntry(filter, 0) != null;
		}

		if (_hasCacheStatement(filter)) {
			++hasStmtHitCount;
			return true;
		}

		return _listStatements(filter, 0, 1, false, REntryFactory.defaultBuilder(), (entry) -> {
			_addCacheStatement(entry);
			return true;
		}) > 0;
	}

	@Override
	public boolean hasStatement(IRList filter, List<OrderEntry> orderList) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> hasStatement: " + filter + ", order=" + orderList);
		}

		if (orderList == null || orderList.isEmpty()) {
			return hasStatement(filter);
		}

		mcHasStatement2++;

		// Has any stmt
		if (filter == null) {
			return _hasAnyStatement();
		}

		// Check root node for root statement
		if (ReteUtil.isReteStmtNoVar(filter)) {
			return _findRootEntry(filter, 0) != null;
		}

		if (_hasCacheStatement(filter)) {
			++hasStmtHitCount;
			return true;
		}

		IRReteEntry entry = _findIndexStatement(filter, orderList);
		if (entry == null) {
			return false;
		}

		_addCacheStatement(entry);
		return true;
	}

	@Override
	public void init(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		this.interpreter = interpreter;

		/******************************************/
		// Create Model Frame
		/******************************************/
		this.modelFrame = RulpFactory.createFrame(frame, A_MODEL);
		RulpUtil.incRef(modelFrame);
		RuleUtil.setDefaultModel(modelFrame, this);

		RuleUtil.createModelVar(this, V_M_STATE, RRunState.toObject(this.getRunState()));
//		RuleUtil.createModelVar(this, V_M_RBS_INIT, RulpFactory.createBoolean(false));
		RuleUtil.createModelVar(this, V_M_CST_INIT, RulpFactory.createBoolean(false));

		this.gcLastGcTime = System.currentTimeMillis();

		RuleUtil.createModelVar(this, V_M_GC_CAPACITY, RulpFactory.createLong(gcCapacity))
				.addVarListener((v1, o1, o2) -> {
					gcCapacity = RulpUtil.asLong(o2).asLong();
				});

		RuleUtil.createModelVar(this, V_M_GC_INTERVAL, RulpFactory.createLong(gcInterval))
				.addVarListener((v1, o1, o2) -> {
					gcInterval = RulpUtil.asLong(o2).asLong();
				});

		RuleUtil.createModelVar(this, V_M_GC_INACTIVE_LEAF, RulpFactory.createLong(DEF_GC_INACTIVE_LEAF))
				.addVarListener((v1, o1, o2) -> {
					nodeGraph.setGcMaxInactiveLeafCount(RulpUtil.asInteger(o2).asInteger());
				});

		RuleUtil.createModelVar(this, V_M_GC_MAX_CACHE_NODE, RulpFactory.createLong(-1))
				.addVarListener((v1, o1, o2) -> {
					nodeGraph.setGcMaxCacheNodeCount(RulpUtil.asInteger(o2).asInteger());
				});

	}

	@Override
	public boolean isCacheEnable() {
		return cacheEnable;
	}

	@Override
	public List<? extends IRCacheWorker> listCacheWorkers() {
		return new ArrayList<>(cacheWorkerList);
	}

	@Override
	public int listStatements(IRList filter, int statusMask, int limit, boolean reverse, IREntryIteratorBuilder builder,
			IREntryAction action) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> listStatements: " + filter + ", " + statusMask + ", " + limit + ", " + reverse);
		}

		mcListStatements++;

		if (builder == null) {
			builder = REntryFactory.defaultBuilder();
		}

		return _listStatements(filter, statusMask, limit, reverse, builder, action);
	}

	@Override
	public void query(IRResultQueue resultQueue, IRList condList, int limit) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> query: cond=" + condList + ", limit=" + limit);
		}

		mcQuery++;

		/*****************************************************/
		// Does not support query when running
		/*****************************************************/
		if (processingLevel > 0) {
			throw new RException("Can't query, the model is running");
		}

		_queryCond(resultQueue, findNode(condList), limit);
		_gc(false);
	}

	@Override
	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		mcRemoveConstraint++;

		if (this.tryRemoveConstraintLevel > 0) {
			return this.nodeGraph.removeConstraint(node, constraint);
		}

		try {

			this.tryRemoveConstraintLevel++;
			return constraintUtil.removeConstraint(node, constraint);

		} finally {
			this.tryRemoveConstraintLevel--;
		}
	}

	@Override
	public IRRule removeRule(String ruleName) throws RException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> removeStatement: " + stmt);
		}

		mcRemoveStatement++;

		if (ReteUtil.getStmtVarCount(stmt) != 0) {
			throw new RException("Invalid stmt: " + stmt);
		}

		IRReteNode rootNode = _findRootNode(stmt.getNamedName(), stmt.size());
		_checkCache(rootNode);

		IRReteEntry entry = ReteUtil.getStmt(rootNode, stmt);
		if (entry == null || entry.isDroped()) {
			return false;
		}

		entryTable.deleteEntry(entry);
		rootNode.incEntryDeleteCount();
		return true;
	}

	@Override
	public int save() throws RException {

		mcSave++;

		int totalSaveLines = 0;

		if (cacheUpdateCount == 0) {
			return 0;
		}

		try {

			for (IRReteNode node : nodeGraph.listNodes(RReteType.ROOT0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRReteNode) node);
				if (cacheWorker == null || cacheWorker.getSaver() == null || !cacheWorker.isDirty()) {
					continue;
				}

				totalSaveLines += cacheWorker.save();
			}

			for (IRReteNode node : nodeGraph.listNodes(RReteType.NAME0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRReteNode) node);
				if (cacheWorker == null || cacheWorker.getSaver() == null || !cacheWorker.isDirty()) {
					continue;
				}

				totalSaveLines += cacheWorker.save();
			}

			cacheUpdateCount = 0;
			return totalSaveLines;

		} catch (IOException e) {

			if (RuleUtil.isModelTrace()) {
				e.printStackTrace();
			}

			throw new RException(e.toString());

		} finally {
			_gc(false);
		}

	}

	@Override
	public void setModelCachePath(String cachePath) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setModelCachePath: " + cachePath);
		}

		mcSetModelCachePath++;

		if (this.modelCachePath != null && !this.modelCachePath.equals(cachePath)) {
			throw new RException(
					String.format("Can't rebind cache path: old=%s, new=%s", this.modelCachePath, cachePath));
		}

		this.modelCachePath = cachePath;
		this.cacheEnable = true;

		if (!FileUtil.isExistDirectory(cachePath)) {
			if (!new File(cachePath).mkdirs()) {
				throw new RException(String.format("Can't mkdirs: %s", cachePath));
			}
		}

		for (File file : new File(modelCachePath).listFiles()) {

			String fileName = file.getName();
			if (!fileName.endsWith(MODEL_CACHE_SUFFIX)) {
				continue;
			}

			String cacheName = FileUtil.getFilePreName(fileName);
			int pos = cacheName.lastIndexOf('.');
			if (pos == -1) {
				continue;
			}

			String nodeNum = cacheName.substring(pos + 1);
			if (!StringUtil.isNumber(nodeNum)) {
				continue;
			}
			int stmtLen = Integer.valueOf(nodeNum);
			if (!ReteUtil.isValidStmtLen(stmtLen)) {
				continue;
			}

			String nodeName = cacheName.substring(0, pos).trim();
			if (nodeName.isEmpty()) {
				nodeName = null;
			}

			IRReteNode node = _findRootNode(nodeName, stmtLen);
			// cache has been created
			if (node.getCacheWorker() != null) {
				continue;
			}

			XRCacheWorker cacheWorker = _getCacheWorker(node);
			if (node.getPriority() > 0 || node.getEntryQueue().size() > 0) {
				int loadCount = cacheWorker.load();
				if (loadCount > 0) {
					addUpdateNode(node);
				}
			}
		}
	}

	@Override
	public void setNodeContext(RNodeContext nodeContext) {
		this.nodeContext = nodeContext;
	}

	@Override
	public void setNodeLoader(IRReteNode node, IRStmtLoader loader) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setNodeLoader: node=" + node);
		}

		mcSetNodeLoader++;

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("invalid node type: " + node);
		}

		_setNodeCache(node, loader, null);
	}

	@Override
	public void setNodeSaver(IRReteNode node, IRStmtSaver saver) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setNodeSaver: node=" + node);
		}

		mcSetNodeSaver++;

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("invalid node type: " + node);
		}

		_setNodeCache(node, null, saver);
	}

	@Override
	public int start(int priority, final int maxStep) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("starting: " + this + ", priority=" + priority);
		}

		mcStart++;

		if (priority > RETE_PRIORITY_MAXIMUM) {
			throw new RException("Invalid priority: " + priority);
		}

		if (priority < 0) {
			priority = modelPriority;
		}

		/*****************************************************/
		// the function must be called in model internal
		// like rule action body
		/*****************************************************/
		if (processingLevel > 0) {

			RRunState state = this.getRunState();
			if (state == RRunState.Halting || state == RRunState.Runnable) {
				_setRunState(RRunState.Running);
			}

			// Let loop running in top level
			return 0;
		}

		this.needRestart = false;
		int oldModelPriority = this.modelPriority;
		this.modelPriority = priority;

		try {

			this.processingLevel++;
			return _run(maxStep);

		} finally {

			this.processingLevel--;
			this.modelPriority = oldModelPriority;
			_gc(false);
		}
	}

	@Override
	public boolean tryAddStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> tryAddStatement: " + stmt);
		}

		mcTryAddStatement++;

		RReteStatus status = _getNewStmtStatus();

		try {

			RUpdateResult rst = _addReteEntry(stmt, status);

			// stmt updated, active the listener
			if (RUpdateResult.isValidUpdate(rst)) {
				stmtListenUpdater.update(this);
				return true;
			}

		} catch (RConstraintConflict e) {
			if (RuleUtil.isModelTrace()) {
				e.printStackTrace();
			}
		}

		return false;
	}

}
