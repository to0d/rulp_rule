package alpha.rulp.ximpl.bs;

import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public abstract class AbsBSNodeLogic extends AbsBSNode {

	protected boolean rst;

	protected List<IRList> stmtList;

	public AbsBSNodeLogic(XRBackSearcher bs, int nodeId, String nodeName, List<IRList> stmtList) {
		super(bs, nodeId, nodeName);
		this.stmtList = stmtList;
	}

	@Override
	public void complete() throws RException {

	}

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

	@Override
	public String toString() {
		return String.format("stmt-list=%s, type=%s, status=%s", stmtList, "" + this.getType(), "" + this.status);
	}
}
