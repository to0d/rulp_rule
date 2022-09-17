package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeZeta0 extends AbsReteNode {

	protected boolean nodeFresh = true;

	protected int[] parentVisitIndexs;

	public XRNodeZeta0(String instanceName) {
		super(instanceName);
	}

	protected boolean _addNewEntry(IRReteEntry[] entryList) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(entryList);

		if (!addReteEntry(newEntry)) {
			return false;
		}

		entryTable.addReference(newEntry, this, entryList);
		return true;
	}

	protected IRReteEntry _getNewEntry(IRReteEntry[] entryList) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			InheritIndex inherit = inheritIndexs[i];
			IRReteEntry parentEntry = entryList[inherit.parentIndex];
			newElements[i] = parentEntry.get(inherit.elementIndex);
		}

		incEntryCreateCount();
		return entryTable.createEntry(null, newElements, ReteUtil.getChildStatus(entryList), false);
	}

	public int _update(int[] parentEntryCount, int middleParentIndex) throws RException {

		int[] lowIndexs = new int[parentCount];
		int[] highIndexs = new int[parentCount];
		int[] visitIndexs = new int[parentCount];

		IRReteEntry[] entryList = new IRReteEntry[parentCount];
		IREntryQueue[] queueList = new IREntryQueue[parentCount];

		for (int i = 0; i < parentCount; ++i) {

			int lowIndex = -1;
			int entryCount = parentEntryCount[i];
			IREntryQueue queue = parentNodes[i].getEntryQueue();

			for (int j = 0; i < entryCount; ++i) {

				IRReteEntry entry = queue.getEntryAt(j);
				if (entry == null || entry.isDroped()) {
					continue;
				}

				lowIndex = j;
				break;
			}

			if (lowIndex == -1) {
				return -1;
			}

			if (lowIndex > parentVisitIndexs[i]) {
				return -1;
			}

//			if (lowIndex == parentVisitIndexs[i]) {
//				return 0;
//			}

			if (i < middleParentIndex) {

				lowIndexs[i] = lowIndex;
				highIndexs[i] = entryCount;

			} else if (i == middleParentIndex) {

				lowIndexs[i] = parentVisitIndexs[i];
				highIndexs[i] = entryCount;

			} else {

				lowIndexs[i] = lowIndex;
				highIndexs[i] = parentVisitIndexs[i];
			}

			if (lowIndexs[i] == highIndexs[i]) {
				return 0;
			}

			visitIndexs[i] = lowIndexs[i];
			queueList[i] = queue;
			entryList[i] = queue.getEntryAt(lowIndexs[i]);

		}

		int updateCount = 0;
		int parentIndex = 0;

		boolean scanNext = false;
		boolean carry = false;

		while (parentIndex < parentCount) {

			// move to next list
			if (scanNext) {

				int index = ++visitIndexs[parentIndex];
				if (index < highIndexs[parentIndex]) {
					entryList[parentIndex] = queueList[parentIndex].getEntryAt(index);
					scanNext = false;
					if (carry) {
						parentIndex = 0;
						carry = false;
					}
//					parentIndex = 0;	
//					entryList[parentIndex] = queueList[parentIndex].getEntryAt(index);
					continue;
				}

				visitIndexs[parentIndex] = lowIndexs[parentIndex];
				entryList[parentIndex] = queueList[parentIndex].getEntryAt(visitIndexs[parentIndex]);
				parentIndex++;
				carry = true;
				continue;
			}

			if (_addNewEntry(entryList)) {
//				System.out.println(RulpUtil.toArray2(visitIndexs));
				++updateCount;
			}

			scanNext = true;
		}

		return updateCount;
	}

	@Override
	public int getParentVisitIndex(int index) {
		return this.parentVisitIndexs[index];
	}

	@Override
	public boolean isNodeFresh() {
		return nodeFresh;
	}

	public void setParentNodes(IRReteNode[] parentNodes) throws RException {

		super.setParentNodes(parentNodes);

		this.parentVisitIndexs = new int[parentCount];
		for (int i = 0; i < parentCount; ++i) {
			this.parentVisitIndexs[i] = 0;
		}
	}

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		int updatedParentCount = 0;

		int[] parentEntryCount = new int[parentCount];
		for (int i = 0; i < parentCount; ++i) {

			int count = parentNodes[i].getEntryQueue().size();
			if (count == 0) {
				++nodeIdleCount;
				return 0;
			}

			if (count > parentVisitIndexs[i]) {
				updatedParentCount++;
			}

			parentEntryCount[i] = count;
		}

		if (updatedParentCount == 0) {
			++nodeIdleCount;
			return 0;
		}

		int updateCount = 0;

		if (nodeFresh) {

			int uc = _update(parentEntryCount, parentCount - 1);
			if (uc == -1) {
				throw new RException("invalid update count");
			}

			updateCount += uc;

		} else {

			for (int i = 0; i < parentCount; ++i) {
				int uc = _update(parentEntryCount, i);
				if (uc == -1) {
					throw new RException("invalid update count");
				}

				updateCount += uc;
			}
		}

		parentVisitIndexs = parentEntryCount;
		nodeFresh = false;

		return updateCount;
	}
}
