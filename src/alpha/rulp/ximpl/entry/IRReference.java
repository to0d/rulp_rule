package alpha.rulp.ximpl.entry;

import alpha.rulp.ximpl.entry.XREntryTable.XRReteEntry;

public interface IRReference extends IFixEntry {

	public int getNodeId();

	public int getParentEntryCount();

	public IRReteEntry getParentEntry(int index);

	public IRReteEntry getChildEntry();
}
