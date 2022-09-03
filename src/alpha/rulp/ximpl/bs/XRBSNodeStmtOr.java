package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.Collections;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.RActionType;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBSNodeStmtOr extends AbsBSNode implements IRBSNodeStmt {

	protected boolean needComplete = true;

	protected boolean rst;

	protected IRList stmt;

	protected IRBSNode succChild = null;

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
		if (!engine.hasStmt(this, this.stmt)) {
			this.rst = false;
		}
	}

	public String getStatusString() {
		return String.format("succ-child=%s, needComplete=%s", succChild == null ? "null" : succChild.getNodeName(),
				"" + needComplete);
	}

	@Override
	public IRList getStmt() {
		return stmt;
	}

	@Override
	public BSNodeType getType() {
		return BSNodeType.STMT_OR;
	}

	public void init() throws RException {

		if (engine.hasStmt(this, this.stmt)) {
			this.status = BSStats.COMPLETE;
			this.rst = true;
			this.needComplete = false;
			return;
		}

		ArrayList<SourceNode> sourceNodes = new ArrayList<>(engine.getGraph().listSourceNodes(stmt));
		Collections.sort(sourceNodes, (s1, s2) -> {
			return s1.rule.getRuleName().compareTo(s2.rule.getRuleName());
		});

		for (SourceNode sn : sourceNodes) {

			for (IAction action : sn.actionList) {

				if (action.getActionType() != RActionType.ADD) {
					continue;
				}

				BSFactory.addChild(engine, this, BSFactory.createNodeStmtAnd(engine, stmt, sn, action));
			}
		}

		// no child
		if (this.getChildCount() == 0) {

			if (engine.isTrace()) {
				engine.trace_outln(this, "not child, return false");
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
		return needComplete;
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
