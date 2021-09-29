package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRAlpha2Node extends XRAlpha1Node {

	protected IREntryTable entryTable;

	protected IRReteEntry _getNewEntry(IRReteEntry parentEntry) throws RException {

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			InheritIndex inherit = inheritIndexs[i];
			newElements[i] = parentEntry.get(inherit.elementIndex);
		}

		return entryTable.createEntry(null, newElements, ReteUtil.getChildStatus(parentEntry), false);
	}

	@Override
	public boolean addReteEntry(IRReteEntry parentEntry) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(parentEntry);

		if (!super.addReteEntry(newEntry)) {
			entryTable.removeEntry(newEntry);
			return false;
		}

		entryTable.addReference(newEntry, this, parentEntry);
		return true;
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

}
