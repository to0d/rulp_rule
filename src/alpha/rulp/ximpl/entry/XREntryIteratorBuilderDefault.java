package alpha.rulp.ximpl.entry;

import java.util.Iterator;

public class XREntryIteratorBuilderDefault implements IREntryIteratorBuilder {

	static class EntryQueueIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryList list;

		public EntryQueueIterator(IREntryList list) {
			super();
			this.list = list;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < list.size();
		}

		@Override
		public IRReteEntry next() {
			return list.getEntryAt(index++);
		}
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryList list) {
		return new EntryQueueIterator(list);
	}

}
