package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;

public interface IREntryIteratorBuilder {

	public IRIterator<IRReteEntry> makeIterator(IREntryList list) throws RException;

	public boolean rebuildOrder();
}
