package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.Collections;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBSNodeStmtOr extends AbsBSNode {

	protected boolean rst;

	protected IRList stmt;

	protected IRBSNode succChild = null;

	public XRBSNodeStmtOr(XRBackSearcher bs, int nodeId, String nodeName) {
		super(bs, nodeId, nodeName);
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
		if (!bs._hasStmt(this, this.stmt)) {
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
		return BSType.STMT_OR;
	}

	public void init() throws RException {

		if (bs._hasStmt(this, this.stmt)) {
			this.status = BSStats.COMPLETE;
			this.rst = true;
			return;
		}

		ArrayList<SourceNode> sourceNodes = new ArrayList<>(bs.graph.listSourceNodes(stmt));
		Collections.sort(sourceNodes, (s1, s2) -> {
			return s1.rule.getRuleName().compareTo(s2.rule.getRuleName());
		});

		for (SourceNode sn : sourceNodes) {

			for (IAction action : sn.actionList) {

				if (action.getActionType() != RActionType.ADD) {
					continue;
				}

				this.addChild(bs._newNodeStmtAnd(stmt, sn, action));
			}
		}

		// no child
		if (this.getChildCount() == 0) {

			if (bs._isTrace()) {
				bs._outln(this, "not child, return false");
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

	@Override
	public boolean needComplete() {
		return true;
	}

	public void process(IRBSNode lastNode) throws RException {

		// (or true xx xx) ==> (true)
		if (lastNode.isSucc()) {
			this.succChild = lastNode;
			this.status = BSStats.COMPLETE;
			this.rst = true;
			return;
		}

	}

	@Override
	public void setSucc(boolean succ) {
		this.rst = succ;
	}

	public String toString() {
		return String.format("stmt=%s, type=%s, status=%s", "" + stmt, "" + this.getType(), "" + this.status);
	}

}
