package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.Constant.A_BS_TRACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IREntryAction;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.entry.IREntryQueueUniq;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBackSearcher {

	abstract class BSNode {

		protected ArrayList<BSNode> childNodes;

		protected int curChildIndex = 0;

		protected BSNode parentNode;

		protected BSStats status;

		public void addChild(BSNode child) {

			if (this.childNodes == null) {
				this.childNodes = new ArrayList<>();
			}

			this.childNodes.add(child);
			child.parentNode = this;
		}

		public abstract void complete() throws RException;

		public int getChildCount() {
			return this.childNodes == null ? 0 : this.childNodes.size();
		}

		public abstract BSType getType();

		public abstract BSNode init() throws RException;

		public abstract boolean isSucc();

		public abstract BSNode process() throws RException;

	}

	class BSNodeAnd extends BSNode {

		protected IAction action;

		protected boolean rst;

		protected SourceNode sourceNode;

		protected IRList stmt;

		public void complete() throws RException {

			// no need process
			if (!this.rst) {
				return;
			}

			// need trigger all related rete-node
			if (execute(listAllChildAndStmts())) {
				this.rst = true;
				return;
			}

			BSNode lastChild = this.childNodes.get(childNodes.size() - 1);
			if (lastChild.getType() == BSType.QUERY) {

				BSNodeQuery queryChildNode = (BSNodeQuery) lastChild;
				if (!queryChildNode.hasMore()) {
					this.rst = false;
					return;
				}
			}

			this.sourceNode.rule.start(-1, -1);

			this.rst = _hasStmt(this.stmt);
			return;

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

			return _hasStmt(this.stmt);
		}

		@Override
		public BSType getType() {
			return BSType.AND;
		}

		public BSNode init() throws RException {

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
							this.status = BSStats.COMPLETE;
							this.rst = false;
							bscCircularProof++;
							return this;
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
				this.status = BSStats.COMPLETE;
				this.rst = false;
				return this;
			}

			this.status = BSStats.PROCESS;
			this.curChildIndex = 0;
			return this.childNodes.get(0);
		}

		@Override
		public boolean isSucc() {
			return this.rst;
		}

		public List<IRList> listAllChildAndStmts() {

			List<IRList> stmts = null;

			for (BSNode childNode : childNodes) {
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

		public BSNode process() throws RException {

			BSNode child = this.childNodes.get(curChildIndex);

			// (and false xx xx) ==> false
			if (!child.isSucc()) {
				this.status = BSStats.COMPLETE;
				this.rst = false;
				return this;
			}

			// Scan next if have more brother nodes
			this.curChildIndex++;
			if (this.curChildIndex < this.getChildCount()) {
				return this.childNodes.get(this.curChildIndex);
			}

			// No child need update, mark the parent's result is true
			this.status = BSStats.COMPLETE;
			this.rst = true;
			return this;
		}

		public String toString() {
			return String.format("stmt=%s, type=%s, status=%s", "" + stmt, "" + this.getType(), "" + this.status);
		}
	}

	class BSNodeOr extends BSNode {

		protected boolean rst;

		protected IRList stmt;

		public void complete() throws RException {

			// no need process
			if (!this.rst) {
				return;
			}

			// re-check statement in case it was deleted
			if (!_hasStmt(this.stmt)) {
				this.rst = false;
			}
		}

		public IRList getStmt() {
			return stmt;
		}

		@Override
		public BSType getType() {
			return BSType.OR;
		}

		public BSNode init() throws RException {

			if (_hasStmt(this.stmt)) {
				this.status = BSStats.COMPLETE;
				this.rst = true;
				return this;
			}

			for (SourceNode sn : graph.listSourceNodes(stmt)) {
				for (IAction action : sn.actionList) {

					if (action.getActionType() != RActionType.ADD) {
						continue;
					}

					this.addChild(_newAndNode(stmt, sn, action));
				}
			}

			// no child
			if (this.getChildCount() == 0) {
				this.status = BSStats.COMPLETE;
				this.rst = false;
				return this;
			}

			this.status = BSStats.PROCESS;
			this.curChildIndex = 0;
			return this.childNodes.get(0);
		}

		@Override
		public boolean isSucc() {
			return this.rst;
		}

		public BSNode process() throws RException {

			BSNode child = this.childNodes.get(curChildIndex);

			// (or true xx xx) ==> (true)
			if (child.isSucc()) {
				this.status = BSStats.COMPLETE;
				this.rst = true;
				return this;
			}

			// Scan next if have more brother nodes
			this.curChildIndex++;
			if (this.curChildIndex < this.getChildCount()) {
				return this.childNodes.get(this.curChildIndex);
			}

			// No child need update, mark the parent's result is true
			this.status = BSStats.COMPLETE;
			this.rst = false;
			return this;
		}

		public String toString() {
			return String.format("stmt=%s, type=%s, status=%s", "" + stmt, "" + this.getType(), "" + this.status);
		}

	}

	class BSNodeQuery extends BSNode implements IREntryAction {

		protected boolean queryBackward = false;

		protected boolean queryForward = false;

		protected Set<String> queryResultEntrySet = new HashSet<>();

		protected IRList queryReteNodeTree;

		protected List<IRList> queryStmtList;

		@Override
		public boolean addEntry(IRReteEntry entry) throws RException {

			if (entry != null && !entry.isDroped()) {
				queryResultEntrySet.add(ReteUtil.uniqName(entry));
			}

			return true;
		}

		@Override
		public void complete() throws RException {

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
		public BSNode init() throws RException {
			queryReteNodeTree = RulpFactory.createList(queryStmtList);
			this.status = BSStats.PROCESS;
			return this;
		}

		@Override
		public boolean isSucc() {
			return queryResultEntrySet.size() > 0;
		}

		@Override
		public BSNode process() throws RException {

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
			return this.parentNode;
		}

	}

	static enum BSStats {
		COMPLETE, INIT, PROCESS
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

	static enum BSType {
		AND, OR, QUERY
	}

	static boolean TRACE = false;

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

	protected XRModel model;

	protected Map<String, BSNode> nodeMap = new HashMap<>();

	protected ArrayList<BSNode> queryStack = new ArrayList<>();

	protected BSNode rootNode;

	protected Map<String, BSNode> visitingOrNodeMap = new HashMap<>();

	public static boolean isBSTrace(IRFrame frame) throws RException {
		return RulpUtil.asBoolean(RulpUtil.getVarValue(frame, A_BS_TRACE)).asBoolean();
	}

	public XRBackSearcher(XRModel model) {
		super();
		this.model = model;
		this.graph = model.getNodeGraph();
	}

	boolean _hasStmt(IRList stmt) throws RException {
		return model._findRootEntry(stmt, 0) != null;
	}

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return visitingOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected BSNode _newAndNode(IRList stmt, SourceNode sourceNode, IAction action) {

		if (TRACE) {
			System.out.println("new and node: " + stmt);
		}

		BSNodeAnd node = new BSNodeAnd();
		node.stmt = stmt;
		node.status = BSStats.INIT;
		node.sourceNode = sourceNode;
		node.action = action;

		this.bscNodeAnd++;

		return node;
	}

	protected BSNode _newOrNode(IRList stmt) throws RException {

		if (TRACE) {
			System.out.println("new or node: " + stmt);
		}

		BSNodeOr node = new BSNodeOr();
		node.stmt = stmt;
		node.status = BSStats.INIT;

		this.bscNodeOr++;

		visitingOrNodeMap.put(ReteUtil.uniqName(stmt), node);

		return node;
	}

	protected BSNode _newQueryNode(List<IRList> stmtList) throws RException {

		if (TRACE) {
			System.out.println("new query node: " + stmtList);
		}

		BSNodeQuery node = new BSNodeQuery();
		node.queryStmtList = stmtList;
		node.status = BSStats.INIT;

		this.bscNodeQuery++;

		return node;
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

	public boolean search(IRList stmt) throws RException {

		rootNode = _newOrNode(stmt);
		BSNode curNode = rootNode;

		this.bscOpSearch++;

		while (rootNode.status != BSStats.COMPLETE) {

			this.bscOpLoop++;

			BSNode oldNode = curNode;
			BSStats oldStatus = curNode.status;

			switch (curNode.status) {

			case INIT:
				this.bscStatusInit++;

				curNode = curNode.init();
				break;

			case PROCESS:
				this.bscStatusProcess++;

				curNode = curNode.process();
				break;

			case COMPLETE:
				this.bscStatusComplete++;

				curNode.complete();
				if (curNode == rootNode) {
					break;
				}

				BSNode parent = curNode.parentNode;
				if (parent.childNodes.get(parent.curChildIndex) != curNode) {
					throw new RException("Invalid child: " + parent.curChildIndex);
				}

				curNode = parent;
				break;

			default:
				throw new RException("unknown status: " + curNode.status);

			}

			if (curNode == oldNode && curNode.status == oldStatus) {
				throw new RException("dead loop found: " + curNode);
			}
		}

		curNode.complete();

		return curNode.isSucc();
	}
}
