package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;

public interface IREntryQueue extends IREntryList {

	public static interface IREntryCounter {

		public int getEntryCount(RReteStatus status);

		public int getEntryNullCount();

		public int getEntryTotalCount();
	}

	public boolean addEntry(IRReteEntry entry) throws RException;

	public int doGC();

	public IREntryCounter getEntryCounter();

	public int getEntryLength();

	public int getQueryFetchCount();

	public REntryQueueType getQueueType();

	public int getRedundantCount();

	public IRReteEntry getStmt(String uniqName) throws RException;

	public int getUpdateCount();

	public void incEntryRedundant();

	public void incNodeUpdateCount();

	public void cleanCache();

}
