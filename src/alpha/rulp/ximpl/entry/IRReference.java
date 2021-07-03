package alpha.rulp.ximpl.entry;

public interface IRReference extends IFixEntry {

	public int getNodeId();

	public int getParentEntryCount();

	public IRReteEntry getParentEntry(int index);

	public IRReteEntry getChildEntry();
}
