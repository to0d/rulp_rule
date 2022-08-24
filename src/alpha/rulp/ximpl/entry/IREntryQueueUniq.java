package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.RException;

public interface IREntryQueueUniq extends IREntryQueue {

	public int getStmtIndex(String uniqName) throws RException;

	public void relocate(int relocatePos, List<Integer> stmtIndexs) throws RException;

}
