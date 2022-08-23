package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;

public interface IREntryQueueUniq extends IREntryQueue {

	public int getStmtIndex(String uniqName) throws RException;

}
