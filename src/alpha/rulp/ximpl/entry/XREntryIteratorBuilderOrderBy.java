package alpha.rulp.ximpl.entry;

import java.util.Iterator;
import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.HeapStack;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.ReteUtil;

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

	private final List<OrderEntry> orderEntrys;

	public XREntryIteratorBuilderOrderBy(List<OrderEntry> orderEntrys) {
		super();
		this.orderEntrys = orderEntrys;
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryList list) {

		HeapStack<IRReteEntry> heapStack = new HeapStack<>((e1, e2) -> {

			try {
				return ReteUtil.compareEntry(e1, e2, orderEntrys);
			} catch (RException e) {
				e.printStackTrace();
				throw new RuntimeException(e.toString());
			}
		});

		int size = list.size();
		for (int i = 0; i < size; ++i) {
			IRReteEntry entry = list.getEntryAt(i);
			if (entry != null && !entry.isDroped()) {
				heapStack.push(entry);
			}
		}

		return new EntryQueueOrderByIterator(heapStack);
	}

	@Override
	public boolean rebuildOrder() {
		return true;
	}
}
