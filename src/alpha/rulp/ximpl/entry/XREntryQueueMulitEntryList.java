package alpha.rulp.ximpl.entry;

import java.util.ArrayList;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueMulitEntryList implements IREntryQueue {

	static class XREntryCounter implements IREntryCounter {

		protected int entryTempCount = 0;

		protected int entryAssumeCount = 0;

		protected int entryDefinedCount = 0;

		protected int entryRemoveCount = 0;

		protected int entryFixCount = 0;

		protected int entryNullCount = 0;

		protected int entryReasonCount = 0;

		protected int entryTotalCount = 0;

		protected int entryDropCount = 0;

		public XREntryCounter(XREntryQueueMulitEntryList queue) {
			_updateEntryCount(queue);
		}

		protected void _updateEntryCount(XREntryQueueMulitEntryList queue) {

			entryTotalCount = 0;
			entryNullCount = 0;
			entryDefinedCount = 0;
			entryRemoveCount = 0;
			entryReasonCount = 0;
			entryAssumeCount = 0;
			entryTempCount = 0;
			entryFixCount = 0;
			entryDropCount = 0;

			if (queue.entryList == null) {
				return;
			}

			for (IRReteEntry entry : queue.entryList) {

				if (entry == null) {
					++entryNullCount;
					continue;
				}

				RReteStatus status = entry.getStatus();

				if (status == null) {
					++entryDropCount;
				} else {
					switch (entry.getStatus()) {
					case ASSUME:
						++entryAssumeCount;
						break;

					case DEFINE:
						++entryDefinedCount;
						break;

					case REMOVE:
						++entryRemoveCount;
						break;

					case REASON:
						++entryReasonCount;
						break;

					case FIXED_:
						++entryFixCount;

					case TEMP__:
						++entryTempCount;

						break;

					default:
						break;
					}
				}
			}

			entryTotalCount = entryDefinedCount + entryReasonCount + entryAssumeCount + entryRemoveCount
					+ entryNullCount + entryTempCount + entryFixCount + entryDropCount;
		}

		@Override
		public int getEntryCount(RReteStatus status) {

			if (status == null) {
				return entryDropCount;
			}

			switch (status) {
			case ASSUME:
				return entryAssumeCount;

			case DEFINE:
				return entryDefinedCount;

			case REMOVE:
				return entryRemoveCount;

			case TEMP__:
				return entryTempCount;

			case REASON:
				return entryReasonCount;

			case FIXED_:
				return entryFixCount;

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
	public boolean addEntry(IRReteEntry entry) throws RException {

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
				if (entry != null && entry.isDroped()) {
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
