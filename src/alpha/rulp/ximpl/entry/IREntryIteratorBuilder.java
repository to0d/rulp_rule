package alpha.rulp.ximpl.entry;

import java.util.Iterator;

public interface IREntryIteratorBuilder {

	public Iterator<IRReteEntry> makeIterator(IREntryList list);

	public boolean rebuildOrder();
}
