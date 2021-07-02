package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.RReteStatus.REASONED;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class XRReteNode2 extends AbsReteNode {

	protected IREntryTable entryTable;

	protected InheritIndex inheritIndexs[];

	protected int lastLeftEntryCount = 0;

	protected int lastRightEntryCount = 0;

	protected boolean _addNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(leftEntry, rightEntry);

		if (addReteEntry(newEntry)) {
			entryTable.addReference(newEntry, this.getNodeId(), leftEntry.getEntryId(), rightEntry.getEntryId());
			return true;
		}

		return false;
	}

	protected IRReteEntry _getNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			InheritIndex inherit = inheritIndexs[i];
			IRReteEntry parentEntry = inherit.parentIndex == 0 ? leftEntry : rightEntry;
			newElements[i] = parentEntry.get(inherit.elementIndex);
		}

		return entryTable.createEntry(null, newElements, REASONED);
	}

	protected abstract boolean _match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException;

	protected int _update_no_primary() throws RException {

		int leftParentEntryCount = parentNodes[0].getEntryQueue().size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();
		int updateCount = 0;

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*(B+Bx)
		/*****************************************************/
		updateCount += _update_no_primary(lastLeftEntryCount, leftParentEntryCount - 1, 0, rightParentEntryCount - 1);

		/*****************************************************/
		// A*Bx
		/*****************************************************/
		updateCount += _update_no_primary(0, lastLeftEntryCount - 1, lastRightEntryCount, rightParentEntryCount - 1);

		lastLeftEntryCount = leftParentEntryCount;
		lastRightEntryCount = rightParentEntryCount;

		return updateCount;

	}

	protected int _update_no_primary(int leftBegin, int leftEnd, int rightBegin, int rightEnd) throws RException {

		if (leftBegin > leftEnd) {
			return 0;
		}

		if (rightBegin > rightEnd) {
			return 0;
		}

		int updateCount = 0;
		IREntryQueue leftQueue = parentNodes[0].getEntryQueue();
		IREntryQueue rightQueue = parentNodes[1].getEntryQueue();

		for (int i = leftBegin; i <= leftEnd; ++i) {

			IRReteEntry leftEntry = leftQueue.getEntryAt(i);
			if (leftEntry == null || leftEntry.isDroped()) {
				continue;
			}

			for (int j = rightBegin; j <= rightEnd; ++j) {

				IRReteEntry rightEntry = rightQueue.getEntryAt(j);
				if (rightEntry == null || rightEntry.isDroped()) {
					continue;
				}

				if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {
					++updateCount;
				}
			}
		}

		return updateCount;
	}

	@Override
	public int getParentVisitIndex(int index) {

		switch (index) {
		case 0:
			return lastLeftEntryCount;

		case 1:
			return lastRightEntryCount;

		default:
			return -1;
		}
	}

	public boolean isNodeFresh() {
		return lastLeftEntryCount == 0 && lastRightEntryCount == 0;
	}

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	public void setInheritIndexs(InheritIndex[] inheritIndexs) {
		this.inheritIndexs = inheritIndexs;
	}

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		/*********************************************/
		// idle
		/*********************************************/
		int leftParentEntryCount = parentNodes[0].getEntryQueue().size();
		if (leftParentEntryCount == 0) {
			++nodeIdleCount;
			return 0;
		}
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();
		if (rightParentEntryCount == 0) {
			++nodeIdleCount;
			return 0;
		}

		if (lastLeftEntryCount == leftParentEntryCount && lastRightEntryCount == rightParentEntryCount) {
			++nodeIdleCount;
			return 0;
		}

		/*********************************************/
		// Process
		/*********************************************/
		return _update_no_primary();
	}
}
