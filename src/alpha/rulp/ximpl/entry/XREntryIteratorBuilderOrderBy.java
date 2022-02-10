package alpha.rulp.ximpl.entry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.HeapStack;
import alpha.rulp.utils.RulpUtil;

public class XREntryIteratorBuilderOrderBy implements IREntryIteratorBuilder {

	static class EntryQueueOrderByIterator implements Iterator<IRReteEntry> {

		private HeapStack<IRReteEntry> heapStack;

		public EntryQueueOrderByIterator(HeapStack<IRReteEntry> heapStack) {
			super();
			this.heapStack = heapStack;
		}

		@Override
		public boolean hasNext() {
			return heapStack.size() > 0;
		}

		@Override
		public IRReteEntry next() {
			return heapStack.pop();
		}
	}

	static class OrderByComparator implements Comparator<IRReteEntry> {

		private final List<OrderEntry> orderEntrys;

		public OrderByComparator(List<OrderEntry> orderEntrys) {
			super();
			this.orderEntrys = orderEntrys;
		}

		@Override
		public int compare(IRReteEntry e1, IRReteEntry e2) {

			try {

				int d = 0;

				for (OrderEntry order : orderEntrys) {

					IRObject o1 = e1.get(order.index);
					IRObject o2 = e2.get(order.index);

					if (order.asc) {
						d = RulpUtil.compare(o2, o1);
					} else {
						d = RulpUtil.compare(o1, o2);
					}

					if (d != 0) {
						break;
					}
				}

				return d;

			} catch (RException e) {
				e.printStackTrace();
				throw new RuntimeException(e.toString());
			}
		}

	}

	public static class OrderEntry {
		public boolean asc;
		public int index;
	}

	private final List<OrderEntry> orderEntrys;

	public XREntryIteratorBuilderOrderBy(List<OrderEntry> orderEntrys) {
		super();
		this.orderEntrys = orderEntrys;
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryList list) {

		HeapStack<IRReteEntry> heapStack = new HeapStack<>(new OrderByComparator(orderEntrys));

		int size = list.size();
		for (int i = 0; i < size; ++i) {
			IRReteEntry entry = list.getEntryAt(i);
			if (entry != null && !entry.isDroped()) {
				heapStack.push(entry);
			}
		}

		return new EntryQueueOrderByIterator(heapStack);
	}
}
