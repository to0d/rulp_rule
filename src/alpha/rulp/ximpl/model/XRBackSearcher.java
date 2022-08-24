package alpha.rulp.ximpl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.entry.IREntryQueueUniq;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBackSearcher {

	static abstract class BSNode {

		public ArrayList<BSNode> childNodes;

		public int curChildIndex = 0;

		public BSNode parentNode;

		public boolean rst;

		public BSStats status;

		public IRList stmt;

		public void addChild(BSNode child) {

			if (this.childNodes == null) {
				this.childNodes = new ArrayList<>();
			}

			this.childNodes.add(child);
			child.parentNode = this;
		}

		public abstract BSNode childComplete(XRBackSearcher bs, BSNode child) throws RException;

		public abstract boolean complete(XRBackSearcher bs) throws RException;

		public int getChildCount() {
			return this.childNodes == null ? 0 : this.childNodes.size();
		}

		public abstract BSType getType();

		public abstract BSNode init(XRBackSearcher bs) throws RException;

		public String toString() {
			return String.format("stmt=%s, type=%s, status=%s", "" + stmt, "" + this.getType(), "" + this.status);
		}
	}

	static class BSNodeAnd extends BSNode {

		public IAction action;

		public SourceNode sourceNode;

		public BSNode childComplete(XRBackSearcher bs, BSNode child) throws RException {

			// (and false xx xx) ==> false
			if (!child.rst) {
				this.status = BSStats.COMPLETED;
				this.rst = false;
				return this;
			}

			// Scan next if have more brother nodes
			this.curChildIndex++;
			if (this.curChildIndex < this.getChildCount()) {
				return this.childNodes.get(this.curChildIndex);
			}

			// No child need update, mark the parent's result is true
			this.status = BSStats.COMPLETED;
			this.rst = true;
			return this;
		}

		static class BSStmtIndexs {

			public int maxStmtIndex = -1;

			public ArrayList<Integer> stmtIndexs = new ArrayList<>();

			public void addIndex(int index) {
				stmtIndexs.add(index);
				maxStmtIndex = Math.max(maxStmtIndex, index);
			}

			public ArrayList<Integer> relocatedStmtIndexs = new ArrayList<>();
		}

		public boolean complete(XRBackSearcher bs) throws RException {

			// no need process
			if (!this.rst) {
				return false;
			}

			// need trigger all related rete-node

			ArrayList<IRReteNode> rootNodes = new ArrayList<>();
			Map<IRReteNode, BSStmtIndexs> stmtIndexMap = new HashMap<>();

			for (BSNode childNode : childNodes) {

				IRList stmt = childNode.stmt;

				IRReteNode rootNode = bs.graph.findRootNode(stmt.getNamedName(), stmt.size());
				if (!rootNodes.contains(rootNode)) {
					rootNodes.add(rootNode);
				}

				IREntryQueueUniq uniqQueue = ((IREntryQueueUniq) rootNode.getEntryQueue());
				int stmtIndex = uniqQueue.getStmtIndex(ReteUtil.uniqName(stmt));

				BSStmtIndexs si = stmtIndexMap.get(rootNode);
				if (si == null) {
					si = new BSStmtIndexs();
					stmtIndexMap.put(rootNode, si);
				}

				si.addIndex(stmtIndex);
			}

			for (IRReteNode rootNode : rootNodes) {

				BSStmtIndexs si = stmtIndexMap.get(rootNode);

				// Get the max visit index
				int childMaxVisitIndex = ReteUtil.findChildMaxVisitIndex(rootNode);

				// All statements are already passed to child nodes
				if (childMaxVisitIndex > si.maxStmtIndex) {
					continue;
				}

				for (int index : si.stmtIndexs) {
					if (index >= childMaxVisitIndex) {
						si.relocatedStmtIndexs.add(index);
					}
				}

				((IREntryQueueUniq) rootNode.getEntryQueue()).relocate(childMaxVisitIndex, si.relocatedStmtIndexs);
			}

			this.sourceNode.rule.start(-1, -1);

			for (IRReteNode rootNode : rootNodes) {
				((IREntryQueueUniq) rootNode.getEntryQueue()).relocate(-1, null);
			}

			if (bs.model._findRootEntry(this.stmt, 0) == null) {
				this.rst = false;
			}

			return this.rst;
		}

		@Override
		public BSType getType() {
			return BSType.AND;
		}

		public BSNode init(XRBackSearcher bs) throws RException {

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

			for (IRList list : sourceNode.rule.getMatchStmtList()) {
				if (ReteUtil.isAlphaMatchTree(list)) {

					IRList newStmt = (IRList) RuntimeUtil.rebuild(list, varValueMap);
					if (!ReteUtil.isReteStmtNoVar(newStmt)) {
						throw new RException("can't prove stmt: " + newStmt);
					}

					this.addChild(_newOrNode(newStmt));
				}
			}

			// no child
			if (this.getChildCount() == 0) {
				this.status = BSStats.COMPLETED;
				this.rst = false;
				return this;
			}

			this.status = BSStats.PROCESS;
			this.curChildIndex = 0;
			return this.childNodes.get(0);
		}
	}

	static class BSNodeOr extends BSNode {

		public BSNode childComplete(XRBackSearcher bs, BSNode child) throws RException {

			// (or true xx xx) ==> (true)
			if (child.rst) {
				this.status = BSStats.COMPLETED;
				this.rst = true;
				return this;
			}

			// Scan next if have more brother nodes
			this.curChildIndex++;
			if (this.curChildIndex < this.getChildCount()) {
				return this.childNodes.get(this.curChildIndex);
			}

			// No child need update, mark the parent's result is true
			this.status = BSStats.COMPLETED;
			this.rst = false;
			return this;
		}

		public boolean complete(XRBackSearcher bs) throws RException {

			if (this.rst) {

				// re-check statement in case it was deleted
				if (bs.model._findRootEntry(this.stmt, 0) == null) {
					this.rst = false;
				}
			}

			return this.rst;
		}

		@Override
		public BSType getType() {
			return BSType.OR;
		}

		public BSNode init(XRBackSearcher bs) throws RException {

			// has statement
			if (bs.model._findRootEntry(this.stmt, 0) != null) {
				this.status = BSStats.COMPLETED;
				this.rst = true;
				return this;
			}

			for (SourceNode sn : bs.graph.listSourceNodes(stmt)) {
				for (IAction action : sn.actionList) {

					if (action.getActionType() != RActionType.ADD) {
						continue;
					}

					this.addChild(_newAndNode(stmt, sn, action));
				}
			}

			// no child
			if (this.getChildCount() == 0) {
				this.status = BSStats.COMPLETED;
				this.rst = false;
				return this;
			}

			this.status = BSStats.PROCESS;
			this.curChildIndex = 0;
			return this.childNodes.get(0);
		}

	}

	static enum BSStats {
		COMPLETED, INIT, PROCESS
	}

	static enum BSType {
		AND, OR
	}

	static BSNode _newAndNode(IRList stmt, SourceNode sourceNode, IAction action) {

		BSNodeAnd node = new BSNodeAnd();
		node.stmt = stmt;
		node.status = BSStats.INIT;
		node.sourceNode = sourceNode;
		node.action = action;

		return node;
	}

	static BSNode _newOrNode(IRList stmt) {

		BSNodeOr node = new BSNodeOr();
		node.stmt = stmt;
		node.status = BSStats.INIT;

		return node;
	}

	IRNodeGraph graph;

	XRModel model;

	Map<String, BSNode> nodeMap = new HashMap<>();

	ArrayList<BSNode> queryStack = new ArrayList<>();

	BSNode rootNode;

	public XRBackSearcher(XRModel model) {
		super();
		this.model = model;
		this.graph = model.getNodeGraph();
	}

	public boolean search(IRList stmt) throws RException {

		rootNode = _newOrNode(stmt);
		BSNode curNode = rootNode;

		while (rootNode.status != BSStats.COMPLETED) {

			BSNode oldNode = curNode;
			BSStats oldStatus = curNode.status;

			switch (curNode.status) {

			case INIT:
				curNode = curNode.init(this);
				break;

			case PROCESS:
				break;

			case COMPLETED:
				curNode.complete(this);
				if (curNode == rootNode) {
					break;
				}

				BSNode parent = curNode.parentNode;
				if (parent.childNodes.get(parent.curChildIndex) != curNode) {
					throw new RException("Invalid child: " + parent.curChildIndex);
				}
				curNode = parent.childComplete(this, curNode);
				break;

			default:
				throw new RException("unknown status: " + curNode.status);

			}

			if (curNode == oldNode && curNode.status == oldStatus) {
				throw new RException("dead loop found: " + curNode);
			}
		}

		return curNode.complete(this);
	}
}
