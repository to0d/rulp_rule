package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBetaNode1 extends XRBetaNode0 {

	private boolean rightConstNodeIsRun = false;

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		IRReteNode rightNode = parentNodes[1];

		/*********************************************/
		// idle
		/*********************************************/
		int rightSingleMaxCount = rightNode.getEntryQueue().size();
		if (!rightConstNodeIsRun && lastRightEntryCount >= rightSingleMaxCount) {
			++nodeIdleCount;
			return 0;
		}

		IRReteEntry rightEntry = rightNode.getEntryQueue().getEntryAt(rightSingleMaxCount - 1);
		if (!rightConstNodeIsRun && (rightEntry == null || rightEntry.isDroped())) {
			++nodeIdleCount;
			return 0;
		}

		/********************************************************/
		// - If the right node is const node & it has been active
		// - Let this beta1 node to to auto child of left node
		/********************************************************/
		IRReteNode leftNode = parentNodes[0];
		if (rightNode.getReteType() == RReteType.CONST && !rightConstNodeIsRun) {
//			leftNode.setChildNodeUpdateMode(this, true);
			rightConstNodeIsRun = true;
		}

		IREntryQueue leftEntryQueue = leftNode.getEntryQueue();
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
