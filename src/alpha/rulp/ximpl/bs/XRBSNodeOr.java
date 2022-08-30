package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.Collections;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBSNodeOr extends AbsBSNode {

	protected boolean rst;

	protected IRList stmt;

	protected IRBSNode succChild = null;

	public XRBSNodeOr(XRBackSearcher bs, int nodeId, String nodeName) {
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
		if (!bs.hasStmt(this, this.stmt)) {
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

		if (bs.hasStmt(this, this.stmt)) {
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

				this.addChild(bs._newAndNode(stmt, sn, action));
			}
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
