package alpha.rulp.ximpl.entry;

import java.util.Collections;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;

public class XREntryQueueOrder extends XREntryQueueMulit {

	private List<OrderEntry> orderList;

	public XREntryQueueOrder(int entryLength) {
		super(entryLength);
	}

	protected int _compare(IRList e1, IRList e2) throws RException {
		return ReteUtil.compareEntry(e2, e1, orderList);
	}

	public IRReteEntry find(IRList stmt) throws RException {

		int len = size();
		if (len == 0) {
			return null;
		}

		// stmt:(a b ?c)
		// '(?a b ?c) order by 0

		for (OrderEntry order : orderList) {
			int index = order.index;
			if (RulpUtil.isVarAtom(stmt.get(index))) {
				throw new RException("invalid index stmt: index=" + index + ", stmt=" + stmt);
			}
		}

		// half search

		int low = 0;
		int high = len - 1;
		while (low <= high) {

			if ((low + 1) >= high) {

				IRReteEntry entry = entryList.get(low);
				int d = _compare(entry, stmt);
				if (d == 0) {
					return entry;
				}

				if (d < 0) {
					low++;
					continue;
				}

				return null;
			}

			int mid = (high + low) / 2;
			IRReteEntry entry = entryList.get(mid);
			int d = _compare(entry, stmt);
			if (d == 0) {
				return entry;
			}

			if (d < 0) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}

		return null;
	}

	public List<OrderEntry> getOrderList() {
		return orderList;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.ORDER;
	}

	public void rebuildOrder() {

		int len = size();
		if (len <= 1) {
			return;
		}

		Collections.sort(entryList, (e1, e2) -> {
			try {
				return _compare(e1, e2);
			} catch (RException e) {
				e.printStackTrace();
				return 0;
			}
		});

	}

	public void setOrderList(List<OrderEntry> orderList) {
		this.orderList = orderList;
	}
}
