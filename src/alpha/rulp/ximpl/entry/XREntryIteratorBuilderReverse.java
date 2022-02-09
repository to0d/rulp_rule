package alpha.rulp.ximpl.entry;

import java.util.Iterator;

public class XREntryIteratorBuilderReverse implements IREntryIteratorBuilder {

	static class EntryQueueReverseIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryQueue queue;

		public EntryQueueReverseIterator(IREntryQueue queue) {
			super();
			this.queue = queue;
			this.index = queue.size();
		}

		@Override
		public boolean hasNext() {
			return index > 0;
		}

		@Override
		public IRReteEntry next() {
			return queue.getEntryAt(--index);
		}
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryQueue queue) {
		return new EntryQueueReverseIterator(queue);
	}
}
