package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.ximpl.entry.XREntryQueueOrder;

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

	@Override
	public int update(int limit) throws RException {

		int update = super.update(limit);
		if (update > 0) {
			((XREntryQueueOrder) getEntryQueue()).rebuildOrder();
		}

		return update;
	}

}
