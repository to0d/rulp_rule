package alpha.rulp.ximpl.model;

import static alpha.rulp.lang.Constant.O_Nil;
import static alpha.rulp.rule.Constant.A_MODEL;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.V_M_CST_INIT;
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
import static alpha.rulp.ximpl.rbs.Constant.V_M_RBS_INIT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import alpha.rulp.error.RConstraintConflict;
import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRMember;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
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
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.cache.XRStmtFileDefaultCacher;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRNodeGraph.IRNodeSubGraph;
import alpha.rulp.ximpl.node.RReteStage;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.XRNodeGraph;
import alpha.rulp.ximpl.node.XTempVarBuilder;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRModel extends AbsRInstance implements IRModel {

	static class EntryQueueIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryQueue queue;

		public EntryQueueIterator(IREntryQueue queue) {
			super();
			this.queue = queue;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < queue.size();
		}

		@Override
		public IRReteEntry next() {
			return queue.getEntryAt(index++);
		}
	}

	static class EntryQueueReverseIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryQueue queue;

		public EntryQueueReverseIterator(IREntryQueue queue) {
			super();
			this.queue = queue;
			this.index = queue.size();
		}

		@Override
		public boolean hasNext() {
			return index > 0;
		}

		@Override
		public IRReteEntry next() {
			return queue.getEntryAt(--index);
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

	class XRCacheWorker implements IRCacheWorker {

		private boolean bLoad = false;

		private int cacheLastEntryId = 0;

		private int cacheStmtCount = 0;

		private int loadCount = 0;

		private IRStmtLoader loader;

		private IRReteNode node;

		private int readLines = 0;

		private int saveCount = 0;

		private IRStmtSaver saver;

		private int writeLines = 0;

		public XRCacheWorker(IRReteNode node) {
			super();
			this.node = node;
		}

		@Override
		public int getLastEntryId() {
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
			return readLines;
		}

		@Override
		public int getSaveCount() {
			return saveCount;
		}

		public IRStmtSaver getSaver() {
			return saver;
		}

		@Override
		public int getStmtCount() {
			return cacheStmtCount;
		}

		@Override
		public int getWriteCount() {
			return writeLines;
		}

		@Override
		public boolean isLoaded() {
			return bLoad;
		}

		public int load() throws RException {

			if (loader == null) {
				return 0;
			}

			_fireLoadNodeAction(node);

			IREntryQueue entryQueue = node.getEntryQueue();

			final String stmtName = node.getReteType() == RReteType.NAME0 ? ((IRReteNode) node).getNamedName() : null;

			int stmtLen = node.getEntryLength();

			boolean pushEmptyNode = (entryQueue.size() == 0);
			int oldCacheStmtCount = this.cacheStmtCount;

			try {

				loader.load((stmt) -> {

					++readLines;

					if (stmt.size() != stmtLen) {
						throw new RException(
								String.format("Invalid stmt for node<%s:%d>: %s", stmtName, stmtLen, stmt));
					}

					if (stmt.getNamedName() != null) {

						if (stmtName == null) {
							stmt = RulpFactory.createList(stmt.iterator());

						} else if (!RuleUtil.equal(stmtName, stmt.getNamedName())) {
							stmt = RulpFactory.createNamedList(stmt.iterator(), stmtName);
						}

					} else if (stmtName != null) {
						stmt = RulpFactory.createNamedList(stmt.iterator(), stmtName);
					}

					if (RUpdateResult.isValidUpdate(_addStmt(node, stmt, DEFINE))) {
						XRModel.this.cacheUpdateCount++;
						this.cacheStmtCount++;
					}
				});

			} catch (IOException e) {

				if (RuleUtil.isModelTrace()) {
					e.printStackTrace();
				}

				throw new RException(e.toString());
			}

			if (pushEmptyNode && (oldCacheStmtCount != this.cacheStmtCount)) {
				cacheLastEntryId = entryQueue.getEntryAt(entryQueue.size() - 1).getEntryId();
			} else {
				cacheLastEntryId = -1;
			}

			this.loadCount++;
			this.bLoad = true;

			return cacheStmtCount;
		}

		public int save() throws RException, IOException {

			IREntryQueue rootQueue = node.getEntryQueue();

			int size = rootQueue.size();
			int lastEntryId = -1;

			ArrayList<IRList> stmtList = new ArrayList<>();

			for (int i = 0; i < size; ++i) {

				IRReteEntry entry = rootQueue.getEntryAt(i);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				stmtList.add(entry);
				lastEntryId = entry.getEntryId();
			}

			int stmtSize = stmtList.size();

			// not update
			if (this.isLoaded()) {

				if (stmtSize == cacheStmtCount && cacheLastEntryId == lastEntryId) {
					return 0;
				}

			} else {

				if (stmtSize == 0) {
					return 0;
				}
			}

			_fireSaveNodeAction(node);

			int saveLineCount = saver.save(stmtList);

			this.cacheLastEntryId = lastEntryId;
			this.cacheStmtCount = rootQueue.size();
			this.saveCount++;
			this.writeLines += saveLineCount;
			this.bLoad = true;

			return saveLineCount;
		}

		public void setLoader(IRStmtLoader loader) {
			this.loader = loader;
		}

		public void setSaver(IRStmtSaver saver) {
			this.saver = saver;
		}

	}

//	public static boolean RuleUtility.isModelTrace() = false;

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

	protected static int nodeExecId = 0;

	protected static Iterator<IRReteEntry> _statementsIterator(IREntryQueue queue, boolean reverse) {
		return !reverse ? new EntryQueueIterator(queue) : new EntryQueueReverseIterator(queue);
	}

	protected List<IRReteNode> activeQueue = new LinkedList<>();

	protected int activeUpdate = 0;

	protected int assuemeStatmentLevel = 0;

	protected boolean cacheEnable = false;

	protected int cacheUpdateCount = 0;

	protected final LinkedList<XRCacheWorker> cacheWorkerList = new LinkedList<>();

	protected final ModelConstraintUtil constraintUtil;

	protected final IREntryTable entryTable = new XREntryTable();

	protected Map<String, IRReteEntry> hasEntryCacheMap = new HashMap<>();

	protected IRInterpreter interpreter;

	protected XRRListener1Adapter<IRReteNode> loadNodeListener = null;

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

		String stmtUniqName = ReteUtil.uniqName(stmt);
		IRReteEntry oldEntry = rootNode.getEntryQueue().getStmt(stmtUniqName);

		/*******************************************************/
		// Insert entry
		// - Entry not exist
		// - or marked as "drop" (removed by entry table automatically)
		/*******************************************************/
		if (oldEntry == null || oldEntry.getStatus() == null) {

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
			if (!rootNode.addReteEntry(newEntry)) {
				entryTable.removeEntry(newEntry);
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

		IREntryQueue entryQueue = rootNode.getEntryQueue();

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
			entryTable.removeEntryReference(oldEntry, rootNode);
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
		if (cache == null || cache.isLoaded()) {
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

	protected IRReteNode _findRootNode(String namedName, int stmtLen) throws RException {
		if (namedName == null) {
			return nodeGraph.getRootNode(stmtLen);
		} else {
			return nodeGraph.getNamedNode(namedName, stmtLen);
		}
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

	protected XRCacheWorker _getCacheWorker(IRReteNode node) throws RException {

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {

			if (modelCachePath == null) {
				return null;
			}

			String nodeCachePath = FileUtil.toValidPath(modelCachePath)
					+ XRStmtFileDefaultCacher.getNodeCacheName(node);

			XRStmtFileDefaultCacher cacher = new XRStmtFileDefaultCacher(nodeCachePath, this.getInterpreter());

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

	protected boolean _hasUpdateNode() throws RException {
		_checkActiveNode();
		return updateQueue.hasNext();
	}

	protected boolean _isInClosingPhase() {
		return this.processingLevel > 0 && getRunState() == RRunState.Completed;
	}

	protected List<IRReteEntry> _listAllStatements(int mask, int count, boolean reverse) throws RException {

		LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();

		List<IRReteNode> nodes = RuleUtil.listNodes(nodeGraph, RReteType.ROOT0, RReteType.NAME0);
		if (reverse) {
			Collections.reverse(nodes);
		}

		for (IRReteNode rootNode : nodes) {
			_checkCache(rootNode);

			if (_listAllStatements(rootNode, matchedEntrys, mask, count, reverse)) {
				return matchedEntrys;
			}
		}

		return matchedEntrys;
	}

	protected boolean _listAllStatements(IRReteNode rootNode, LinkedList<IRReteEntry> matchedEntrys, int mask,
			int count, boolean reverse) throws RException {

		IREntryQueue rootNodeQueue = rootNode.getEntryQueue();

		Iterator<IRReteEntry> it = _statementsIterator(rootNodeQueue, reverse);
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

			matchedEntrys.add(entry);
			if (count > 0 && matchedEntrys.size() >= count) {
				return true;
			}
		}

		return false;
	}

	protected List<IRReteEntry> _listStatements(IRList filter, int statusMask, int limit, boolean reverse)
			throws RException {

		String namedName = filter.getNamedName();

		/******************************************************/
		// '(a ? ?)
		/******************************************************/
		{
			ArrayList<IRObject> filterObjs = null;
			XTempVarBuilder tmpVarBuilder = null;

			IRIterator<? extends IRObject> iter = filter.iterator();
			for (int stmtIndex = 0; iter.hasNext(); stmtIndex++) {

				IRObject obj = iter.next();

				if (ReteUtil.isAnyVar(obj)) {

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
				if (namedName == null) {
					filter = RulpFactory.createList(filterObjs);
				} else {
					filter = RulpFactory.createNamedList(filterObjs, namedName);
				}
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

				LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();

				int subLimit = limit;

				if (!reverse) {

					FIND_SUB: while (extendFilterObjs.size() <= nodeGraph.getMaxRootStmtLen()) {

						if (nodeGraph.findRootNode(extendFilterObjs.size()) != null) {

							IRList subFilter = RulpFactory.createNamedList(extendFilterObjs, namedName);

							List<IRReteEntry> subEntries = _listStatements(subFilter, statusMask, subLimit, reverse);
							matchedEntrys.addAll(subEntries);

							if (limit > 0) {
								subLimit = subLimit - subEntries.size();
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
						if (nodeGraph.findRootNode(extendFilterObjs.size()) != null) {
							filterList.add(new ArrayList<>(extendFilterObjs));
						}
						extendFilterObjs.add(tmpVarBuilder.next());
					}

					Collections.reverse(filterList);

					FIND_SUB: for (ArrayList<IRObject> extendFilter : filterList) {

						IRList subFilter = RulpFactory.createNamedList(extendFilter, namedName);

						List<IRReteEntry> subEntries = _listStatements(subFilter, statusMask, subLimit, reverse);
						matchedEntrys.addAll(subEntries);

						if (limit > 0) {
							subLimit = subLimit - subEntries.size();
							if (subLimit <= 0) {
								break FIND_SUB;
							}
						}
					}

				}

				return matchedEntrys;
			}

			IRReteNode namedNode = nodeGraph.findNamedNode(namedName);
			if (namedNode == null || extendFilterObjs.size() > namedNode.getEntryLength()) {
				return Collections.emptyList();
			}

			for (int i = anyIndex; i < namedNode.getEntryLength(); ++i) {
				extendFilterObjs.add(tmpVarBuilder.next());
			}

			filter = RulpFactory.createNamedList(extendFilterObjs, namedName);
		}

		/******************************************************/
		// Check named node
		/******************************************************/
		if (namedName != null) {

			IRReteNode namedNode = nodeGraph.findNamedNode(namedName);
			if (namedNode == null || namedNode.getEntryLength() != filter.size()) {
				return Collections.emptyList();
			}
		}

		/******************************************************/
		// Query uniq stmt
		/******************************************************/
		if (ReteUtil.getStmtVarCount(filter) == 0) {

			IRReteNode rootNode = _findRootNode(filter.getNamedName(), filter.size());
			_checkCache(rootNode);

			String uniqName = ReteUtil.uniqName(filter);
			IRReteEntry oldEntry = rootNode.getEntryQueue().getStmt(uniqName);
			if (oldEntry == null) {
				return Collections.emptyList();
			}

			LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();
			matchedEntrys.add(oldEntry);

			return matchedEntrys;
		}

		IRReteNode matchedNode = nodeGraph.getNodeByTree(filter);

		if (!RReteType.isRootType(matchedNode.getReteType()) && matchedNode.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid list node: " + matchedNode);
		}

		_checkCache(matchedNode);

		LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();
		IREntryQueue matchedNodeQueue = matchedNode.getEntryQueue();
		Iterator<IRReteEntry> it = _statementsIterator(matchedNodeQueue, reverse);

		boolean completed = false;

		/******************************************************/
		// Check cached entry
		/******************************************************/
		while (it.hasNext()) {

			IRReteEntry entry = it.next();
			if (entry == null || !ReteUtil.matchReteStatus(entry, statusMask)) {
				continue;
			}

			matchedEntrys.add(entry);
			if (limit > 0 && matchedEntrys.size() >= limit) {
				completed = true;
				break;
			}
		}

		/******************************************************/
		// Update all current node chain
		/******************************************************/
		if (!completed) {

			int oldQueryMatchCount = modelCounter.getQueryMatchCount();

			RuleUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node);
				if (update > 0) {
					modelCounter.incQueryMatchCount();
				}

				return false;
			});

			// clear old match entries if there is any updated
			if (modelCounter.getQueryMatchCount() > oldQueryMatchCount && reverse) {
				it = _statementsIterator(matchedNodeQueue, reverse);
				matchedEntrys.clear();
			}

			while (it.hasNext()) {

				IRReteEntry entry = it.next();

				if (entry == null || !ReteUtil.matchReteStatus(entry, statusMask)) {
					continue;
				}

				matchedEntrys.add(entry);
				if (limit > 0 && matchedEntrys.size() >= limit) {
					break;
				}
			}
		}

		return matchedEntrys;
	}

	protected IRReteNode _nextUpdateNode() throws RException {
		_checkActiveNode();
		return updateQueue.pop();
	}

	protected void _queryCond(IRResultQueue objQueue, IRReteNode queryNode, final int limit) throws RException {

		int queryEntryIndex = 0;

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

		if (OptimizeUtil.OPT_RULE) {
			Pair<IRList, IRList> rst = OptimizeUtil.optimizeRule(condList, actionList, this.getInterpreter(),
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
		modelRuleExecutedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addRuleFailedListener(IRListener1<IRRule> listener) {
		modelRuleFailedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addSaveNodeListener(IRListener1<IRReteNode> listener) {

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

		stmtListenUpdater.addStatementListener(this.findNode(condList), listener);
	}

	@Override
	public void addUpdateNode(IRReteNode node) throws RException {

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

		int smtGcCount = 0;
		int alpahGcCount = 0;
		int betaGcCount = 0;
		int exprGcCount = 0;
		int ruleGcCount = 0;

		LinkedList<IRReteNode> gcQueue = new LinkedList<>(nodeGraph.listNodes(RReteType.ROOT0));

		while (!gcQueue.isEmpty()) {

			IRReteNode node = gcQueue.pop();
			int gcCount = node.doGC();
			switch (node.getReteType()) {
			case ALPH0:
			case ALPH1:
//			case ALPH2:
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

			gcQueue.addAll(node.getChildNodes());
		}

		entryTable.doGC();

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("==> GC: rs=%d, ra=%d, rb=%d, re=%d, rr=%d", smtGcCount, alpahGcCount,
					betaGcCount, exprGcCount, ruleGcCount));
		}

		return smtGcCount + alpahGcCount + betaGcCount + exprGcCount + ruleGcCount;
	}

	@Override
	public int execute(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> " + node.toString());
		}

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

		// Has any stmt
		if (filter == null) {

			for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
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

		String uniqName = ReteUtil.uniqName(filter);
		IRReteEntry cacheEntry = hasEntryCacheMap.get(uniqName);
		if (cacheEntry != null) {
			if (cacheEntry.isDroped()) {
				hasEntryCacheMap.remove(uniqName);
			} else {
				return true;
			}
		}

		List<IRReteEntry> entrys = _listStatements(filter, 0, 1, false);
		if (entrys == null || entrys.isEmpty()) {
			return false;
		}

		hasEntryCacheMap.put(uniqName, entrys.get(0));
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
		RuleUtil.createModelVar(this, V_M_RBS_INIT, RulpFactory.createBoolean(false));
		RuleUtil.createModelVar(this, V_M_CST_INIT, RulpFactory.createBoolean(false));
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
	public List<IRReteEntry> listStatements(IRList filter, int statusMask, int limit, boolean reverse)
			throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> listStatements: " + filter + ", " + statusMask + ", " + limit + ", " + reverse);
		}

		if (filter == null) {
			return _listAllStatements(statusMask, limit, reverse);
		}

		return _listStatements(filter, statusMask, limit, reverse);
	}

	@Override
	public void query(IRResultQueue resultQueue, IRList condList, int limit) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> query: cond=" + condList + ", limit=" + limit);
		}

		/*****************************************************/
		// Does not support query when running
		/*****************************************************/
		if (processingLevel > 0) {
			throw new RException("Can't query, the model is running");
		}

		IRReteNode queryNode = this.findNode(condList);
		_queryCond(resultQueue, queryNode, limit);
	}

	@Override
	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException {

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
	public List<? extends IRList> removeStatement(IRList filter) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> dropStatement: " + filter);
		}

		List<IRReteEntry> dropStmts = new ArrayList<>();

		for (IRReteEntry entry : _listStatements(filter, 0, 0, false)) {

			if (entry != null && !entry.isDroped()) {

				entryTable.removeEntry(entry);
				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + entry);
				}

				dropStmts.add(entry);
			}
		}

		return dropStmts;
	}

	@Override
	public int save() throws RException {

		int totalSaveLines = 0;

		if (cacheUpdateCount == 0) {
			return 0;
		}

		try {

			for (IRReteNode node : nodeGraph.listNodes(RReteType.ROOT0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRReteNode) node);
				if (cacheWorker == null) {
					continue;
				}

				totalSaveLines += cacheWorker.save();
			}

			for (IRReteNode node : nodeGraph.listNodes(RReteType.NAME0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRReteNode) node);
				if (cacheWorker == null) {
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
		}
	}

	@Override
	public void setModelCachePath(String cachePath) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setModelCachePath: " + cachePath);
		}

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
		}
	}

	@Override
	public boolean tryAddStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> tryAddStatement: " + stmt);
		}

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
