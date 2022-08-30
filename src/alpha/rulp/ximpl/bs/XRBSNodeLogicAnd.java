package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.O_B_AND;

import java.util.List;

import alpha.rulp.lang.IRAtom;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public class XRBSNodeLogicAnd extends AbsBSNodeLogic {

	public XRBSNodeLogicAnd(XRBackSearcher bs, int nodeId, String nodeName, List<IRList> stmtList) {
		super(bs, nodeId, nodeName, stmtList);
	}

	@Override
	protected IRAtom getLogicAtom() {
		return O_B_AND;
	}

	@Override
	public String getStatusString() {
		return String.format("fail-child=%s", failChild == null ? "null" : failChild.getNodeName());
	}

	protected IRBSNode failChild = null;

	@Override
	public BSType getType() {
		return BSType.LOGIC_AND;
	}

	@Override
	public void process(IRBSNode lastNode) throws RException {

		// (and false xx xx) ==> false
		if (!lastNode.isSucc()) {
			this.failChild = lastNode;
			this.status = BSStats.COMPLETE;
			this.rst = false;
			return;
		}
	}

}
