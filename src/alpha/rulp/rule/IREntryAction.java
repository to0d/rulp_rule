package alpha.rulp.rule;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IREntryAction {

	public boolean addEntry(IRReteEntry entry) throws RException;
}
