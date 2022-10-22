package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public class XREntryIteratorBuilderDefault implements IREntryIteratorBuilder {

	static class EntryQueueIterator implements IRIterator<IRReteEntry> {

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
		public IRReteEntry next() throws RException {
			return list.getEntryAt(index++);
		}
	}

	@Override
	public IRIterator<IRReteEntry> makeIterator(IREntryList list) {
		return new EntryQueueIterator(list);
	}

	@Override
	public boolean rebuildOrder() {
		return false;
	}

}
