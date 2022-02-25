package alpha.rulp.ximpl.cache;

import alpha.rulp.rule.IRReteNode;

public interface IRCacheWorker {

	public enum CacheStatus {
		LOADED, LOADING, UNLOAD
	}

	public int getLastEntryId();

	public int getLoadCount();

	public IRReteNode getNode();

	public int getReadCount();

	public int getSaveCount();

	public CacheStatus getStatus();

	public int getStmtCount();

	public int getWriteCount();

}
