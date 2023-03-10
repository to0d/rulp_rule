package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_HIGH_PRIORITY;
import static alpha.rulp.rule.Constant.A_MODEL;
import static alpha.rulp.rule.Constant.DEF_GC_CAPACITY;
import static alpha.rulp.rule.Constant.DEF_GC_INACTIVE_LEAF;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_QUERY;
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
import static alpha.rulp.ximpl.model.XRModelCount.CK_HAS_STMT_CACHE;

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
import java.util.Stack;
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
import alpha.rulp.utils.AttrUtil;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.cache.IRBufferWorker;
import alpha.rulp.ximpl.cache.IRBufferWorker.CacheStatus;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.cache.XRStmtFileDefaultCacher;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.constraint.RConstraintConflict;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryFactory;
import alpha.rulp.ximpl.entry.REntryQueueType;
import alpha.rulp.ximpl.entry.XREntryQueueOrder;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRNodeSubGraph;
import alpha.rulp.ximpl.node.IRNodeUpdateQueue;
import alpha.rulp.ximpl.node.RReteStage;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.SourceNode;
import alpha.rulp.ximpl.node.XRNodeGraph;
import alpha.rulp.ximpl.node.XRNodeUpdateQueue;
import alpha.rulp.ximpl.node.XTempVarBuilder;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRModel extends AbsRInstance implements IRModel {

	static class RQueryHelper {

		protected boolean activate = false;

		protected final boolean backward;

		protected final int limit;

		protected final XRModel model;

		protected int oldModelPriority = -1;

		protected final IRReteNode queryNode;

		protected IRNodeSubGraph subGraph;

		protected boolean gc;

		public RQueryHelper(XRModel model, IRReteNode queryNode, int limit, boolean backward, boolean gc) {
			super();
			this.model = model;
			this.queryNode = queryNode;
			this.limit = limit;
			this.backward = backward;
			this.gc = gc;
		}

		public void activateSubGraph() throws RException {

			if (activate) {
				return;
			}

			/******************************************************/
			// Invoke running
			/******************************************************/
			model.processingLevel++;
			model.activeUpdate++;
			model._setRunState(RRunState.Running);

			oldModelPriority = model.modelPriority;
			model.modelPriority = RETE_PRIORITY_QUERY;

			/******************************************************/
			// Active subgraph
			/******************************************************/
			if (subGraph != null) {
				subGraph.activate();
			}

			activate = true;
		}

		public void buildSubGraph() throws RException {

			/******************************************************/
			// Build subgraph
			/******************************************************/
			subGraph = model.nodeGraph.createSubGraphForQueryNode(queryNode, backward);

			/******************************************************/
			// Ensure there is no executing node in the SubGraph
			/******************************************************/
			if (model._getExecuteLevel() > 0) {

				Set<IRReteNode> allExecutingNodes = new HashSet<>(model.executeStack);
				for (IRReteNode node : subGraph.getNodes()) {
					if (allExecutingNodes.contains(node)) {
						throw new RException("node is already on stack: " + node);
					}
				}
			}

			/******************************************************/
			// Activate sub group
			/******************************************************/
			subGraph.setGraphPriority(RETE_PRIORITY_QUERY + 1);

			if (limit > 0) {

				/********************************************/
				// Lower priority for nodes that can run incrementally and parallelly
				/********************************************/
				for (IRReteNode node : subGraph.getNodes()) {

					if (ReteUtil.supportUpdateIncrementally(node)) {
						subGraph.setNodePriority(node, RETE_PRIORITY_QUERY);
					}
				}

				RuleUtil.travelReteParentNodeByPostorder(queryNode, (_node) -> {

					if (!ReteUtil.supportUpdateIncrementally(_node) && !RReteType.isRootType(_node.getReteType())) {
						subGraph.setNodePriority(_node, RETE_PRIORITY_QUERY + 2);
					}

					return false;
				});

			}

			/********************************************/
			// Increase [high-priority] node
			/********************************************/
			for (IRReteNode node : subGraph.getNodes()) {

				if (AttrUtil.containAttribute(node, A_HIGH_PRIORITY)) {

					RuleUtil.travelReteParentNodeByPostorder(node, (_node) -> {

						if (subGraph.containNode(_node)) {
							int pri = subGraph.getNewPriority(_node);
							if (pri < RETE_PRIORITY_PARTIAL_MAX) {
								subGraph.setNodePriority(_node, pri + 1);
							}
						}

						return false;
					});
				}
			}
		}

		public boolean hasNext(int maxLimit) throws RException {

			while (model._hasUpdateNode()) {

				IRReteNode node = model._nextUpdateNode();
				if (node != null) {

					switch (node.getRunState()) {
					case Completed:
					case Runnable:
					case Running:

						int execLimit = -1;
						if (limit > 0) {

							if (ReteUtil.supportUpdateIncrementally(node)) {
								execLimit = 1;

							} else if (node == queryNode) {
								execLimit = maxLimit;
							}
						}

						/*******************************************/
						// Auto remove unused entry to save space
						/*******************************************/
						if (gc && node.isAutoGC()) {
							node.doGCEntry();
						}

						int update = model.execute(node, execLimit);
						if (node.getRunState() == RRunState.Failed) {
							throw new RException(
									String.format("node <%s> failed, msg=%s", node.getNodeName(), node.getLastError()));
						}

						if (node == queryNode && update > 0) {
							return true;
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

			return false;
		}

		public void prepare() throws RException {

			/******************************************************/
			// Update cache
			/******************************************************/
			if (model.isBufferEnable()) {
				for (SourceNode sn : RuleUtil.listSource(model, queryNode)) {
					model._checkCache(sn.rule);
				}
				model._checkCache(queryNode);
			}

		}

		public void query(IREntryAction action) throws RException {

			prepare();

			int resultCount = 0;

			int queryEntryIndex = 0;

			IREntryQueue queryQueue = queryNode.getEntryQueue();

			/******************************************************/
			// Load results directly
			/******************************************************/
			{

				while (queryEntryIndex < queryQueue.size()) {

					IRReteEntry entry = queryQueue.getEntryAt(queryEntryIndex++);
					if (!action.addEntry(entry)) {
						continue;
					}

					resultCount++;

					// limit <= 0 means query all
					if (limit > 0 && resultCount >= limit) {
						return;
					}
				}
			}

//			/******************************************************/
//			// 
//			/******************************************************/
//			if (limit > 0 && backward) {
//
//				subGraph = model.nodeGraph.createSubGraphForQueryNode(queryNode, false);
//
////				List<IRReteNode> nodes = RuleUtil.getAllParentNodes(queryNode);
////				Collections.sort(nodes, (n1, n2) -> {
////					return n1.getNodeId() - n2.getNodeId();
////				});
//
//			}

			/******************************************************/
			// Build SubGraph
			/******************************************************/
			buildSubGraph();

			/******************************************************/
			// Activate SubGraph
			/******************************************************/
			activateSubGraph();

			/******************************************************/
			// Search
			/******************************************************/
			try {

				while ((limit <= 0 || limit > resultCount) && hasNext(limit - resultCount)) {

					while (queryEntryIndex < queryQueue.size()) {

						IRReteEntry entry = queryQueue.getEntryAt(queryEntryIndex++);
						if (!action.addEntry(entry)) {
							continue;
						}

						resultCount++;

						// limit <= 0 means query all
						if (limit > 0 && resultCount >= limit) {
							return;
						}
					}
				}

				return;

			} finally {
				rollback();
			}
		}

		public void rollback() throws RException {

			if (!activate) {
				return;
			}

			/******************************************************/
			// Stop model running
			/******************************************************/
			model.processingLevel--;
			model._setRunState(RRunState.Partial);

			if (oldModelPriority != -1) {
				model.modelPriority = oldModelPriority;
			}

			/******************************************************/
			// rollback subgraph
			/******************************************************/
			if (subGraph != null) {
				subGraph.rollback();
			}

			activate = false;
		}

	}

	static class RQueryIndexHelper extends RQueryHelper {

		public RQueryIndexHelper(XRModel model, IRReteNode queryNode, int limit, boolean backward, boolean gc) {
			super(model, queryNode, limit, backward, gc);
		}

		public void query(IREntryAction action, IRList matchStmt) throws RException {

			prepare();

			int resultCount = 0;

			XREntryQueueOrder queryQueue = (XREntryQueueOrder) queryNode.getEntryQueue();
			IRIterator<IRReteEntry> queryIt = queryQueue.iterator(matchStmt);

			/******************************************************/
			// Load results directly
			/******************************************************/
			while (queryIt.hasNext()) {

				IRReteEntry entry = queryIt.next();
				if (!action.addEntry(entry)) {
					continue;
				}

				resultCount++;

				// limit <= 0 means query all
				if (limit > 0 && resultCount >= limit) {
					return;
				}
			}

			/******************************************************/
			// Build SubGraph
			/******************************************************/
			buildSubGraph();

			/******************************************************/
			// Activate SubGraph
			/******************************************************/
			activateSubGraph();

			/******************************************************/
			// Search
			/******************************************************/
			try {

				while ((limit <= 0 || limit > resultCount) && hasNext(limit - resultCount)) {

					while (queryIt.hasNext()) {

						IRReteEntry entry = queryIt.next();
						if (!action.addEntry(entry)) {
							continue;
						}

						resultCount++;

						// limit <= 0 means query all
						if (limit > 0 && resultCount >= limit) {
							return;
						}
					}

				}

				return;

			} finally {
				rollback();
			}
		}
	}

	static class RQueryIterator implements IRIterator<IRReteEntry> {

		private boolean buildSubGraph = false;

		private boolean completed = false;

		private boolean ended = false;

		private RQueryHelper helper;

		private int limit;

		private IRReteEntry nextEntry;

		private int queryEntryIndex = 0;

		private int queryEntrySize = -1;

		private IREntryQueue queryQueue;

		private int resultCount = 0;

		public RQueryIterator(RQueryHelper helper) throws RException {
			super();
			this.helper = helper;
			this.queryQueue = helper.queryNode.getEntryQueue();
			this.limit = helper.limit;

		}

		public boolean _reachEnded() {
			return limit > 0 && resultCount >= limit;
		}

		@Override
		public boolean hasNext() throws RException {

			if (nextEntry != null) {
				return true;
			}

			if (ended) {
				return false;
			}

			if (queryEntrySize == -1) {
				helper.prepare();
				queryEntrySize = queryQueue.size();
			}

			if (_reachEnded()) {
				ended = true;
				return false;
			}

			FIND: while (nextEntry == null) {

				/********************************************/
				// expand
				/********************************************/
				if (queryEntryIndex >= queryEntrySize) {

					if (completed) {
						ended = true;
						return false;
					}

					/****************************************/
					// Build SubGraph
					/****************************************/
					if (!buildSubGraph) {
						helper.buildSubGraph();
						buildSubGraph = true;
					}

					/****************************************/
					// Activate SubGraph
					/****************************************/
					helper.activateSubGraph();

					/****************************************/
					// Search
					/****************************************/
					try {

						boolean expend = false;
						while (!_reachEnded() && helper.hasNext(limit - resultCount)) {

							if (queryQueue.size() > queryEntrySize) {
								queryEntrySize = queryQueue.size();
								expend = true;

								/********************************************/
								// Find next entry
								/********************************************/
								while (nextEntry == null && queryEntryIndex < queryEntrySize) {

									IRReteEntry entry = queryQueue.getEntryAt(queryEntryIndex++);
									if (entry == null || entry.isDroped()) {
										continue;
									}

									nextEntry = entry;
									resultCount++;
								}

								break;
							}
						}

						if (!expend) {
							completed = true;
							break FIND;
						}

					} finally {
						helper.rollback();
					}
				}

				/********************************************/
				// Find next entry
				/********************************************/
				while (nextEntry == null && queryEntryIndex < queryEntrySize) {

					IRReteEntry entry = queryQueue.getEntryAt(queryEntryIndex++);
					if (entry == null || entry.isDroped()) {
						continue;
					}

					nextEntry = entry;
					resultCount++;
				}

			}

			return nextEntry != null;
		}

		@Override
		public IRReteEntry next() throws RException {

			if (nextEntry == null && !hasNext()) {
				return null;
			}

			IRReteEntry rt = nextEntry;
			nextEntry = null;

//			if (!rebuildResult) {
//				return rt;
//			}

			/******************************************************/
			// Update variable value
			/******************************************************/

			return rt;
		}

	}

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

	static final String MODEL_BUFFER_SUFFIX = ".mc";

	static final RRunState MODEL_SSTATE[][] = {

			// Completed(0), Runnable(1), Running(2), Halting(3), Failed(4), Partial(5)
			{ Completed, Completed, Completed, Halting, Failed, Completed }, // Completed
			{ Runnable, Runnable, Running, Halting, Failed, Runnable }, // Runnable
			{ Running, Running, Running, Halting, Failed, Running }, // Running
			{ Halting, Halting, Halting, Halting, Halting, Halting }, // Halting
			{ Failed, Failed, Failed, Failed, Failed, Failed }, // Failed
			{ Partial, Partial, Partial, Halting, Failed, Partial }, // Partial
	};

	protected static int nodeExecId = 0;

	protected List<IRReteNode> activeQueue = new LinkedList<>();

	protected int activeUpdate = 0;

	protected int assuemeStatmentLevel = 0;

	protected boolean cacheEnable = false;

	protected int cacheUpdateCount = 0;

	protected final LinkedList<XRBufferWorker> cacheWorkerList = new LinkedList<>();

	protected final ModelConstraintUtil constraintUtil;

	protected final XRModelCount counter = new XRModelCount();

	protected final IREntryTable entryTable = new XREntryTable();

	protected Stack<IRReteNode> executeStack = new Stack<>();

	protected long gcCapacity = DEF_GC_CAPACITY;

	protected long gcInterval = -1;

	protected long gcLastGcTime = -1;

	protected Map<String, IRReteEntry> hasEntryCacheMap = new HashMap<>();

	protected IRInterpreter interpreter;

	protected XRRListener1Adapter<IRReteNode> loadNodeListener = null;

	protected String modelBufferPath = null;

	protected final XRRModelCounter modelCounter;

	protected IRFrame modelFrame;

	protected int modelPriority = RETE_PRIORITY_DEFAULT;

	protected final XRRListener1Adapter<IRRule> modelRuleExecutedListenerDispatcher = new XRRListener1Adapter<>();

	protected final XRRListener1Adapter<IRRule> modelRuleFailedListenerDispatcher = new XRRListener1Adapter<>();

	protected RRunState modelRunState = RRunState.Completed; // default

	protected IRVar modelStatsVar;

	protected boolean needRestart = false;

	protected Stack<RNodeContext> nodeContextStack = new Stack<>();

	protected final XRNodeGraph nodeGraph;

	protected int processingLevel = 0;

	protected final LinkedList<IRReteNode> restartingNodeList = new LinkedList<>();

	protected XRRListener1Adapter<IRReteNode> saveNodeListener = null;

	protected final XRStmtListenUpdater stmtListenUpdater = new XRStmtListenUpdater();

	protected int stmtMaxIndex = 0;

	protected IRTransaction transaction = null;

	protected int tryAddConstraintLevel = 0;

	protected int tryRemoveConstraintLevel = 0;

	protected final IRNodeUpdateQueue updateQueue = new XRNodeUpdateQueue();

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

		IRReteNode rootNode = _getRootNode(stmt.getNamedName(), stmt.size(), true);
		RNodeContext topContext = _topContext();
		int oldSize = 0;

		if (topContext != null) {
			topContext.tryAddStmt++;
			oldSize = rootNode.getEntryQueue().size();
		}

		RUpdateResult rst = _addStmt(rootNode, stmt, toStatus);

		// new stmt added
		if (rst == RUpdateResult.NEW && topContext != null) {
			int newSize = rootNode.getEntryQueue().size();
			if (newSize > oldSize) {
				topContext.actualAddStmt++;
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

		RNodeContext topContext = _topContext();

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
					if (topContext == null || this.assuemeStatmentLevel > 0) {
						throw new RConstraintConflict(String.format("Unable to add entry<%s> due to constraint<%s>",
								newEntry, failedConstraint1), rootNode, newEntry, failedConstraint1);
					}
				}

				return RUpdateResult.INVALID;
			}

			/*******************************************************/
			// Add reference
			/*******************************************************/
			if (topContext != null) {
				entryTable.addReference(newEntry, topContext.currentNode, topContext.currentEntry);
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
			if (finalStatus != REMOVE && finalStatus != TEMP__ && topContext != null) {
				entryTable.addReference(oldEntry, topContext.currentNode, topContext.currentEntry);
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
		if (finalStatus != REMOVE && finalStatus != TEMP__ && topContext != null) {
			entryTable.addReference(oldEntry, topContext.currentNode, topContext.currentEntry);
		}

		return RUpdateResult.CHANGE;
	}

	protected void _checkActiveNode() throws RException {

		if (activeUpdate > 0) {

			Iterator<IRReteNode> it = activeQueue.iterator();
			while (it.hasNext()) {
				IRReteNode node = it.next();
				if (node.getPriority() >= modelPriority) {
					updateQueue.push(node, false);
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

		XRBufferWorker cache = (XRBufferWorker) node.getBufferWorker();
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
		IRNodeSubGraph subGraph = this.nodeGraph.createSubGraphForConstraintCheck(rootNode);
		if (subGraph.isEmpty()) {
			return;
		}

		/********************************************/
		// Activate sub group
		/********************************************/
		subGraph.setGraphPriority(this.getPriority());
		subGraph.activate();

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

	protected IRReteEntry _findAnyReteEntry() throws RException {

		for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
			_checkCache(rootNode);
			IREntryCounter rootEntryCounter = rootNode.getEntryQueue().getEntryCounter();
			int totalCount = rootEntryCounter.getEntryTotalCount();
			int nullCount = rootEntryCounter.getEntryNullCount();
			int dropCount = rootEntryCounter.getEntryCount(REMOVE);
			if (totalCount > nullCount - dropCount) {

				IREntryQueue queue = rootNode.getEntryQueue();
				int size = queue.size();
				for (int i = 0; i < size; ++i) {

					IRReteEntry entry = queue.getEntryAt(i);
					if (entry != null && !entry.isDroped()) {
						return entry;
					}

				}
			}
		}

		return null;
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
		IRReteNode indexNode = graph.createNodeIndex(graph.createNodeByTree(newStmt), orderList);

		// '(?a b ?c) ==> (a b ?c)
		XREntryQueueOrder orderQueue = (XREntryQueueOrder) indexNode.getEntryQueue();
		IRReteEntry entry = orderQueue.find(stmt);
		if (entry == null) {

			XCount xcount = new XCount();

			RuleUtil.travelReteParentNodeByPostorder(indexNode, (node) -> {
				if (execute(node, -1) > 0) {
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

		IRReteNode rootNode = _getRootNode(filter.getNamedName(), filter.size(), false);
		if (rootNode == null) {
			return null;
		}

		IRReteEntry oldEntry = ReteUtil.getStmt(rootNode, filter);
		if (oldEntry == null || !ReteUtil.matchReteStatus(oldEntry, statusMask)) {
			return null;
		}

		return oldEntry;
	}

//	protected IRReteNode _getRootNode(String namedName, int stmtLen) throws RException {
//		 nodeGraph.createNodeRoot(namedName, stmtLen);
//	}

	protected Pair<Boolean, IRReteEntry> _findUniqStatement(IRList filter) throws RException {

		// Check root node for root statement
		if (ReteUtil.isReteStmtNoVar(filter)) {
			return new Pair<>(true, _findRootEntry(filter, 0));
		}

		// Check whether there is any uniq constraint that match the filter
		if (filter.getNamedName() != null && ReteUtil.indexOfVaryArgStmt(filter) == -1) {

			IRNamedNode namedNode = (IRNamedNode) _getRootNode(filter.getNamedName(), filter.size(), false);
			if (namedNode == null) {
				return new Pair<>(false, null);
			}

			// Check func node firstly
			IRConstraint1Uniq funcUniq = namedNode.getFuncUniqConstraint();
			if (funcUniq != null) {

				String uniqName = funcUniq.getUniqString(filter);
				if (uniqName != null) {

					IRReteEntry entry = funcUniq.getReteEntry(uniqName);
					if (entry == null) {
						IRList stmt = namedNode.computeFuncEntry(filter);
						if (stmt != null) {

							pushNodeContext(null);

							try {
								IREntryQueue queue = namedNode.getEntryQueue();
								int oldSize = queue.size();
								if (namedNode.getModel().addStatement(stmt) && queue.size() > oldSize) {
									entry = queue.getEntryAt(oldSize);
								}

							} finally {
								popNodeContext(null);
							}

						}
					}

					return new Pair<>(true, entry);
				}
			}

			for (IRConstraint1Uniq uniqCons : namedNode.listUniqConstraints()) {

				if (uniqCons == funcUniq) {
					continue;
				}

				// constraint match
				String uniqName = uniqCons.getUniqString(filter);
				if (uniqName != null) {
					return new Pair<>(true, uniqCons.getReteEntry(uniqName));
				}
			}
		}

		return new Pair<>(false, null);
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
		int zetaGcCount = 0;

		for (IRReteNode node : ReteUtil.getAllNodes(this.getNodeGraph())) {

			int gcCount = node.doGC();
			totalGcCount += gcCount;

			switch (node.getReteType()) {
			case ALPH0:
			case ALPH1:
			case ALPH2:
				alpahGcCount += gcCount;
				break;

			case BETA0:
			case BETA1:
			case BETA2:
			case BETA3:
				betaGcCount += gcCount;
				break;

			case ZETA0:
				zetaGcCount += gcCount;
				break;

			case ROOT0:
			case NAME0:
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
			System.out.println(String.format("==> GC: rs=%d, ra=%d, rb=%d, rz=%d, re=%d, rr=%d", smtGcCount,
					alpahGcCount, betaGcCount, zetaGcCount, exprGcCount, ruleGcCount));
		}

		this.counter.gcCount++;
		this.nodeGraph.doGc();
		this.entryTable.doGC();
		this.hasEntryCacheMap.clear();

		long curTime = System.currentTimeMillis();
		if (curTime < (this.gcLastGcTime + this.gcInterval)) {
			return totalGcCount;
		}

		counter.gcTrigger++;

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

	protected IRReteEntry _getCacheStatement(IRList filter) throws RException {

		String uniqName = ReteUtil.uniqName(filter);
		IRReteEntry cacheEntry = hasEntryCacheMap.get(uniqName);
		if (cacheEntry != null) {
			if (cacheEntry.isDroped()) {
				hasEntryCacheMap.remove(uniqName);
			} else {
				return cacheEntry;
			}
		}

		return null;
	}

	protected XRBufferWorker _getCacheWorker(IRReteNode node) throws RException {

		XRBufferWorker cache = (XRBufferWorker) node.getBufferWorker();
		if (cache == null) {

			if (modelBufferPath == null) {
				return null;
			}

			XRStmtFileDefaultCacher cacher = new XRStmtFileDefaultCacher(modelBufferPath, node);
			cache = _setNodeCache(node, cacher, cacher);
		}

		return cache;
	}

	protected int _getExecuteLevel() {
		return executeStack.size();
	}

	protected RReteStatus _getNewStmtStatus() throws RException {

		RNodeContext topContext = _topContext();
		if (topContext == null) {
			return DEFINE;
		}

		return ReteUtil.getChildStatus(topContext.currentEntry);
	}

	protected IRReteNode _getRootNode(String namedName, int stmtLen, boolean create) throws RException {

		IRReteNode node;
		if (create) {
			node = nodeGraph.createNodeRoot(namedName, stmtLen);
		} else {
			node = nodeGraph.findRootNode(namedName, stmtLen);
		}

		if (node != null) {
			_checkCache(node);
		}

		return node;
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

			IRIterator<IRReteEntry> it = builder.makeIterator(rootNode.getEntryQueue());

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
		int varyIndex = ReteUtil.indexOfVaryArgStmt(filter);
		if (varyIndex != -1) {

			if (varyIndex != (filter.size() - 1)) {
				throw new RException(String.format("invalid filter: %s", filter));
			}

			ArrayList<IRObject> extendFilterObjs = new ArrayList<>();
			XTempVarBuilder tmpVarBuilder = new XTempVarBuilder("?_vg_");

			for (int i = 0; i < varyIndex; ++i) {
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

			for (int i = varyIndex; i < namedNode.getEntryLength(); ++i) {
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
		// Find uniq statement
		/******************************************************/
		Pair<Boolean, IRReteEntry> rst = _findUniqStatement(filter);
		if (rst.getKey()) {

			IRReteEntry oldEntry = rst.getValue();
			if (oldEntry == null) {
				return 0;
			}

			if (!action.addEntry(oldEntry)) {
				return 0;
			}

			size++;
			return size;
		}

		/******************************************************/
		//
		/******************************************************/
		IRReteNode matchedNode = nodeGraph.createNodeByTree(filter);
		if (!RReteType.isRootType(matchedNode.getReteType()) && matchedNode.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid list node: " + matchedNode);
		}

		_checkCache(matchedNode);

		/******************************************************/
		// Force update node chain if query reverse
		/******************************************************/
		if (reverse) {

			RuleUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node, -1);
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

				int update = execute(node, -1);
				if (update > 0) {
					modelCounter.incQueryMatchCount();
				}

				return false;
			});

			nodeUpdated = true;
		}

		IREntryQueue matchedNodeQueue = matchedNode.getEntryQueue();
		IRIterator<IRReteEntry> it = builder.makeIterator(matchedNodeQueue);

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

				int update = execute(node, -1);
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

		IRReteNode node = updateQueue.pop();
		int _modelPriority = this.getPriority();

//		if (node.getNodeName().equals("CST02")) {
//			System.out.println();
//		}

		while (node != null && node.getPriority() < _modelPriority) {
			node = updateQueue.pop();
		}

		return node;
	}

	protected void _popExecute(IRReteNode node) throws RException {

		if (executeStack.isEmpty()) {
			throw new RException("fail to pop empty stack: " + node);
		}

		IRReteNode top = executeStack.pop();
		if (top != node) {
			throw new RException("fail to pop node: " + node);
		}
	}

	protected void _pushExecute(IRReteNode node) throws RException {

		if (!executeStack.isEmpty()) {
			if (executeStack.contains(node)) {
				throw new RException("duplicated stack: " + node);
			}
		}

		executeStack.push(node);

		int deep = executeStack.size();
		if (deep > 1 && deep > counter.mcMaxStackDeepExecute) {
			counter.mcMaxStackDeepExecute = deep;
		}
	}

	protected IRList _rebuild(IRList list) throws RException {

		/******************************************************/
		// '(a ? ?)
		// update constant
		/******************************************************/
		ArrayList<IRObject> filterObjs = null;

		IRIterator<? extends IRObject> iter = list.iterator();
		for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

			IRObject obj = iter.next();
			boolean update = false;

			if (obj.getType() == RType.LIST || obj.getType() == RType.EXPR) {

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

		if (filterObjs == null) {
			return list;
		}

		if (list.getType() == RType.EXPR) {
			return RulpFactory.createExpression(filterObjs);
		}

		return RulpUtil.toList(list.getNamedName(), filterObjs);
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
					updateQueue.push(rootNode, false);
				}

				for (IRReteNode waitingNode : restartingNodeList) {
					updateQueue.push(waitingNode, false);
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
					execute(node, -1);
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

	protected XRBufferWorker _setNodeCache(IRReteNode node, IRStmtLoader loader, IRStmtSaver saver) throws RException {

		if (loader == null && saver == null) {
			throw new RException("null loader and saver");
		}

		XRBufferWorker cache = (XRBufferWorker) node.getBufferWorker();
		if (cache == null) {
			cache = new XRBufferWorker(this, node);
			node.setBufferWorker(cache);
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

	protected RNodeContext _topContext() {

		if (nodeContextStack.isEmpty()) {
			return null;
		}

		return nodeContextStack.get(nodeContextStack.size() - 1);
	}

	protected int _update(IRReteNode updateNode) throws RException {

		IRFrame queryFrame = RulpFactory.createFrame(modelFrame, "UPDATE");
		RuleUtil.setDefaultModel(queryFrame, this);

		AtomicInteger count = new AtomicInteger();

		/******************************************************/
		// update loaders
		/******************************************************/
		RuleUtil.travelReteParentNodeByPostorder(updateNode, (node) -> {
			for (IRReteNode loader : nodeGraph.listBindFromNodes(node)) {
				if (loader.getPriority() >= 0) {
					int c = execute(loader, -1);
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
			int c = execute(node, -1);
			if (c > 0) {
				count.addAndGet(c);
			}
			return false;
		});

		return count.get();
	}

	@Override
	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		counter.mcAddConstraint++;

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

		counter.mcAddLoadNodeListener++;

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

		counter.mcAddRule++;

		/******************************************************/
		// n1:'(a ?...) ==> n1:'(a ?tmp_1 ?tmp_2)
		/******************************************************/
		condList = ReteUtil.rebuildVaryStmtList(nodeGraph, condList, new HashMap<>());

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

		IRRule rule = nodeGraph.createNodeRule(ruleName, condList, actionList, RETE_PRIORITY_DEFAULT);

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

		counter.mcAddRuleExecutedListener++;
		modelRuleExecutedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addRuleFailedListener(IRListener1<IRRule> listener) {

		counter.mcAddRuleFailedListener++;
		modelRuleFailedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addSaveNodeListener(IRListener1<IRReteNode> listener) {

		counter.mcAddSaveNodeListener++;

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

		counter.mcAddStatement++;

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

		counter.mcAddStatementListener++;

		stmtListenUpdater.addStatementListener(this.findNode(condList), listener);
	}

	@Override
	public void addUpdateNode(IRReteNode node) throws RException {

		counter.mcAddUpdateNode++;

		if (node.getReteType() == RReteType.VAR) {

			updateQueue.push(node, false);

		} else {

			/***********************************************************/
			// If the statement is added in closing phase, do not update
			// the Rete tree until all working node completed
			/***********************************************************/
			if (_isInClosingPhase()) {
				needRestart = true;
			} else {
				updateQueue.push(node, false);
				_setRunState(RRunState.Runnable);
			}
		}

	}

	@Override
	public boolean assumeStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> assumeStatement: " + stmt);
		}

		counter.mcAssumeStatement++;

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
			_checkConstraintConflict(nodeGraph.createNodeRoot(stmt.getNamedName(), stmt.size()));

			stmtListenUpdater.update(this);
			return true;

		} catch (RConstraintConflict e) {
			removeStatement(stmt);
			this.assuemeStatmentLevel--;
			return false;
		}
	}

	@Override
	public IRIterator<? extends IRList> buildStatementIterator(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> buildStatementIterator: " + filter);
		}

		if (filter == null) {
			throw new RException("null filter not support");
		}

		/******************************************************/
		// find matched node
		/******************************************************/
		IRReteNode matchedNode = nodeGraph.createNodeByTree(filter);
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

		counter.mcDoGC++;

		return _gc(true);
	}

	@Override
	public int execute(IRReteNode node, int limit) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> execute: " + node + ", limit=" + limit);
		}

		counter.mcExecute++;

		_checkCache(node);

		int oldQueryMatchCount = modelCounter.getQueryMatchCount();
		modelCounter.incNodeExecCount();
		boolean pushBack = false;
		int update = 0;

		_pushExecute(node);

		try {
			update = node.update(limit);
		} finally {
			_popExecute(node);
		}

		// may need more process
		if (limit > 0 && update > 0) {
			pushBack = true;
		} else {
			node.setReteStage(RReteStage.InActive);
			node.clean();
		}

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
						updateQueue.push(child, false);
						activeUpdate++;
					}

					break;

				case InQueue:
					// Do nothing
					break;

				case InActive:

					if (child.getPriority() >= modelPriority) {
						updateQueue.push(child, false);
					} else {
						activeQueue.add(child);
						child.setReteStage(RReteStage.Active);
						activeUpdate++;
					}

					break;

				case OutQueue:
					// this node should be executing stack, do nothing
					break;
				default:
					throw new RException("Invalid stage: " + child.getReteStage() + ", node=" + child);
				}

			}

		} else {
			modelCounter.incNodeIdleCount();
		}

		int newQueryMatchCount = modelCounter.getQueryMatchCount();
		if (newQueryMatchCount > oldQueryMatchCount) {
			node.addQueryMatchCount(newQueryMatchCount - oldQueryMatchCount);
		}

		if (pushBack) {
			updateQueue.push(node, false);
		}

		return update;
	}

	@Override
	public IRFrame findFrame() {
		return this.modelFrame;
	}

	@Override
	public IRReteNode findNode(IRList condList) throws RException {
		return nodeGraph.createNodeByTree(condList);
	}

	@Override
	public IRReteEntry findReteEntry(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> findReteEntry: " + filter);
		}

		counter.mcFindReteEntry1++;

		// Has any stmt
		if (filter == null) {
			return _findAnyReteEntry();
		}

		// Find uniq statement
		Pair<Boolean, IRReteEntry> rst = _findUniqStatement(filter);
		if (rst.getKey()) {
			return rst.getValue();
		}

		// Check cache statement
		IRReteEntry cacheEntry = _getCacheStatement(filter);
		if (cacheEntry != null) {
			++counter.hasStmtHitCount;
			return cacheEntry;
		}

		IRReteEntry findEntries[] = new IRReteEntry[1];

		int count = _listStatements(filter, 0, 1, false, REntryFactory.defaultBuilder(), (_entry) -> {
			findEntries[0] = _entry;
			return true;
		});

		if (count == 0) {
			return null;
		}

		return findEntries[0];
	}

	@Override
	public IRReteEntry findReteEntry(IRList filter, List<OrderEntry> orderList) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> findReteEntry: " + filter + ", order=" + orderList);
		}

		if (orderList == null || orderList.isEmpty()) {
			return findReteEntry(filter);
		}

		counter.mcFindReteEntry2++;

		// Has any stmt
		if (filter == null) {
			return _findAnyReteEntry();
		}

		// Find uniq statement
		Pair<Boolean, IRReteEntry> rst = _findUniqStatement(filter);
		if (rst.getKey()) {
			return rst.getValue();
		}

		// Check cache statement
		IRReteEntry cacheEntry = _getCacheStatement(filter);
		if (cacheEntry != null) {
			++counter.hasStmtHitCount;
			return cacheEntry;
		}

		IRReteEntry entry = _findIndexStatement(filter, orderList);
		if (entry == null) {
			return null;
		}

		_addCacheStatement(entry);
		return entry;
	}

	@Override
	public boolean fixStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> fixStatement: " + stmt);
		}

		counter.mcFixStatement++;

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
		return modelBufferPath;
	}

	@Override
	public IRModelCounter getCounter() {
		return modelCounter;
	}

	@Override
	public List<String> getCounterKeyList() {
		return XRModelCount.getCounterKeyList();
	}

	@Override
	public long getCounterValue(String countkey) {

		switch (countkey) {

		case CK_HAS_STMT_CACHE:
			return hasEntryCacheMap.size();

		default:
			return counter.getCounterValue(countkey);

		}
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
	public IRNodeUpdateQueue getNodeUpdateQueue() {
		return updateQueue;
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
	public IRReteNode getTopExecuteNode() {

		if (executeStack.isEmpty()) {
			return null;
		}

		return executeStack.get(executeStack.size() - 1);
	}

	@Override
	public IRVar getVar(String varName) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> getVar: " + varName);
		}

		counter.mcGetVar++;

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

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> halt: ");
		}

		counter.mcHalt++;

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
	public void init(IRList args, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> init: " + args);
		}

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

		RuleUtil.createModelVar(this, V_M_GC_INTERVAL, RulpFactory.createLong(this.gcInterval))
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
	public boolean isBufferEnable() {
		return cacheEnable;
	}

	@Override
	public List<? extends IRBufferWorker> listCacheWorkers() {
		return new ArrayList<>(cacheWorkerList);
	}

	@Override
	public int listStatements(IRList filter, int statusMask, int limit, boolean reverse, IREntryIteratorBuilder builder,
			IREntryAction action) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> listStatements: " + filter + ", " + statusMask + ", " + limit + ", " + reverse);
		}

		counter.mcListStatements++;

		if (builder == null) {
			builder = REntryFactory.defaultBuilder();
		}

		return _listStatements(filter, statusMask, limit, reverse, builder, action);
	}

	@Override
	public void popNodeContext(RNodeContext nodeContext) throws RException {

		if (nodeContextStack.isEmpty()) {
			throw new RException("fail to pop empty stack: " + nodeContext.currentNode);
		}

		RNodeContext top = nodeContextStack.pop();
		if (top != nodeContext) {
			throw new RException("fail to pop node: " + nodeContext.currentNode);
		}

	}

	@Override
	public void pushNodeContext(RNodeContext nodeContext) {

		nodeContextStack.push(nodeContext);

		int deep = nodeContextStack.size();
		if (deep > 1 && deep > counter.mcMaxStackDeepNodeContext) {
			counter.mcMaxStackDeepNodeContext = deep;
		}
	}

	@Override
	public void query(IREntryAction action, IRList condList, Map<String, IRObject> whereVarMap, int limit,
			boolean backward, boolean gc) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> query: cond=" + condList + ", where=" + whereVarMap + ", limit=" + limit
					+ ", backward=" + backward + ", gc=" + gc);
		}

		counter.mcQuery++;

//		/*****************************************************/
//		// Does not support query when running
//		/*****************************************************/
//		if (processingLevel > 0) {
//			throw new RException("Can't query, the model is running");
//		}

		if (whereVarMap == null) {

			new RQueryHelper(this, findNode(condList), limit, backward, gc).query(action);

		} else {

			IRObject[] varEntry = ReteUtil.buildVarEntry(this, condList);

			List<OrderEntry> orderList = new ArrayList<>();
			ArrayList<IRObject> matchStmtList = new ArrayList<>();

			NEXT: for (int i = 0; i < varEntry.length; ++i) {

				IRObject varObj = varEntry[i];
				if (varObj != null) {
					IRObject val = whereVarMap.get(varObj.asString());
					if (val != null) {
						matchStmtList.add(val);
						OrderEntry orderEntry = new OrderEntry();
						orderEntry.index = i;
						orderEntry.asc = true;
						orderList.add(orderEntry);
						continue NEXT;
					}
				}

				matchStmtList.add(O_Nil);
			}

			if (orderList.isEmpty()) {
				throw new RException("order not found");
			}

			IRReteNode node = findNode(condList);
			IRNodeGraph graph = getNodeGraph();
			IRReteNode indexNode = graph.createNodeIndex(node, orderList);

			IRList matchStmt = RulpFactory.createList(matchStmtList);

			new RQueryIndexHelper(this, indexNode, limit, backward, gc).query(action, matchStmt);
		}

		_gc(false);
	}

	@Override
	public IRIterator<IRReteEntry> query(IRList condList, int limit, boolean backward) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> query-iterator: cond=" + condList + ", limit=" + limit + ", backward=" + backward);
		}

		counter.mcQueryIterator++;

		/*****************************************************/
		// Does not support query when running
		/*****************************************************/
		if (processingLevel > 0) {
			throw new RException("Can't query, the model is running");
		}

		return new RQueryIterator(new RQueryHelper(this, findNode(condList), limit, backward, false));
	}

	@Override
	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> removeConstraint: " + node + ", " + constraint);
		}

		counter.mcRemoveConstraint++;

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

		counter.mcRemoveStatement++;

		if (ReteUtil.getStmtVarCount(stmt) != 0) {
			throw new RException("Invalid stmt: " + stmt);
		}

		IRReteNode rootNode = _getRootNode(stmt.getNamedName(), stmt.size(), false);
		if (rootNode == null) {
			return false;
		}

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

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> save: ");
		}

		counter.mcSave++;

		int totalSaveLines = 0;

		if (cacheUpdateCount == 0) {
			return 0;
		}

		try {

			for (IRReteNode node : nodeGraph.listNodes(RReteType.ROOT0)) {

				XRBufferWorker cacheWorker = _getCacheWorker((IRReteNode) node);
				if (cacheWorker == null || cacheWorker.getSaver() == null || !cacheWorker.isDirty()) {
					continue;
				}

				totalSaveLines += cacheWorker.save();
			}

			for (IRReteNode node : nodeGraph.listNodes(RReteType.NAME0)) {

				XRBufferWorker cacheWorker = _getCacheWorker((IRReteNode) node);
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
	public void setModelBufferPath(String bufferPath) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setModelBufferPath: " + bufferPath);
		}

		counter.mcSetModelCachePath++;

		if (this.modelBufferPath != null && !this.modelBufferPath.equals(bufferPath)) {
			throw new RException(
					String.format("Can't rebind buffer path: old=%s, new=%s", this.modelBufferPath, bufferPath));
		}

		this.modelBufferPath = bufferPath;
		this.cacheEnable = true;

		if (!FileUtil.isExistDirectory(bufferPath)) {
			if (!new File(bufferPath).mkdirs()) {
				throw new RException(String.format("Can't mkdirs: %s", bufferPath));
			}
		}

		for (File file : new File(modelBufferPath).listFiles()) {

			String fileName = file.getName();
			if (!fileName.endsWith(MODEL_BUFFER_SUFFIX)) {
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

			IRReteNode node = nodeGraph.createNodeRoot(nodeName, stmtLen);
			// cache has been created
			if (node.getBufferWorker() != null) {
				continue;
			}

			XRBufferWorker cacheWorker = _getCacheWorker(node);
			if (node.getPriority() > 0 || node.getEntryQueue().size() > 0) {
				int loadCount = cacheWorker.load();
				if (loadCount > 0) {
					addUpdateNode(node);
				}
			}
		}
	}

	@Override
	public void setNodeLoader(IRReteNode node, IRStmtLoader loader) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setNodeLoader: node=" + node);
		}

		counter.mcSetNodeLoader++;

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

		counter.mcSetNodeSaver++;

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("invalid node type: " + node);
		}

		_setNodeCache(node, null, saver);
	}

	@Override
	public int start(int priority, final int maxStep) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("start: " + priority + ", " + maxStep);
		}

		counter.mcStart++;

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

		counter.mcTryAddStatement++;

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
