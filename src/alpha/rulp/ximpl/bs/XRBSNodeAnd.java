package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.entry.IREntryQueueUniq;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBSNodeAnd extends AbsBSNode {

	protected IAction action;

	protected IRBSNode failChild = null;

	protected boolean rst;

	protected SourceNode sourceNode;

	protected IRList stmt;

	public XRBSNodeAnd(XRBackSearcher bs, int nodeId, String nodeName) {
		super(bs, nodeId, nodeName);
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

			boolean hasMore = ((XRBSNodeQuery) lastChild).hasMore();

			if (bs.isTrace()) {
				bs.outln(this, String.format("complete-query, hasMore=%s", "" + hasMore));
			}

			if (!hasMore) {
				this.rst = false;
				return;
			}
		}

		this.sourceNode.rule.start(-1, -1);
		this.rst = bs.hasStmt(this, this.stmt);
	}

	public boolean execute(List<IRList> childStmts) throws RException {

		ArrayList<IRReteNode> rootNodes = new ArrayList<>();
		ArrayList<BSStmtIndexs> BSStmtIndexsList = new ArrayList<>();

		Map<IRReteNode, BSStmtIndexs> stmtIndexMap = new HashMap<>();

		for (IRList childStmt : childStmts) {

			IRReteNode rootNode = bs.getGraph().findRootNode(childStmt.getNamedName(), childStmt.size());
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
				bs.bscOpRelocate += si.relocatedStmtIndexs.size();
				((IREntryQueueUniq) si.rootNode.getEntryQueue()).relocate(childMaxVisitIndex, si.relocatedStmtIndexs);
			}
		}

		this.sourceNode.rule.start(-1, -1);

		for (BSStmtIndexs si : BSStmtIndexsList) {
			if (si.relocatedStmtIndexs != null) {
				((IREntryQueueUniq) si.rootNode.getEntryQueue()).relocate(-1, null);
			}
		}

		return bs.hasStmt(this, this.stmt);
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
					if (bs._isCircularProof(newStmt)) {

						if (bs.isTrace()) {
							bs.outln(this, String.format("circular proof found, stmt=%s, return false", newStmt));
						}

						this.status = BSStats.COMPLETE;
						this.rst = false;
						bs.bscCircularProof++;

						return;
					}

					this.addChild(bs._newOrNode(newStmt));
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
			this.addChild(bs._newQueryNode(queryStmtList));
		}

		// no child
		if (this.getChildCount() == 0) {

			if (bs.isTrace()) {
				bs.outln(this, "not child, return false");
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

				XRBSNodeOr orNode = (XRBSNodeOr) childNode;
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