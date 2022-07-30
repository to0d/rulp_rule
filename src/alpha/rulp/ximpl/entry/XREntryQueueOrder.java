package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.utils.ReteUtil.OrderEntry;

public class XREntryQueueOrder extends XREntryQueueMulit {

	private List<OrderEntry> orderList;

	public XREntryQueueOrder(int entryLength) {
		super(entryLength);
	}

	public List<OrderEntry> getOrderList() {
		return orderList;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.ORDER;
	}

	public void setOrderList(List<OrderEntry> orderList) {
		this.orderList = orderList;
	}
}
