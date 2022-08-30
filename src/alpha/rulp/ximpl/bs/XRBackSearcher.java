package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.F_B_AND;
import static alpha.rulp.lang.Constant.F_B_OR;
import static alpha.rulp.rule.Constant.A_BS_TRACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.entry.IREntryQueueUniq;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.model.XRModel;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBackSearcher {

	abstract class AbsBSNode implements IRBSNode {

		protected ArrayList<AbsBSNode> childNodes;

//		protected int curChildIndex = 0;

		protected int indexInParent = -1;

		protected int level = -1;

		protected int nodeId;

		protected String nodeName;

		protected AbsBSNode parentNode;

		protected BSStats status;

		public AbsBSNode(int nodeId, String nodeName) {
			super();
			this.nodeId = nodeId;
			this.nodeName = nodeName;
		}

		public void addChild(AbsBSNode child) {

			if (trace) {
				_outln(this, String.format("add child, type=%s, name=%s", child.getType(), child.nodeName));
			}

			if (this.childNodes == null) {
				this.childNodes = new ArrayList<>();
			}

			child.parentNode = this;
			child.indexInParent = this.getChildCount();

			this.childNodes.add(child);
		}

		@Override
		public IRBSNode getChild(int index) {
			return this.childNodes == null || index < 0 || index >= this.childNodes.size() ? null
					: this.childNodes.get(index);
		}

		public int getChildCount() {
			return this.childNodes == null ? 0 : this.childNodes.size();
		}

		public int getIndexInParent() {
			return indexInParent;
		}

		public int getLevel() {

			if (level == -1) {
				if (this.parentNode == null) {
					level = 0;
				} else {
					level = this.parentNode.getLevel() + 1;
				}
			}

			return level;
		}

		public String getNodeName() {
			return nodeName;
		}

		public IRBSNode getParentNode() {
			return parentNode;
		}

		public BSStats getStatus() {
			return status;
		}

		public void setStatus(BSStats status) {
			this.status = status;
		}

	}

	class BSNodeAnd extends AbsBSNode {

		protected IAction action;

		protected IRBSNode failChild = null;

		protected boolean rst;

		protected SourceNode sourceNode;

		protected IRList stmt;

		public BSNodeAnd(int nodeId, String nodeName) {
			super(nodeId, nodeName);
		}

		public IRList buildResultTree(boolean explain) throws RException {

			if (!this.isSucc()) {
				return RulpFactory.emptyConstList();
			}

			if (!explain) {
				return RulpFactory.createList(stmt);
			}

			ArrayList<IRObject> treeList = new ArrayList<>();
			treeList.add(RulpFactory.createString(sourceNode.rule.getRuleName()));

			if (childNodes != null) {
				for (AbsBSNode child : childNodes) {
					treeList.add(child.buildResultTree(explain));
				}
			}

			return RulpFactory.createExpression(treeList);
		}

		public void complete() throws RException {

			// need trigger all related rete-node
			if (execute(listAllChildAndStmts())) {
				this.rst = true;
				return;
			}

			AbsBSNode lastChild = this.childNodes.get(childNodes.size() - 1);
			if (lastChild.getType() == BSType.QUERY) {

				boolean hasMore = ((BSNodeQuery) lastChild).hasMore();

				if (trace) {
					_outln(this, String.format("complete-query, hasMore=%s", "" + hasMore));
				}

				if (!hasMore) {
					this.rst = false;
					return;
				}
			}

			this.sourceNode.rule.start(-1, -1);
			this.rst = _hasStmt(this, this.stmt);
		}

		public boolean execute(List<IRList> childStmts) throws RException {

			ArrayList<IRReteNode> rootNodes = new ArrayList<>();
			ArrayList<BSStmtIndexs> BSStmtIndexsList = new ArrayList<>();

			Map<IRReteNode, BSStmtIndexs> stmtIndexMap = new HashMap<>();

			for (IRList childStmt : childStmts) {

				IRReteNode rootNode = graph.findRootNode(childStmt.getNamedName(), childStmt.size());
				if (!rootNodes.contains(rootNode)) {
					rootNodes.add(rootNode);
				}

				IREntryQueueUniq uniqQueue = ((IREntryQueueUniq) rootNode.getEntryQueue());
				int stmtIndex = uniqQueue.getStmtIndex(ReteUtil.uniqName(childStmt));

				BSStmtIndexs si = stmtIndexMap.get(rootNode);
				if (si == null) {
					si = new BSStmtIndexs();
					si.rootNode = rootNode;
					stmtIndexMap.put(rootNode, si);
					BSStmtIndexsList.add(si);
				}

				si.addIndex(stmtIndex);
			}

			for (BSStmtIndexs si : BSStmtIndexsList) {

				// Get the max visit index
				int childMaxVisitIndex = ReteUtil.findChildMaxVisitIndex(si.rootNode);

				// All statements are already passed to child nodes
				if (childMaxVisitIndex > si.maxStmtIndex) {
					continue;
				}

				for (int index : si.stmtIndexs) {
					if (index >= childMaxVisitIndex) {
						if (si.relocatedStmtIndexs == null) {
							si.relocatedStmtIndexs = new ArrayList<>();
						}
						si.relocatedStmtIndexs.add(index);
					}
				}

				if (si.relocatedStmtIndexs != null) {
					XRBackSearcher.this.bscOpRelocate += si.relocatedStmtIndexs.size();
					((IREntryQueueUniq) si.rootNode.getEntryQueue()).relocate(childMaxVisitIndex,
							si.relocatedStmtIndexs);
				}
			}

			this.sourceNode.rule.start(-1, -1);

			for (BSStmtIndexs si : BSStmtIndexsList) {
				if (si.relocatedStmtIndexs != null) {
					((IREntryQueueUniq) si.rootNode.getEntryQueue()).relocate(-1, null);
				}
			}

			return _hasStmt(this, this.stmt);
		}

		public String getStatusString() {
			return String.format("fail-child=%s", failChild == null ? "null" : failChild.getNodeName());
		}

		@Override
		public BSType getType() {
			return BSType.AND;
		}

		public void init() throws RException {

			IActionSimpleStmt addAction = (IActionSimpleStmt) action;
			int[] inheritIndexs = addAction.getInheritIndexs();
			int inheritSize = inheritIndexs.length;
			if (inheritSize != stmt.size()) {
				throw new RException("invalid action: " + addAction);
			}

			Map<String, IRObject> varValueMap = new HashMap<>();

			for (int i = 0; i < inheritSize; ++i) {
				int inheritIndex = inheritIndexs[i];
				if (inheritIndex != -1) {

					IRObject var = sourceNode.rule.getVarEntry()[inheritIndex];
					if (var == null) {
						throw new RException("invalid inheritIndex: " + inheritIndex);
					}

					varValueMap.put(RulpUtil.toString(var), stmt.get(i));
				}
			}

			ArrayList<IRList> queryStmtList = null;

			for (IRList list : sourceNode.rule.getMatchStmtList()) {

				if (ReteUtil.isAlphaMatchTree(list)) {

					IRList newStmt = (IRList) RuntimeUtil.rebuild(list, varValueMap);

					if (ReteUtil.isReteStmtNoVar(newStmt)) {

						// The and should fail once circular proof be found
						if (_isCircularProof(newStmt)) {

							if (trace) {
								_outln(this, String.format("circular proof found, stmt=%s, return false", newStmt));
							}

							this.status = BSStats.COMPLETE;
							this.rst = false;
							bscCircularProof++;

							return;
						}

						this.addChild(_newOrNode(newStmt));
					}
					// '(?a p b) should be used in query node
					else {

						if (queryStmtList == null) {
							queryStmtList = new ArrayList<>();
						}

						queryStmtList.add(newStmt);
					}
				}
			}

			if (queryStmtList != null) {
				this.addChild(_newQueryNode(queryStmtList));
			}

			// no child
			if (this.getChildCount() == 0) {

				if (trace) {
					_outln(this, "not child, return false");
				}

				this.status = BSStats.COMPLETE;
				this.rst = false;
				return;
			}

			this.status = BSStats.PROCESS;

		}

		@Override
		public boolean isSucc() {
			return this.rst;
		}

		public List<IRList> listAllChildAndStmts() {

			List<IRList> stmts = null;

			for (AbsBSNode childNode : childNodes) {
				if (childNode.getType() == BSType.OR) {

					BSNodeOr orNode = (BSNodeOr) childNode;
					if (stmts == null) {
						stmts = new ArrayList<>();
					}
					stmts.add(orNode.getStmt());
				}
			}

			if (stmts == null) {
				stmts = Collections.emptyList();
			}

			return stmts;
		}

		public void process(IRBSNode lastNode) throws RException {

			// (and false xx xx) ==> false
			if (!lastNode.isSucc()) {
				this.failChild = lastNode;
				this.status = BSStats.COMPLETE;
				this.rst = false;
				return;
			}

			// No child need update, mark the parent's result is true
			if ((lastNode.getIndexInParent() + 1) >= getChildCount()) {
				this.status = BSStats.COMPLETE;
				this.rst = true;
				return;
			}
		}

		public String toString() {
			return String.format("stmt=%s, rule=%s, action=%s(%d), type=%s, status=%s", stmt, sourceNode.rule,
					action.toString(), action.getIndex(), "" + this.getType(), "" + this.status);
		}

	}

	class BSNodeOr extends AbsBSNode {

		protected boolean rst;

		protected IRList stmt;

		protected IRBSNode succChild = null;

		public BSNodeOr(int nodeId, String nodeName) {
			super(nodeId, nodeName);
		}

		public IRList buildResultTree(boolean explain) throws RException {

			if (!this.isSucc()) {
				return RulpFactory.emptyConstList();
			}

			if (!explain || succChild == null) {
				return RulpFactory.createList(stmt);
			}

			return RulpFactory.createList(stmt, succChild.buildResultTree(explain));
		}

		public void complete() throws RException {

			// re-check statement in case it was deleted
			if (!_hasStmt(this, this.stmt)) {
				this.rst = false;
			}
		}

		public String getStatusString() {
			return String.format("succ-child=%s", succChild == null ? "null" : succChild.getNodeName());
		}

		public IRList getStmt() {
			return stmt;
		}

		@Override
		public BSType getType() {
			return BSType.OR;
		}

		public void init() throws RException {

			if (_hasStmt(this, this.stmt)) {
				this.status = BSStats.COMPLETE;
				this.rst = true;
				return;
			}

			ArrayList<SourceNode> sourceNodes = new ArrayList<>(graph.listSourceNodes(stmt));
			Collections.sort(sourceNodes, (s1, s2) -> {
				return s1.rule.getRuleName().compareTo(s2.rule.getRuleName());
			});

			for (SourceNode sn : sourceNodes) {

				for (IAction action : sn.actionList) {

					if (action.getActionType() != RActionType.ADD) {
						continue;
					}

					this.addChild(_newAndNode(stmt, sn, action));
				}
			}

			// no child
			if (this.getChildCount() == 0) {

				if (trace) {
					_outln(this, "not child, return false");
				}

				this.status = BSStats.COMPLETE;
				this.rst = false;
				return;
			}

			this.status = BSStats.PROCESS;

		}

		@Override
		public boolean isSucc() {
			return this.rst;
		}

		public void process(IRBSNode lastNode) throws RException {

			// (or true xx xx) ==> (true)
			if (lastNode.isSucc()) {
				this.succChild = lastNode;
				this.status = BSStats.COMPLETE;
				this.rst = true;
				return;
			}

			if ((lastNode.getIndexInParent() + 1) >= getChildCount()) {
				this.status = BSStats.COMPLETE;
				this.rst = false;
				return;
			}
		}

		public String toString() {
			return String.format("stmt=%s, type=%s, status=%s", "" + stmt, "" + this.getType(), "" + this.status);
		}

	}

	class BSNodeQuery extends AbsBSNode implements IREntryAction {

		protected boolean queryBackward = false;

		protected boolean queryForward = false;

		protected Set<String> queryResultEntrySet = new HashSet<>();

		protected IRList queryReteNodeTree;

//		protected List<IRList> queryStmtList;

		public BSNodeQuery(int nodeId, String nodeName) {
			super(nodeId, nodeName);
		}

		@Override
		public boolean addEntry(IRReteEntry entry) throws RException {

			if (entry != null && !entry.isDroped()) {
				queryResultEntrySet.add(ReteUtil.uniqName(entry));
			}

			return true;
		}

		public IRList buildResultTree(boolean explain) throws RException {
			return queryReteNodeTree;
		}

		@Override
		public void complete() throws RException {

		}

		public String getStatusString() {
			return String.format("forward=%s, backward=%s, query=%d", queryForward, queryBackward,
					queryResultEntrySet.size());
		}

		@Override
		public BSType getType() {
			return BSType.QUERY;
		}

		public boolean hasMore() throws RException {

			if (queryBackward) {
				return false;
			}

			int oldSize = queryResultEntrySet.size();
			model.query(this, queryReteNodeTree, -1, true);
			queryBackward = true;

			return queryResultEntrySet.size() > oldSize;
		}

		@Override
		public void init() throws RException {

//			if (trace) {
//				_outln(this, String.format("init, stmt-list=%s", queryStmtList));
//			}

//			queryReteNodeTree = RulpFactory.createList(queryStmtList);
			this.status = BSStats.PROCESS;
		}

		@Override
		public boolean isSucc() {
			return queryResultEntrySet.size() > 0;
		}

		@Override
		public void process(IRBSNode lastNode) throws RException {

			if (!queryForward) {

				// query forward
				model.query(this, queryReteNodeTree, -1, false);
				queryForward = true;
			}

			if (!queryBackward && !isSucc()) {

				// query forward
				model.query(this, queryReteNodeTree, -1, true);
				queryBackward = true;
			}

			this.status = BSStats.COMPLETE;
		}

		public String toString() {
			return String.format("tree=%s, type=%s, status=%s", "" + queryReteNodeTree, "" + this.getType(),
					"" + this.status);
		}
	}

	static class BSStmtIndexs {

		public int maxStmtIndex = -1;

		public ArrayList<Integer> relocatedStmtIndexs = null;

		public IRReteNode rootNode;

		public ArrayList<Integer> stmtIndexs = new ArrayList<>();

		public void addIndex(int index) {
			stmtIndexs.add(index);
			maxStmtIndex = Math.max(maxStmtIndex, index);
		}
	}

	abstract interface IRBSNode {

		public IRList buildResultTree(boolean explain) throws RException;

		public void complete() throws RException;

		public IRBSNode getChild(int index);

		public int getChildCount();

		public int getIndexInParent();

		public int getLevel();

		public String getNodeName();

		public IRBSNode getParentNode();

		public BSStats getStatus();

		public String getStatusString();

		public BSType getType();

		public void init() throws RException;

		public boolean isSucc();

		public void process(IRBSNode lastNode) throws RException;

	}

	public static boolean isBSTrace(IRFrame frame) throws RException {
		return RulpUtil.asBoolean(RulpUtil.getVarValue(frame, A_BS_TRACE)).asBoolean();
	}

	public static boolean isBSTree(IRObject obj) throws RException {

		switch (obj.getType()) {
		case LIST:
			return ReteUtil.isReteStmt(obj);

		case EXPR:

			IRExpr expr = (IRExpr) obj;
			if (expr.size() < 2) {
				return false;
			}

			switch (expr.get(0).asString()) {
			case F_B_AND:
			case F_B_OR:
				break;
			default:
				return false;
			}

			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				if (!isBSTree(it.next())) {
					return false;
				}
			}

			return true;

		default:
			return false;
		}

	}

	protected int bscCircularProof = 0;

	protected int bscNodeAnd = 0;

	protected int bscNodeOr = 0;

	protected int bscNodeQuery = 0;

	protected int bscOpLoop = 0;

	protected int bscOpRelocate = 0;

	protected int bscOpSearch = 0;

	protected int bscStatusComplete = 0;

	protected int bscStatusInit = 0;

	protected int bscStatusProcess = 0;

	protected IRNodeGraph graph;

	protected IRInterpreter interpreter;

	protected XRModel model;

	protected int nodeId = 0;

	protected Map<String, IRBSNode> nodeMap = new HashMap<>();

	protected ArrayList<IRBSNode> queryStack = new ArrayList<>();

	protected IRBSNode rootNode;

	protected boolean trace = false;

	protected Map<String, AbsBSNode> visitingOrNodeMap = new HashMap<>();

	public XRBackSearcher(XRModel model) {
		super();
		this.model = model;
		this.graph = model.getNodeGraph();
		this.interpreter = model.getInterpreter();
	}

	protected boolean _hasStmt(IRBSNode node, IRList stmt) throws RException {

		boolean rc = model.hasStatement(stmt);
		if (trace) {
			_outln(node, "has stmt, stmt=" + stmt + ", rst=" + rc);
		}

		return rc;
	}

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return visitingOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected AbsBSNode _newAndNode(IRList stmt, SourceNode sourceNode, IAction action) {

		int nodeId = _nextNodeId();

		BSNodeAnd node = new BSNodeAnd(nodeId, String.format("A%04d", nodeId));
		node.stmt = stmt;
		node.status = BSStats.INIT;
		node.sourceNode = sourceNode;
		node.action = action;

		this.bscNodeAnd++;

		return node;
	}

	protected AbsBSNode _newOrNode(IRList stmt) throws RException {

		int nodeId = _nextNodeId();

		BSNodeOr node = new BSNodeOr(nodeId, String.format("O%04d", nodeId));
		node.stmt = stmt;
		node.status = BSStats.INIT;

		this.bscNodeOr++;

		visitingOrNodeMap.put(ReteUtil.uniqName(stmt), node);

		return node;
	}

	protected AbsBSNode _newQueryNode(List<IRList> stmtList) throws RException {

		int nodeId = _nextNodeId();

		BSNodeQuery node = new BSNodeQuery(nodeId, String.format("Q%04d", nodeId));
		node.queryReteNodeTree = RulpFactory.createList(stmtList);
		node.status = BSStats.INIT;
		this.bscNodeQuery++;

		return node;
	}

	protected int _nextNodeId() {
		return nodeId++;
	}

	protected void _outln(IRBSNode node, String line) {
		_outln(String.format("%05d %s%s: %s", getBscOpLoop(), RulpUtil.getSpaceLine(node.getLevel()),
				node.getNodeName(), line));
	}

	protected void _outln(String line) {
//		System.out.println(line);
		interpreter.out(line + "\n");
	}

	public int getBscCircularProof() {
		return bscCircularProof;
	}

	public int getBscNodeAnd() {
		return bscNodeAnd;
	}

	public int getBscNodeOr() {
		return bscNodeOr;
	}

	public int getBscNodeQuery() {
		return bscNodeQuery;
	}

	public int getBscOpLoop() {
		return bscOpLoop;
	}

	public int getBscOpRelocate() {
		return bscOpRelocate;
	}

	public int getBscOpSearch() {
		return bscOpSearch;
	}

	public int getBscStatusComplete() {
		return bscStatusComplete;
	}

	public int getBscStatusInit() {
		return bscStatusInit;
	}

	public int getBscStatusProcess() {
		return bscStatusProcess;
	}

//	protected BSNode _buildTree(IRList tree) throws RException {
//
//	}

	public IRList search(IRList tree, boolean explain) throws RException {

		if (!isBSTree(tree)) {
			throw new RException("invalid bs tree: " + tree);
		}

		trace = isBSTrace(model.getFrame());
		rootNode = _newOrNode(tree);

		if (trace) {
			_outln(rootNode, "create_root, " + rootNode.toString());
		}

		IRBSNode curNode = rootNode;
		IRBSNode lastNode = null;

		this.bscOpSearch++;

		while (rootNode.getStatus() != BSStats.COMPLETE) {

			IRBSNode oldNode = curNode;
			BSStats oldStatus = curNode.getStatus();

			switch (oldStatus) {

			case INIT:
				this.bscStatusInit++;

				if (trace) {
					_outln(curNode, "init begin, " + curNode.toString());
				}

				try {
					curNode.init();
				} finally {
					if (trace) {
						_outln(curNode, String.format("init end, rst=%s, status=%s, %s", "" + curNode.isSucc(),
								curNode.getStatus(), curNode.getStatusString()));
					}
				}

				if (curNode.getStatus() == BSStats.PROCESS && curNode.getChildCount() > 0) {
					curNode = curNode.getChild(0);
				}

				break;

			case PROCESS:
				this.bscStatusProcess++;

				if (lastNode != curNode && lastNode.getParentNode() != curNode) {
					throw new RException(
							String.format("%s is not child of %s", lastNode.getNodeName(), curNode.getNodeName()));
				}

				if (trace) {
					_outln(curNode, "process begin");
				}

				int nextChildIndex = lastNode.getIndexInParent() + 1;

				try {
					curNode.process(lastNode);
				} finally {
					if (trace) {
						_outln(curNode,
								String.format("process end, rst=%s, status=%s, child=%d/%d, %s", "" + curNode.isSucc(),
										curNode.getStatus(), nextChildIndex, curNode.getChildCount(),
										curNode.getStatusString()));
					}
				}

				// Process next node if have more child
				if (curNode.getStatus() == BSStats.PROCESS && nextChildIndex < curNode.getChildCount()) {
					curNode = curNode.getChild(nextChildIndex);
				}

				break;

			case COMPLETE:
				this.bscStatusComplete++;

				// re-check
				if (curNode.isSucc()) {

					if (trace) {
						_outln(curNode, "complete begin");
					}

					try {
						curNode.complete();
					} finally {
						if (trace) {
							_outln(curNode, String.format("complete end, rst=%s", "" + curNode.isSucc()));
						}
					}
				}

				if (curNode == rootNode) {
					break;
				}

				curNode = curNode.getParentNode();
				break;

			default:
				throw new RException("unknown status: " + curNode.getStatus());

			}

			if (curNode == oldNode && curNode.getStatus() == oldStatus) {
				throw new RException("dead loop found: " + curNode);

			} else {

				lastNode = oldNode;

				if (trace) {
					if (curNode != oldNode) {
						_outln(oldNode, "route to " + curNode.getNodeName());
					}
				}
			}

			this.bscOpLoop++;
		}

		rootNode.complete();
		if (trace) {
			_outln(rootNode, String.format("complete end, rst=%s", "" + rootNode.isSucc()));
		}

		if (!rootNode.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		return rootNode.buildResultTree(explain);
	}
}
