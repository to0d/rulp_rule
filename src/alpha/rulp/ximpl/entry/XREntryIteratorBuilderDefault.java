package alpha.rulp.ximpl.entry;

import java.util.Iterator;

public class XREntryIteratorBuilderDefault implements IREntryIteratorBuilder {

	static class EntryQueueIterator implements Iterator<IRReteEntry> {

		private int index;

		private IREntryQueue queue;

		public EntryQueueIterator(IREntryQueue queue) {
			super();
			this.queue = queue;
			this.index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < queue.size();
		}

		@Override
		public IRReteEntry next() {
			return queue.getEntryAt(index++);
		}
	}

	@Override
	public Iterator<IRReteEntry> makeIterator(IREntryQueue queue) {
		return new EntryQueueIterator(queue);
	}

}
