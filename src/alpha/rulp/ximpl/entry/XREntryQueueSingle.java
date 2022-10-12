package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;

public class XREntryQueueSingle implements IREntryQueue {

	protected IRReteNode bindNode;

	protected IRReteEntry curEntry;

	protected final int entryLength;

	protected int entryRedundant = 0;

	protected IREntryTable entryTable;

	protected int maxValueCount = 0;

	protected int nodeUpdateCount = 0;

	protected int queryFetchCount = 0;

	public XREntryQueueSingle(int entryLength) {
		super();
		this.entryLength = entryLength;
	}

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		if (entry == null) {
			return false;
		}

		if (curEntry != null && !curEntry.isDroped()) {

			if (RulpUtil.equal(curEntry, entry)) {
				++entryRedundant;
				return false;
			}

			// drop old entry
			if (bindNode != null) {
				entryTable.deleteEntryReference(curEntry, bindNode);
			}

		}

		this.curEntry = entry;
		this.maxValueCount++;
		this.nodeUpdateCount++;

		if (bindNode != null) {
			entryTable.addReference(entry, bindNode);
		}

		return true;
	}

	@Override
	public void cleanCache() {
		this.curEntry = null;
	}

	@Override
	public int doGC() {
		return 0;
	}

	public String getCacheInfo() {
		return "";
	}

	@Override
	public IRReteEntry getEntryAt(int index) {
		++queryFetchCount;
		return index == (maxValueCount - 1) ? curEntry : null;
	}

	@Override
	public IREntryCounter getEntryCounter() {

		return new IREntryCounter() {

			@Override
			public int getEntryCount(RReteStatus status) {

				if (curEntry != null && status == curEntry.getStatus()) {
					return 1;
				}

				return 0;
			}

			@Override
			public int getEntryNullCount() {
				return curEntry == null ? 1 : 0;
			}

			@Override
			public int getEntryTotalCount() {
				return 1;
			}
		};
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
		return REntryQueueType.SINGLE;
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

		if (curEntry != null && !curEntry.isDroped() && ReteUtil.uniqName(curEntry).equals(uniqName)) {
			return curEntry;
		}

		return null;
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

	public void setBindNode(IRReteNode bindNode) {
		this.bindNode = bindNode;
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	@Override
	public void setRelocateSize(int relocateSize) {

	}

	@Override
	public int size() {
		return maxValueCount;
	}

}
