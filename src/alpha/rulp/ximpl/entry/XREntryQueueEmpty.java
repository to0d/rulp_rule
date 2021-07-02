package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;

public class XREntryQueueEmpty implements IREntryQueue {

	static IREntryCounter zeroCounter = new IREntryCounter() {

		@Override
		public int getEntryCount(RReteStatus status) {
			return 0;
		}

		@Override
		public int getEntryNullCount() {
			return 0;
		}

		@Override
		public int getEntryTotalCount() {
			return 0;
		}
	};

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {
		return true;
	}

	@Override
	public int doGC() {
		return 0;
	}

	@Override
	public IRReteEntry getEntryAt(int index) {
		return null;
	}

	@Override
	public IREntryCounter getEntryCounter() {
		return zeroCounter;
	}

	@Override
	public int getQueryFetchCount() {
		return 0;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.EMPTY;
	}

	@Override
	public int getRedundantCount() {
		return 0;
	}

	@Override
	public int getUpdateCount() {
		return 0;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public int getEntryLength() {
		return -1;
	}

	@Override
	public void incEntryRedundant() {
		
	}

	@Override
	public void incNodeUpdateCount() {
		
	}

}
