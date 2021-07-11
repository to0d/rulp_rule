package alpha.rulp.ximpl.entry;

import alpha.rulp.ximpl.node.IRReteNode;

public interface IRReference extends IFixEntry {

	public IRReteEntry getChildEntry();

	public IRReteNode getNode();

	public IRReteEntry getParentEntry(int index);

	public int getParentEntryCount();
}
