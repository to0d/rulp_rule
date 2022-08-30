package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;

public abstract class AbsBSNodeLogic extends AbsBSNode {

	protected boolean rst;

	protected List<IRList> stmtList;

	public AbsBSNodeLogic(XRBackSearcher bs, int nodeId, String nodeName, List<IRList> stmtList) {
		super(bs, nodeId, nodeName);
		this.stmtList = stmtList;
	}

	@Override
	public IRList buildResultTree(boolean explain) throws RException {

		if (!this.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		ArrayList<IRObject> treeList = new ArrayList<>();
		treeList.add(getLogicAtom());

		if (childNodes != null) {
			for (AbsBSNode child : childNodes) {
				treeList.add(child.buildResultTree(explain));
			}
		}

		return RulpFactory.createExpression(treeList);
	}

	@Override
	public void complete() throws RException {

	}

	protected abstract IRAtom getLogicAtom();

	@Override
	public void init() throws RException {

		for (IRList stmt : stmtList) {
			this.addChild(bs._newNode(stmt));
		}

		this.status = BSStats.PROCESS;
	}

	@Override
	public boolean isSucc() {
		return rst;
	}

	@Override
	public boolean needComplete() {
		return false;
	}

	@Override
	public void setSucc(boolean succ) {
		this.rst = succ;
	}
}
