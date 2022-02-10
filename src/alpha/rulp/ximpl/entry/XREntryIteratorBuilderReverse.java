package alpha.rulp.ximpl.entry;

import java.util.Iterator;

public class XREntryIteratorBuilderReverse implements IREntryIteratorBuilder {

	static class EntryQueueReverseIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryList list;

		public EntryQueueReverseIterator(IREntryList list) {
			super();
			this.list = list;
			this.index = list.size();
		}

		@Override
		public boolean hasNext() {
			return index > 0;
		}

		@Override
		public IRReteEntry next() {
			return list.getEntryAt(--index);
		}
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryList list) {
		return new EntryQueueReverseIterator(list);
	}
}
