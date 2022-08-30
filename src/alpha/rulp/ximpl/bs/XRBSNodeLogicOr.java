package alpha.rulp.ximpl.bs;

import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;

public class XRBSNodeLogicOr extends AbsBSNodeLogic {

	protected IRBSNode succChild = null;

	public XRBSNodeLogicOr(XRBackSearcher bs, int nodeId, String nodeName, List<IRList> stmtList) {
		super(bs, nodeId, nodeName, stmtList);
	}

	@Override
	public IRList buildResultTree(boolean explain) throws RException {

		if (!this.isSucc() || succChild == null) {
			return RulpFactory.emptyConstList();
		}

		return succChild.buildResultTree(explain);
	}

	@Override
	public String getStatusString() {
		return String.format("succ-child=%s", succChild == null ? "null" : succChild.getNodeName());
	}

	@Override
	public BSType getType() {
		return BSType.LOGIC_OR;
	}

	@Override
	public void process(IRBSNode lastNode) throws RException {

		// (or true xx xx) ==> (true)
		if (lastNode.isSucc()) {
			this.succChild = lastNode;
			this.status = BSStats.COMPLETE;
			this.rst = true;
			return;
		}
	}

}
