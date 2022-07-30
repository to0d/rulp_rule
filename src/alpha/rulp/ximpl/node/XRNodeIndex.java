package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.utils.ReteUtil.OrderEntry;

public class XRNodeIndex extends XRNodeRete1 implements IRIndexNode {

	private List<OrderEntry> orderList;

	public XRNodeIndex(String instanceName) {
		super(instanceName);
	}

	@Override
	public List<OrderEntry> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<OrderEntry> orderList) {
		this.orderList = orderList;
	}

}
