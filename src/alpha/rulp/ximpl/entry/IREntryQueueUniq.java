package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.RException;

public interface IREntryQueueUniq extends IREntryQueue {

	public int getStmtIndex(String uniqName) throws RException;

	public int relocate(int relocateStartPos, List<Integer> stmtIndexs) throws RException;

}
