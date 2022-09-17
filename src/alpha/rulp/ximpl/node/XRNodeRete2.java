package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.constraint.IRConstraint2;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class XRNodeRete2 extends AbsReteNode implements IRBetaNode {

	protected List<IRConstraint2> constraint2List = null;

	protected int lastLeftEntryCount = 0;

	protected int lastRightEntryCount = 0;

	public XRNodeRete2(String instanceName) {
		super(instanceName);
	}

	protected boolean _addNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(leftEntry, rightEntry);

		if (!addReteEntry(newEntry)) {
			return false;
		}

		entryTable.addReference(newEntry, this, leftEntry, rightEntry);
		return true;
	}

	protected IRReteEntry _getNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			InheritIndex inherit = inheritIndexs[i];
			IRReteEntry parentEntry = inherit.parentIndex == 0 ? leftEntry : rightEntry;
			newElements[i] = parentEntry.get(inherit.elementIndex);
		}

		incEntryCreateCount();
		return entryTable.createEntry(null, newElements, ReteUtil.getChildStatus(leftEntry, rightEntry), false);
	}

	protected abstract boolean _match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException;

	protected int _update_no_primary(int limit) throws RException {

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
	public boolean addConstraint2(IRConstraint2 constraint) throws RException {

		if (constraint2List == null) {
			constraint2List = new ArrayList<>();
		}

		constraint2List.add(constraint);
		return true;
	}

	@Override
	public int getConstraint2Count() {
		return constraint2List == null ? 0 : constraint2List.size();
	}

	@Override
	public List<IRConstraint2> getConstraint2List() {
		return constraint2List;
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

	@Override
	public int update(int limit) throws RException {

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
		return _update_no_primary(limit);
	}
}
