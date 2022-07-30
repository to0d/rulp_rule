package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.utils.ReteUtil.OrderEntry;

public class XRIndexNode extends XRNodeRete1 implements IRIndexNode {

	private IRExpr orderExpr;

	private List<OrderEntry> orderList;

	public XRIndexNode(String instanceName) {
		super(instanceName);
	}

	@Override
	public IRExpr getOrderExpr() {
		return null;
	}

}
