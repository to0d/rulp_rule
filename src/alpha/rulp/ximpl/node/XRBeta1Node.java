package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBeta1Node extends XRBeta0Node {

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		/*********************************************/
		// idle
		/*********************************************/
		int rightSingleMaxCount = parentNodes[1].getEntryQueue().size();
		if (lastRightEntryCount >= rightSingleMaxCount) {
			++nodeIdleCount;
			return 0;
		}

		IRReteEntry rightEntry = parentNodes[1].getEntryQueue().getEntryAt(rightSingleMaxCount - 1);
		if (rightEntry == null || rightEntry.isDroped()) {
			++nodeIdleCount;
			return 0;
		}

		IREntryQueue leftEntryQueue = parentNodes[0].getEntryQueue();
		int leftParentEntryCount = leftEntryQueue.size();
		if (leftParentEntryCount == 0) {
			++nodeIdleCount;
			return 0;
		}

		/*********************************************/
		// Process
		/*********************************************/

		int updateCount = 0;

		try {

			for (int i = lastLeftEntryCount; i < leftParentEntryCount; ++i) {

				IRReteEntry leftEntry = leftEntryQueue.getEntryAt(i);
				if (leftEntry == null || leftEntry.isDroped()) {
					continue;
				}

				if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {
					++updateCount;
				}
			}

			return updateCount;

		} finally {

			lastLeftEntryCount = leftParentEntryCount;
			lastRightEntryCount = rightSingleMaxCount;
		}

	}

}
