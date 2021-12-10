package alpha.rulp.ximpl.cache;

import alpha.rulp.rule.IRReteNode;

public interface IRCacheWorker {

	public int getLastEntryId();

	public int getLoadCount();

	public IRReteNode getNode();

	public int getReadCount();

	public int getSaveCount();

	public int getStmtCount();

	public int getWriteCount();

	public boolean isLoaded();

}
