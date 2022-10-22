package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public class XREntryIteratorBuilderReverse implements IREntryIteratorBuilder {

	static class EntryQueueReverseIterator implements IRIterator<IRReteEntry> {

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
		public IRReteEntry next() throws RException {
			return list.getEntryAt(--index);
		}
	}

	@Override
	public IRIterator<IRReteEntry> makeIterator(IREntryList list) {
		return new EntryQueueReverseIterator(list);
	}

	@Override
	public boolean rebuildOrder() {
		return true;
	}
}
