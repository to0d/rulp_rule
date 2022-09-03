package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.O_B_AND;

import java.util.ArrayList;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpFactory;

public class XRBSNodeLogicAnd extends AbsBSNodeLogic {

	protected IRBSNode failChild = null;

	@Override
	public IRList buildResultTree(boolean explain) throws RException {

		if (!this.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		ArrayList<IRObject> treeList = new ArrayList<>();
		treeList.add(O_B_AND);

		if (childNodes != null) {
			for (AbsBSNode child : childNodes) {
				treeList.add(child.buildResultTree(explain));
			}
		}

		return RulpFactory.createExpression(treeList);
	}

	@Override
	public String getStatusString() {
		return String.format("fail-child=%s", failChild == null ? "null" : failChild.getNodeName());
	}

	@Override
	public BSNodeType getType() {
		return BSNodeType.LOGIC_AND;
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
