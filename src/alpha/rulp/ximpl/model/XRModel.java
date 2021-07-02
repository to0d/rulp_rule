package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.RETE_PRIORITY_DEFAULT;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_INACTIVE;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_MAXIMUM;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MAX;
import static alpha.rulp.rule.Constant.RETE_PRIORITY_PARTIAL_MIN;
import static alpha.rulp.rule.Constant.V_M_STATE;
import static alpha.rulp.rule.RReteStatus.ASSUMED;
import static alpha.rulp.rule.RReteStatus.DEFINED;
import static alpha.rulp.rule.RReteStatus.REMOVED;
import static alpha.rulp.rule.RRunState.Completed;
import static alpha.rulp.rule.RRunState.Failed;
import static alpha.rulp.rule.RRunState.Halting;
import static alpha.rulp.rule.RRunState.Partial;
import static alpha.rulp.rule.RRunState.Runnable;
import static alpha.rulp.rule.RRunState.Running;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import alpha.rulp.lang.IRClass;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRFrameEntry;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModelCounter;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.IRRListener2;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRTransaction;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.rule.RRunState;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.MatchTree;
import alpha.rulp.utils.NodeUtil;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.Pair;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.StringUtil;
import alpha.rulp.utils.XRRListener1Adapter;
import alpha.rulp.utils.XRRListener2Adapter;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.cache.XRStmtFileCacher;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.XREntryTable;
import alpha.rulp.ximpl.node.IRNamedNode;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRReteNode;
import alpha.rulp.ximpl.node.IRRootNode;
import alpha.rulp.ximpl.node.RReteType;
import alpha.rulp.ximpl.node.XRNodeGraph;
import alpha.rulp.ximpl.node.XTempVarBuilder;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public class XRModel extends AbsRInstance implements IRModel {

	static class QuerySourceEntry {

		IRReteNode fromNode;
		IRReteNode sourceNode;

		public QuerySourceEntry(IRReteNode fromNode, IRReteNode sourceNode) {
			super();
			this.fromNode = fromNode;
			this.sourceNode = sourceNode;
		}
	}

	static class QuerySourceInfo {

		public IRReteNode node;
		public int partialRecoveryPriority = -1;
	}

	static class UniqObjQueue {

		private IRVar[] _vars;

		private IRList condList;

		private IRModel model;

		private IRFrame queryFrame;

		private IRObject rstExpr;

		private ArrayList<IRObject> rstList = new ArrayList<>();

		private Set<String> uniqNames = new HashSet<>();

		public UniqObjQueue(IRModel model, IRObject rstExpr, IRList condList) {
			super();
			this.model = model;
			this.rstExpr = rstExpr;
			this.condList = condList;
		}

		public boolean addEntry(IRReteEntry entry) throws RException {

			if (entry == null || entry.isDroped()) {
				return false;
			}

			/******************************************************/
			// Update variable value
			/******************************************************/
			for (int j = 0; j < getVars().length; ++j) {
				IRVar var = _vars[j];
				if (var != null) {
					var.setValue(entry.get(j));
				}
			}

			IRObject rstObj = model.getInterpreter().compute(getQueryFrame(), rstExpr);
			String uniqName = ReteUtil.uniqName(rstObj);
			if (uniqNames.contains(uniqName)) {
				return false;
			}

			uniqNames.add(uniqName);
			rstList.add(rstObj);
			return true;
		}

		public IRFrame getQueryFrame() throws RException {

			if (queryFrame == null) {
				queryFrame = RulpFactory.createFrame(model.getModelFrame(), "QUERY");
				RuleUtil.setDefaultModel(queryFrame, model);
			}

			return queryFrame;
		}

		public List<? extends IRObject> getRstList() {
			return rstList;
		}

		public IRVar[] getVars() throws RException {

			if (_vars == null) {

				/******************************************************/
				// Build var list
				/******************************************************/
				List<IRList> matchStmtList = ReteUtil.toCondList(condList, model.getNodeGraph());
				IRList matchTree = MatchTree.build(matchStmtList);
				IRObject[] varEntry = ReteUtil._varEntry(ReteUtil.buildTreeVarList(matchTree));
				_vars = new IRVar[varEntry.length];

				for (int i = 0; i < varEntry.length; ++i) {
					IRObject obj = varEntry[i];
					if (obj != null) {
						_vars[i] = getQueryFrame().addVar(RulpUtil.asAtom(obj).getName());
					}
				}
			}

			return _vars;
		}

		public int size() {
			return rstList.size();
		}
	}

	class XRCacheWorker implements IRCacheWorker {

		private boolean bLoad = false;

		private IRObject cacheKey;

		private int cacheLastEntryId = 0;

		private int cacheStmtCount = 0;

		private int loadCount = 0;

		private IRStmtLoader loader;

		private IRRootNode node;

		private int readLines = 0;

		private int saveCount = 0;

		private IRStmtSaver saver;

		private int writeLines = 0;

		public XRCacheWorker(IRRootNode node) {
			super();
			this.node = node;
		}

		public IRObject getCacheKey() {
			return cacheKey;
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

			_fireLoadNodeAction(node, cacheKey);

			IREntryQueue entryQueue = node.getEntryQueue();

			String stmtName = null;
			if (node.getReteType() == RReteType.NAME0) {
				stmtName = ((IRNamedNode) node).getNamedName();
			}

			int stmtLen = node.getEntryLength();

			boolean pushEmptyNode = (entryQueue.size() == 0);

			int stmtCount = 0;

			try {

				IRIterator<? extends IRList> iterator = loader.load(cacheKey);
				while (iterator.hasNext()) {

					IRList stmt = iterator.next();

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

					if (node.addStmt(stmt, DEFINED)) {
						cacheUpdateCount++;
						stmtCount++;
					}
				}

			} catch (IOException e) {

				if (RuleUtil.isModelTrace()) {
					e.printStackTrace();
				}

				throw new RException(e.toString());
			}

			if (pushEmptyNode && stmtCount > 0) {
				cacheLastEntryId = entryQueue.getEntryAt(entryQueue.size() - 1).getEntryId();
			} else {
				cacheLastEntryId = -1;
			}

			this.loadCount++;
			this.cacheStmtCount += stmtCount;
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

			_fireSaveNodeAction(node, cacheKey);

			int saveLineCount = saver.save(stmtList, cacheKey);

			this.cacheLastEntryId = lastEntryId;
			this.cacheStmtCount = rootQueue.size();
			this.saveCount++;
			this.writeLines += saveLineCount;
			this.bLoad = true;

			return saveLineCount;
		}

		public void setCacheKey(IRObject cacheKey) {
			this.cacheKey = cacheKey;
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

//	public static boolean RuleUtility.isModelTrace() = false;

	protected static int nodeExecId = 0;

	protected boolean cacheEnable = false;

	private int cacheUpdateCount = 0;

	protected final LinkedList<XRCacheWorker> cacheWorkerList = new LinkedList<>();

	protected IREntryTable entryTable = new XREntryTable();

	protected Map<String, IRReteEntry> hasEntryCacheMap = new HashMap<>();

	protected IRInterpreter interpreter;

	protected boolean isProcessing = false;

	protected String modelCachePath = null;

	protected XRRModelCounter modelCounter = new XRRModelCounter();

	protected IRFrame modelFrame;

	protected int modelPriority = RETE_PRIORITY_DEFAULT;

	protected final XRRListener1Adapter<IRRule> modelRuleExecutedListenerDispatcher = new XRRListener1Adapter<>();

	protected final XRRListener1Adapter<IRRule> modelRuleFailedListenerDispatcher = new XRRListener1Adapter<>();

	protected XRRListener2Adapter<IRReteNode, IRObject> saveNodeListener = null;

	protected XRRListener2Adapter<IRReteNode, IRObject> loadNodeListener = null;

	protected RRunState modelRunState = RRunState.Completed; // default

	protected IRVar modelStatsVar;

	protected boolean needRestart = false;

	protected RNodeContext nodeContext = null;

	protected IRNodeGraph nodeGraph = new XRNodeGraph(this);

	protected final LinkedList<IRReteNode> restartingNodeList = new LinkedList<>();

	protected final XRStmtListenUpdater stmtListenUpdater = new XRStmtListenUpdater();

	protected int stmtMaxIndex = 0;

	protected IRTransaction transaction = null;

	protected final XRUpdateQueue updateQueue = new XRUpdateQueue();

	public XRModel(String modelName, IRClass rclass, IRFrame frame) throws RException {
		super(rclass, modelName, frame);
	}

	protected int _addReteEntry(IRList stmt, RReteStatus toStatus) throws RException {

		if (!ReteUtil.isReteStmtNoVar(stmt)) {
			throw new RException("not support stmt: " + stmt);
		}

		IRRootNode rootNode;
		String namedName = stmt.getNamedName();
		if (namedName == null) {
			rootNode = nodeGraph.getRootNode(stmt.size());
		} else {
			rootNode = nodeGraph.getNamedNode(namedName, stmt.size());
		}

		_checkCache(rootNode);

		return _addReteEntry(rootNode, stmt, toStatus);
	}

	protected int _addReteEntry(IRRootNode rootNode, IRList stmt, RReteStatus toStatus) throws RException {

//		XREntryQueueRootStmtList rootQueue = (XREntryQueueRootStmtList) rootNode.getEntryQueue();

		// there is no any update
		if (!rootNode.addStmt(stmt, toStatus)) {

			if (this.nodeContext != null) {
				this.nodeContext.tryAddStmt++;
			}

			return 0;
		}

		// new stmt added or stats updated
		if (this.nodeContext != null) {
			this.nodeContext.actualAddStmt++;
			this.nodeContext.tryAddStmt++;
		}

		cacheUpdateCount++;
		addUpdateNode(rootNode);
		return 1;
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

	protected void _fireLoadNodeAction(IRReteNode node, IRObject cacheKey) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> loadNode: " + ", node=" + node + ", key=" + cacheKey);
		}

		if (loadNodeListener == null) {
			return;
		}

		loadNodeListener.doAction(node, cacheKey);
	}

	protected void _fireSaveNodeAction(IRReteNode node, IRObject cacheKey) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> saveNode: " + ", node=" + node + ", key=" + cacheKey);
		}

		if (saveNodeListener == null) {
			return;
		}

		saveNodeListener.doAction(node, cacheKey);
	}

	protected XRCacheWorker _getCacheWorker(IRRootNode node) throws RException {

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {

			if (modelCachePath == null) {
				return null;
			}

			String nodeCachePath = FileUtil.toValidPath(modelCachePath) + XRStmtFileCacher.getNodeCacheName(node);
			XRStmtFileCacher cacher = new XRStmtFileCacher(this.getInterpreter());

			cache = _setNodeCache(node, cacher, cacher, RulpFactory.createString(nodeCachePath));
		}

		return cache;
	}

	protected RReteStatus _getNewStmtStatus() {

		RReteStatus status = DEFINED;

		if (nodeContext != null) {
			status = nodeContext.getNewStmtStatus();
		}

		return status;
	}

	protected boolean _isInClosingPhase() {
		return this.isProcessing && getRunState() == RRunState.Completed;
	}

	protected List<IRReteEntry> _listAllStatements(int mask, int count) throws RException {

		LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();

		for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
			_checkCache(rootNode);
			if (_listAllStatements(rootNode, matchedEntrys, mask, count)) {
				return matchedEntrys;
			}
		}

		for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.NAME0)) {
			_checkCache(rootNode);
			if (_listAllStatements(rootNode, matchedEntrys, mask, count)) {
				return matchedEntrys;
			}
		}

		return matchedEntrys;
	}

	protected boolean _listAllStatements(IRReteNode rootNode, LinkedList<IRReteEntry> matchedEntrys, int mask,
			int count) throws RException {

		IREntryQueue rootNodeQueue = rootNode.getEntryQueue();
		int maxCount = rootNodeQueue.size();

		for (int i = 0; i < maxCount; ++i) {

			IRReteEntry entry = rootNodeQueue.getEntryAt(i);
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

	protected List<IRReteEntry> _listStatements(IRList filter, int requiredMask, int limit) throws RException {

		IRReteNode matchedNode = nodeGraph.getNodeByTree(filter);

		if (!RReteType.isRootType(matchedNode.getReteType()) && matchedNode.getReteType() != RReteType.ALPH0) {
			throw new RException("Invalid list node: " + matchedNode);
		}

		_checkCache(matchedNode);

		LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();
		int fromIndex = 0;
		boolean completed = false;

		/******************************************************/
		// Check cached entry
		/******************************************************/
		{
			IREntryQueue matchedNodeQueue = matchedNode.getEntryQueue();

			int maxCount = matchedNodeQueue.size();
			for (; fromIndex < maxCount; ++fromIndex) {

				IRReteEntry entry = matchedNodeQueue.getEntryAt(fromIndex);
				if (entry == null || !ReteUtil.matchReteStatus(entry, requiredMask)) {
					continue;
				}

				matchedEntrys.add(entry);
				if (limit > 0 && matchedEntrys.size() >= limit) {
					completed = true;
					break;
				}
			}
		}

		/******************************************************/
		// Update all current node chain
		/******************************************************/
		if (!completed) {

			NodeUtil.travelReteParentNodeByPostorder(matchedNode, (node) -> {

				int update = execute(node);
				if (update > 0) {
					modelCounter.queryMatchCount++;
				}

				return false;
			});

			IREntryQueue matchedNodeQueue = matchedNode.getEntryQueue();
			int maxCount = matchedNodeQueue.size();
			for (; fromIndex < maxCount; ++fromIndex) {

				IRReteEntry entry = matchedNodeQueue.getEntryAt(fromIndex);
				if (entry == null || !ReteUtil.matchReteStatus(entry, requiredMask)) {
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

	protected List<? extends IRObject> _queryCond(IRObject rstExpr, IRList condList, final int limit)
			throws RException {

		IRReteNode queryNode = this.findNode(condList);
		UniqObjQueue objQueue = new UniqObjQueue(this, rstExpr, condList);

		int queryEntryIndex = 0;

		boolean isRootMode = RReteType.isRootType(queryNode.getReteType());

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
					return objQueue.getRstList();
				}
			}
		}

		/******************************************************/
		// Build source graph
		/******************************************************/
		Map<IRReteNode, QuerySourceInfo> sourceMap = new HashMap<>();
		Set<IRReteNode> sourceNodes = new HashSet<>();
		List<QuerySourceInfo> changeList = new LinkedList<>();

		LinkedList<QuerySourceEntry> visitStack = new LinkedList<>();
		visitStack.add(new QuerySourceEntry(null, queryNode));

		while (!visitStack.isEmpty()) {

			QuerySourceEntry entry = visitStack.pop();
			IRReteNode fromNode = entry.fromNode;
			IRReteNode sourceNode = entry.sourceNode;

			if (sourceNode.getPriority() < RETE_PRIORITY_INACTIVE) {
				continue;
			}

			if (!isRootMode && RReteType.isRootType(sourceNode.getReteType())) {
				continue;
			}

			int new_priority = RETE_PRIORITY_PARTIAL_MAX;
			if (sourceNode != queryNode) {
				new_priority = Math.min(sourceNode.getPriority(), fromNode.getPriority()) - 1;
				if (new_priority < RETE_PRIORITY_PARTIAL_MIN) {
					new_priority = RETE_PRIORITY_PARTIAL_MIN;
				}
			}

			// source node is not visited before
			QuerySourceInfo info = sourceMap.get(sourceNode);
			if (info == null) {
				info = new QuerySourceInfo();
				info.node = sourceNode;
				info.partialRecoveryPriority = sourceNode.getPriority();
				sourceNode.setPriority(new_priority);
				changeList.add(info);
			}

			// ignore visited node
			if (sourceNodes.contains(sourceNode)) {
				continue;
			}

			updateQueue.push(sourceNode);

			// add all source nodes
			sourceNodes.add(sourceNode);

			if (sourceNode.getParentNodes() != null) {
				for (IRReteNode newSrcNode : sourceNode.getParentNodes()) {
					visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
				}
			}

			for (IRReteNode newSrcNode : nodeGraph.getBindFromNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}

			for (IRReteNode newSrcNode : nodeGraph.listSourceNodes(sourceNode)) {
				visitStack.add(new QuerySourceEntry(sourceNode, newSrcNode));
			}
		}

		/******************************************************/
		// Invoke running
		/******************************************************/
		int oldModelPriority = this.modelPriority;
		this.modelPriority = RETE_PRIORITY_PARTIAL_MIN;
		this.isProcessing = true;
		this._setRunState(RRunState.Running);

		try {

			while (updateQueue.hasNext()) {

				IRReteNode node = updateQueue.pop();
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
									return objQueue.getRstList();
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

			this._setRunState(RRunState.Partial);
			return objQueue.getRstList();

		} finally {

			this.isProcessing = false;
			this.modelPriority = oldModelPriority;

			// recovery priority
			for (QuerySourceInfo changeInfo : changeList) {

				if (changeInfo.partialRecoveryPriority == -1) {
					throw new RException("partial recovery priority invalid: " + changeInfo.node);
				}

				// ignore dead node
				if (changeInfo.node.getPriority() >= 0) {
					changeInfo.node.setPriority(changeInfo.partialRecoveryPriority);
				}

				changeInfo.partialRecoveryPriority = -1;
			}
		}
	}

	protected XRCacheWorker _setNodeCache(IRRootNode node, IRStmtLoader loader, IRStmtSaver saver, IRObject cacheKey)
			throws RException {

		if (loader == null && saver == null) {
			throw new RException("null loader and saver");
		}

		XRCacheWorker cache = (XRCacheWorker) node.getCacheWorker();
		if (cache == null) {
			cache = new XRCacheWorker(node);
			node.setCacheWorker(cache);
			cacheWorkerList.add(cache);
		}

		cache.setLoader(loader);
		cache.setSaver(saver);
		cache.setCacheKey(cacheKey);
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
			++modelCounter.stateChangeCount;

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
		NodeUtil.travelReteParentNodeByPostorder(updateNode, (node) -> {
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
		NodeUtil.travelReteParentNodeByPostorder(updateNode, (node) -> {
			int c = execute(node);
			if (c > 0) {
				count.addAndGet(c);
			}
			return false;
		});

		return count.get();
	}

	@Override
	public IRRule addRule(String ruleName, IRList condList, IRList actionList) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println(String.format("==> addRule: %s, %s, %s", ruleName, condList, actionList));
		}

		if (OptimizeUtil.OPT_RULE) {
			Pair<IRList, IRList> rst = OptimizeUtil.optimizeRule(condList, actionList, this.getModelFrame());
			condList = rst.getKey();
			actionList = rst.getValue();
		}

		IRRule rule = nodeGraph.addRule(ruleName, condList, actionList, RETE_PRIORITY_DEFAULT);

		for (IRReteNode parentNode : rule.getParentNodes()) {

			NodeUtil.travelReteParentNodeByPostorder(parentNode, (node) -> {

				/******************************************************/
				// Check node update queue
				/******************************************************/
				if (node.getReteType() != RReteType.ROOT0 && !node.isInQueue()) {

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
	public void addRuleExecutedListener(IRRListener1<IRRule> listener) {
		modelRuleExecutedListenerDispatcher.addListener(listener);
	}

	@Override
	public void addRuleFailedListener(IRRListener1<IRRule> listener) {
		modelRuleFailedListenerDispatcher.addListener(listener);
	}

	@Override
	public int addStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStatement: " + stmt);
		}

		int actualAddStmt = _addReteEntry(stmt, _getNewStmtStatus());

		// active stmt listeners
		if (actualAddStmt > 0) {
			stmtListenUpdater.update(this);
		}

		return actualAddStmt;
	}

	@Override
	public void addStatementListener(IRList condList, IRRListener1<IRList> listener) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStmtListener: " + condList);
		}

		stmtListenUpdater.addStatementListener(this.findNode(condList), listener);
	}

	@Override
	public int addStatements(IRIterator<? extends IRList> stmtIterator) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStatements: ");
		}

		RReteStatus status = _getNewStmtStatus();

		int actualAddStmt = 0;
		while (stmtIterator.hasNext()) {

			IRList stmt = stmtIterator.next();
			if (RuleUtil.isModelTrace()) {
				System.out.println("\t(" + stmt + ")");
			}

			actualAddStmt += _addReteEntry(stmt, status);
		}

		// active stmt listeners
		if (actualAddStmt > 0) {
			stmtListenUpdater.update(this);
		}

		return actualAddStmt;
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
	public int assumeStatement(IRList stmt) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> assumeStatement: " + stmt);
		}

		return _addReteEntry(stmt, ASSUMED);
	}

	@Override
	public int assumeStatements(IRIterator<? extends IRList> stmtIterator) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> addStatements: ");
		}

		int count = 0;
		while (stmtIterator.hasNext()) {

			IRList stmt = stmtIterator.next();
			if (RuleUtil.isModelTrace()) {
				System.out.println("\t(" + stmt + ")");
			}

			count += _addReteEntry(stmt, ASSUMED);
		}

		return count;
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
			case ALPH2:
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

		for (IRReteEntry entry : listStatements(filter, 0, 0)) {

			if (entry != null && !entry.isDroped()) {

				entryTable.removeEntry(entry.getEntryId());
				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + entry);
				}

				dropStmts.add(entry);
			}
		}

		return dropStmts;
	}

	@Override
	public int execute(IRReteNode node) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> " + node.toString());
		}

		int oldQueryMatchCount = modelCounter.queryMatchCount;

		++modelCounter.nodeExecCount;

		int update = node.update();

		if (node.getReteType() == RReteType.RULE) {

			if (node.getRunState() == RRunState.Failed) {
				this.modelRuleFailedListenerDispatcher.doAction((IRRule) node);
			} else {
				this.modelRuleExecutedListenerDispatcher.doAction((IRRule) node);
			}
		}

		if (update > 0) {

			node.incExecCount(modelCounter.nodeExecCount);

			for (IRReteNode child : node.getChildNodes(true)) {
				if (child.getPriority() >= modelPriority) {
					updateQueue.push(child);
				}
			}

		} else {
			++modelCounter.nodeIdleCount;
		}

		if (modelCounter.queryMatchCount > oldQueryMatchCount) {
			node.addQueryMatchCount(modelCounter.queryMatchCount - oldQueryMatchCount);
		}

		return update;
	}

	@Override
	public IRReteNode findNode(IRList condList) throws RException {
		return nodeGraph.getNodeByTree(condList);
	}

	@Override
	public String getCachePath() {
		return modelCachePath;
	}

	@Override
	public IRModelCounter getCounter() {

		return new IRModelCounter() {

			@Override
			public IRModel getModel() {
				return XRModel.this;
			}

			@Override
			public int getNodeExecuteCount() {
				return modelCounter.getNodeExecuteCount();
			}

			@Override
			public int getNodeIdleCount() {
				return modelCounter.getNodeIdleCount();
			}

//			@Override
//			public int getObjectCount() {
//				return allObjectList.size();
//			}

			@Override
			public int getProcessQueueMaxNodeCount() {
				return updateQueue.getMaxNodeCount();
			}

			@Override
			public int getQueryFetchCount() {

				int totalCount = 0;

				for (IRReteNode node : nodeGraph.getNodeMatrix().getAllNodes()) {
					totalCount += node.getEntryQueue().getQueryFetchCount();
				}

				return totalCount;
			}

			@Override
			public int getQueryMatchCount() {
				return modelCounter.getQueryMatchCount();
			}

			@Override
			public int getRuleCount() {
				return nodeGraph.listNodes(RReteType.RULE).size();
			}

			@Override
			public int getStateChangeCount() {
				return modelCounter.getStateChangeCount();
			}

			@Override
			public int getStatementCount() {

				int totalCount = 0;
				int nullCount = 0;
				int dropCount = 0;

				for (IRReteNode rootNode : nodeGraph.listNodes(RReteType.ROOT0)) {
					IREntryCounter rootEntryCounter = rootNode.getEntryQueue().getEntryCounter();
					totalCount += rootEntryCounter.getEntryTotalCount();
					nullCount += rootEntryCounter.getEntryNullCount();
					dropCount += rootEntryCounter.getEntryCount(REMOVED);
				}

				return totalCount - nullCount - dropCount;
			}

		};
	}

	@Override
	public IREntryTable getEntryTable() {
		return entryTable;
	}

	@Override
	public IRInterpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public IRFrame getModelFrame() {
		return this.modelFrame;
	}

//	@Override
//	public IRIterator<IRObject> listObjects() throws RException {
//
//		return new IRIterator<IRObject>() {
//
//			protected int index = 0;
//			protected ArrayList<IRObject> objList = allObjectList;
//			protected int size = allObjectList.size();
//
//			@Override
//			public boolean hasNext() throws RException {
//				return index < size;
//			}
//
//			@Override
//			public IRObject next() throws RException {
//				return objList.get(index++);
//			}
//		};
//	}

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
				modelStatsVar = RulpFactory.createVar(V_M_STATE);
				this.getModelFrame().setEntry(V_M_STATE, modelStatsVar);
				modelStatsVar.setValue(RRunState.toObject(this.getRunState()));
			}

			return modelStatsVar;

		default:

			IRFrameEntry varEntry = this.getModelFrame().getEntry(varName);
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
				int dropCount = rootEntryCounter.getEntryCount(REMOVED);
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

		List<IRReteEntry> entrys = listStatements(filter, 0, 1);
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
		this.modelFrame = RulpFactory.createFrame(frame, "MODEL");
		RulpUtil.incRef(modelFrame);
		RuleUtil.setDefaultModel(modelFrame, this);
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
	public Collection<? extends IRReteNode> listSourceNodes(IRList condList) throws RException {
		return this.nodeGraph.listSourceNodes(findNode(condList));
	}

	@Override
	public List<IRReteEntry> listStatements(IRList filter, int statusMask, int limit) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> listStatements: " + filter + ", " + statusMask + ", " + limit);
		}

		if (filter == null) {
			return _listAllStatements(statusMask, limit);
		}

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

				FIND_SUB: while (extendFilterObjs.size() <= nodeGraph.getMaxRootStmtLen()) {

					if (nodeGraph.findRootNode(extendFilterObjs.size()) != null) {

						IRList subFilter = RulpFactory.createNamedList(extendFilterObjs, namedName);

						List<IRReteEntry> subEntries = listStatements(subFilter, statusMask, subLimit);
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

				return matchedEntrys;
			}

			IRNamedNode namedNode = nodeGraph.findNamedNode(namedName);
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

			IRNamedNode namedNode = nodeGraph.findNamedNode(namedName);
			if (namedNode == null || namedNode.getEntryLength() != filter.size()) {
				return Collections.emptyList();
			}
		}

		/******************************************************/
		// Query uniq stmt
		/******************************************************/
		if (ReteUtil.getStmtVarCount(filter) == 0) {

			IRRootNode rootNode = null;

			if (filter.getNamedName() == null) {
				rootNode = nodeGraph.getRootNode(filter.size());
			} else {
				rootNode = nodeGraph.getNamedNode(filter.getNamedName(), filter.size());
			}

			_checkCache(rootNode);

			String uniqName = ReteUtil.uniqName(filter);
//			XREntryQueueRootStmtList rootQueue = (XREntryQueueRootStmtList) rootNode.getEntryQueue();

			IRReteEntry oldEntry = rootNode.getStmt(uniqName);
			if (oldEntry == null) {
				return Collections.emptyList();
			}

			LinkedList<IRReteEntry> matchedEntrys = new LinkedList<>();
			matchedEntrys.add(oldEntry);

			return matchedEntrys;
		}

		return _listStatements(filter, statusMask, limit);
	}

	@Override
	public List<? extends IRObject> query(IRObject rstExpr, IRList condList, int limit) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> query: rst=" + rstExpr + ", cond=" + condList + ", limit=" + limit);
		}

		/*****************************************************/
		// Does not support query when running
		/*****************************************************/
		if (this.isProcessing) {
			throw new RException("Can't query, the model is running");
		}

		return _queryCond(rstExpr, condList, limit);
	}

	@Override
	public int save() throws RException {

		int totalSaveLines = 0;

		if (cacheUpdateCount == 0) {
			return 0;
		}

		try {

			for (IRReteNode node : nodeGraph.listNodes(RReteType.ROOT0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRRootNode) node);
				if (cacheWorker == null) {
					continue;
				}

				totalSaveLines += cacheWorker.save();
			}

			for (IRReteNode node : nodeGraph.listNodes(RReteType.NAME0)) {

				XRCacheWorker cacheWorker = _getCacheWorker((IRRootNode) node);
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

			IRRootNode node;
			if (nodeName == null) {
				node = this.nodeGraph.getRootNode(stmtLen);
			} else {
				node = this.nodeGraph.getNamedNode(nodeName, stmtLen);
			}

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
	public void setNodeCache(IRReteNode node, IRStmtLoader loader, IRStmtSaver saver, IRObject cacheKey)
			throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("==> setNodeCache: node=" + node + ", key=" + cacheKey);
		}

		if (node.getReteType() != RReteType.NAME0) {
			throw new RException("invalid node type: " + node);
		}

		_setNodeCache((IRRootNode) node, loader, saver, cacheKey);
	}

	@Override
	public void setNodeContext(RNodeContext nodeContext) {
		this.nodeContext = nodeContext;
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
		if (this.isProcessing) {

			RRunState state = this.getRunState();
			if (state == RRunState.Halting || state == RRunState.Runnable) {
				_setRunState(RRunState.Running);
			}

			// Let loop running in top level
			return 0;
		}

		int oldModelPriority = this.modelPriority;

		this.modelPriority = priority;
		this.isProcessing = true;
		this.needRestart = false;

//		boolean running = false;

		try {

			int runTimes = 0;
			int execTimes = 0;

			RUN: for (; runTimes == 0 || updateQueue.hasNext() || this.needRestart; ++runTimes) {

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

				IRReteNode node = updateQueue.pop();
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

		} finally {
			this.isProcessing = false;
			this.modelPriority = oldModelPriority;
		}
	}

	@Override
	public void addLoadNodeListener(IRRListener2<IRReteNode, IRObject> listener) {

		if (loadNodeListener == null) {
			loadNodeListener = new XRRListener2Adapter<>();
		}

		loadNodeListener.addListener(listener);
	}

	@Override
	public void addSaveNodeListener(IRRListener2<IRReteNode, IRObject> listener) {

		if (saveNodeListener == null) {
			saveNodeListener = new XRRListener2Adapter<>();
		}

		saveNodeListener.addListener(listener);
	}

//	@Override
//	public int updateStatements(IRList condList) throws RException {
//
//		if (RuleUtility.isModelTrace()) {
//			System.out.println("==> updateStatements: " + condList);
//		}
//
//		return _update(this.findNode(condList));
//	}
}
