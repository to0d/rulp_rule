package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.RReteStatus.REMOVE;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBeta3Node extends XRReteNode2 {

	protected boolean _addNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(leftEntry, rightEntry);

		if (addReteEntry(newEntry)) {
			entryTable.addReference(newEntry, this, leftEntry, rightEntry);
			return true;
		}

		return false;
	}

	protected boolean _match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		if (leftEntry == null || leftEntry.getStatus() == REMOVE) {
			return false;
		}

		if (rightEntry == null || rightEntry.getStatus() == REMOVE) {
			return false;
		}

		return true;
	}

}
