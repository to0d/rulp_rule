package alpha.rulp.ximpl.bs;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;

public class XRBSNodeLogicAnd extends AbsBSNode {

	public XRBSNodeLogicAnd(XRBackSearcher bs, int nodeId, String nodeName) {
		super(bs, nodeId, nodeName);
	}

	protected boolean rst;

	@Override
	public IRList buildResultTree(boolean explain) throws RException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void complete() throws RException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getStatusString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BSType getType() {
		return BSType.LOGIC_AND;
	}

	@Override
	public void init() throws RException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSucc() {
		return rst;
	}

	@Override
	public void process(IRBSNode lastNode) throws RException {
		// TODO Auto-generated method stub

	}

}
