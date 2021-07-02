package alpha.rulp.ximpl.entry;

import static alpha.rulp.rule.RReteStatus.REMOVED;

import java.util.ArrayList;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueMulitEntryList implements IREntryQueue {

	static class XREntryCounter implements IREntryCounter {

		protected int entryAssumeCount = 0;

		protected int entryDefinedCount = 0;

		protected int entryDropCount = 0;

		protected int entryNullCount = 0;

		protected int entryReasonCount = 0;

		protected int entryTotalCount = 0;

		public XREntryCounter(XREntryQueueMulitEntryList queue) {
			_updateEntryCount(queue);
		}

		protected void _updateEntryCount(XREntryQueueMulitEntryList queue) {

			entryTotalCount = 0;
			entryNullCount = 0;
			entryDefinedCount = 0;
			entryDropCount = 0;
			entryReasonCount = 0;
			entryAssumeCount = 0;

			if (queue.entryList == null) {
				return;
			}

			for (IRReteEntry entry : queue.entryList) {

				if (entry == null) {
					++entryNullCount;
					continue;
				}

				switch (entry.getStatus()) {
				case ASSUMED:
					++entryAssumeCount;
					break;

				case DEFINED:
					++entryDefinedCount;
					break;

				case REMOVED:
					++entryDropCount;
					break;

				case REASONED:
					++entryReasonCount;
					break;

				default:
					break;
				}
			}

			entryTotalCount = entryDefinedCount + entryReasonCount + entryAssumeCount + entryDropCount + entryNullCount;
		}

		@Override
		public int getEntryCount(RReteStatus status) {

			switch (status) {
			case ASSUMED:
				return entryAssumeCount;
			case DEFINED:
				return entryDefinedCount;
			case REMOVED:
				return entryDropCount;
			case REASONED:
				return entryReasonCount;
			default:
				return 0;
			}

		}

		@Override
		public int getEntryNullCount() {
			return entryNullCount;
		}

		@Override
		public int getEntryTotalCount() {
			return entryTotalCount;
		}

	}

	protected final int entryLength;

	protected ArrayList<IRReteEntry> entryList = null;

	protected int entryRedundant = 0;

	protected int nodeUpdateCount = 0;

	protected int queryFetchCount = 0;

	public XREntryQueueMulitEntryList(int entryLength) {
		super();
		this.entryLength = entryLength;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		if (RuleUtil.isModelTrace()) {
			System.out.println("\t(" + ReteUtil.uniqName(entry) + ")");
		}

		if (entry.size() != getEntryLength()) {
			throw new RException("invalid entry: " + entry);
		}

		if (entryList == null) {
			entryList = new ArrayList<>();
		}

		entryList.add(entry);
		++nodeUpdateCount;
		return true;
	}

	@Override
	public int doGC() {

		int removeCount = 0;

		if (entryList != null) {

			int size = entryList.size();
			for (int i = 0; i < size; ++i) {
				IRReteEntry entry = entryList.get(i);
				if (entry != null && entry.getStatus() == REMOVED) {
					entryList.set(i, null);
					++removeCount;
				}
			}
		}

		return removeCount;
	}

	@Override
	public IRReteEntry getEntryAt(int index) {

		if (index < 0 || index >= size()) {
			return null;
		}

		++queryFetchCount;

		IRReteEntry stmt = entryList.get(index);
		if (stmt == null || stmt.isDroped()) {
			return null;
		}

		return stmt;
	}

	@Override
	public IREntryCounter getEntryCounter() {
		return new XREntryCounter(this);
	}

	@Override
	public int getEntryLength() {
		return entryLength;
	}

	@Override
	public int getQueryFetchCount() {
		return queryFetchCount;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.MULTI;
	}

	@Override
	public int getRedundantCount() {
		return entryRedundant;
	}

	@Override
	public int getUpdateCount() {
		return nodeUpdateCount;
	}

	@Override
	public void incEntryRedundant() {
		entryRedundant++;
	}

	@Override
	public void incNodeUpdateCount() {
		nodeUpdateCount++;
	}

	@Override
	public int size() {
		return entryList == null ? 0 : entryList.size();
	}

}
