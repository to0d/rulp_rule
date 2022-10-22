package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.utils.OrderList;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueOrder2 implements IREntryQueue {

	static class XREntryCounter implements IREntryCounter {

		protected int entryAssumeCount = 0;

		protected int entryDefinedCount = 0;

		protected int entryDropCount = 0;

		protected int entryFixCount = 0;

		protected int entryNullCount = 0;

		protected int entryReasonCount = 0;

		protected int entryRemoveCount = 0;

		protected int entryTempCount = 0;

		protected int entryTotalCount = 0;

		public XREntryCounter(XREntryQueueOrder2 queue) throws RException {
			_updateEntryCount(queue);
		}

		protected void _updateEntryCount(XREntryQueueOrder2 queue) throws RException {

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

			IRIterator<IRReteEntry> it = queue.entryList.iterator();
			while (it.hasNext()) {

				IRReteEntry entry = it.next();

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

	protected OrderList<IRReteEntry> entryList = null;

	protected int entryRedundant = 0;

	protected int nodeUpdateCount = 0;

	private List<OrderEntry> orderList;

	protected int queryFetchCount = 0;

	public XREntryQueueOrder2(int entryLength) {
		super();
		this.entryLength = entryLength;
	}

	protected int _compare(IRList e1, IRList e2) {
		try {
			return ReteUtil.compareEntry(e2, e1, orderList);
		} catch (RException e) {
			e.printStackTrace();
			return 0;
		}
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
			entryList = new OrderList<IRReteEntry>((e1, e2) -> {
				return _compare(e1, e2);
			});
		}

		entryList.add(entry);
		++nodeUpdateCount;
		return true;
	}

	@Override
	public void cleanCache() {
		entryList = null;
	}

	@Override
	public int doGC() {
		return 0;
	}

	@Override
	public String getCacheInfo() {
		return "";
	}

	@Override
	public IRReteEntry getEntryAt(int index) throws RException {
		throw new RException("invalid operation");
	}

	@Override
	public IREntryCounter getEntryCounter() throws RException {
		return new XREntryCounter(this);
	}

	@Override
	public int getEntryLength() {
		return entryLength;
	}

	public List<OrderEntry> getOrderList() {
		return orderList;
	}

	@Override
	public int getQueryFetchCount() {
		return queryFetchCount;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.ORDER;
	}

	@Override
	public int getRedundantCount() {
		return entryRedundant;
	}

	@Override
	public int getRelocateSize() {
		return -1;
	}

	@Override
	public IRReteEntry getStmt(String uniqName) throws RException {
		throw new RException("invalid operation");
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

	public void setOrderList(List<OrderEntry> orderList) {
		this.orderList = orderList;
	}

	@Override
	public void setRelocateSize(int relocateSize) throws RException {

	}

	@Override
	public int size() {
		return entryList == null ? 0 : entryList.size();
	}
}
