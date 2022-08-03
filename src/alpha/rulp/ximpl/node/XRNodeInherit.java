package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeInherit extends XRNodeRete1 {

	protected int[] inheritIndexs;

	protected IREntryTable entryTable;

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	public XRNodeInherit(String instanceName) {
		super(instanceName);
	}

	public void setInheritIndexs(int[] inheritIndexs) {
		this.inheritIndexs = inheritIndexs;
	}

	@Override
	public boolean addReteEntry(IRReteEntry entry) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			newElements[i] = entry.get(inheritIndexs[i]);
		}

		IRReteEntry newEntry = entryTable.createEntry(null, newElements, entry.getStatus(), false);
		if (!super.addReteEntry(newEntry)) {
			entryTable.removeEntry(newEntry);
			return false;
		}

		entryTable.addReference(newEntry, this, entry);
		return true;
	}
}
