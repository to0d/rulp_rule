package alpha.rulp.ximpl.cache;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRCacheWorker {

	public enum CacheStatus {
		LOADED, LOADING, UNLOAD, CLEAN
	}

	public int getCacheLastEntryId();

	public int getLoadCount();

	public IRReteNode getNode();

	public int getReadCount();

	public int getSaveCount();

	public CacheStatus getStatus();

	public int getStmtCount();

	public int getWriteCount();

	public void cleanCache() throws RException;

	public boolean isDirty() throws RException;

}
