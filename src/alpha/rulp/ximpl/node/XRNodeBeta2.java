package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeBeta2 extends XRNodeBeta0 {

	protected int leftBeginIndex = 0;

	protected Set<Integer> leftUsedIndexSet = new HashSet<>();

	private Boolean sameInherit = null;

	public XRNodeBeta2(String instanceName) {
		super(instanceName);
	}

	protected Map<String, List<Integer>> _buildPrimaryIndexMap(IREntryQueue entryQueue, int begin, int end,
			int primayIndex, Set<String> supportKeys, List<String> keysList, boolean isLeft) throws RException {

		Map<String, List<Integer>> primaryMap = new HashMap<>();

		for (int i = begin; i <= end; ++i) {

			// ignore used main inherit entry
			if (isLeft && _isIgnoreLeftIndex(i)) {
				continue;
			}

			IRReteEntry entry = entryQueue.getEntryAt(i);
			if (entry == null || entry.isDroped()) {
				continue;
			}

			IRObject primayObj = entry.get(primayIndex);
			String primaryKey = RulpUtil.toUniqString(primayObj);

			if (supportKeys != null && !supportKeys.contains(primaryKey)) {
				continue;
			}

			List<Integer> list = primaryMap.get(primaryKey);
			if (list == null) {
				list = new LinkedList<>();
				primaryMap.put(primaryKey, list);
				if (keysList != null) {
					keysList.add(primaryKey);
				}
			}

			list.add(i);
		}

		return primaryMap;
	}

	@Override
	protected IRReteEntry _getNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		if (!this._isSameInherit()) {
			return super._getNewEntry(leftEntry, rightEntry);
		}

		return leftEntry;
	}

	protected void _ignoreLeftIndex(int leftIndex) {

		if (leftIndex == leftBeginIndex) {

			while (++leftBeginIndex < lastLeftEntryCount) {
				if (!leftUsedIndexSet.remove(leftBeginIndex)) {
					break;
				}
			}

		} else {

			leftUsedIndexSet.add(leftIndex);
		}
	}

	protected boolean _isIgnoreLeftIndex(int leftIndex) {
		return leftIndex < leftBeginIndex || leftUsedIndexSet.contains(leftIndex);
	}

	private boolean _isSameInherit() {

		if (sameInherit == null) {
			sameInherit = ReteUtil.isSameInheritIndex(inheritIndexs);
		}

		return sameInherit;
	}

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
		updateCount += _update_no_primary(leftBeginIndex, lastLeftEntryCount - 1, lastRightEntryCount,
				rightParentEntryCount - 1);

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

		IREntryQueue leftQueue = parentNodes[0].getEntryQueue();
		IREntryQueue rightQueue = parentNodes[1].getEntryQueue();
		int updateCount = 0;

		NEXT_LEFT: for (int leftIndex = leftBegin; leftIndex <= leftEnd; ++leftIndex) {

			// ignore used main inherit entry
			if (_isIgnoreLeftIndex(leftIndex)) {
				continue;
			}

			IRReteEntry leftEntry = leftQueue.getEntryAt(leftIndex);
			if (leftEntry == null || leftEntry.isDroped()) {
				continue;
			}

			for (int rightIndex = rightBegin; rightIndex <= rightEnd; ++rightIndex) {

				IRReteEntry rightEntry = rightQueue.getEntryAt(rightIndex);

				if (rightEntry == null || rightEntry.isDroped()) {
					continue;
				}

				if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {

					++updateCount;

					// The main inherit entry will generate one child entry
					// ignore other right entry
					_ignoreLeftIndex(leftIndex);
					continue NEXT_LEFT;
				}
			}
		}

		return updateCount;
	}

	protected int _update_primary() throws RException {

		int leftParentEntryCount = parentNodes[0].getEntryQueue().size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();
		int updateCount = 0;

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*(B+Bx)
		/*****************************************************/
		updateCount += _update_primary(lastLeftEntryCount, leftParentEntryCount - 1, 0, rightParentEntryCount - 1);

		/*****************************************************/
		// A*Bx
		/*****************************************************/
		updateCount += _update_primary(leftBeginIndex, lastLeftEntryCount - 1, lastRightEntryCount,
				rightParentEntryCount - 1);

		lastLeftEntryCount = leftParentEntryCount;
		lastRightEntryCount = rightParentEntryCount;
		return updateCount;

	}

	protected int _update_primary(int leftBegin, int leftEnd, int rightBegin, int rightEnd) throws RException {

		if (leftBegin > leftEnd) {
			return 0;
		}

		if (rightBegin > rightEnd) {
			return 0;
		}

		ArrayList<String> keysList = new ArrayList<>();

		Map<String, List<Integer>> leftPrimaryMap = _buildPrimaryIndexMap(parentNodes[0].getEntryQueue(), leftBegin,
				leftEnd, getPrimaryJoinIndex().leftIndex, null, keysList, true);
		if (leftPrimaryMap.isEmpty()) {
			return 0;
		}

		Map<String, List<Integer>> rightPrimaryMap = _buildPrimaryIndexMap(parentNodes[1].getEntryQueue(), rightBegin,
				rightEnd, getPrimaryJoinIndex().rightIndex, leftPrimaryMap.keySet(), null, false);
		if (rightPrimaryMap.isEmpty()) {
			return 0;
		}

		IREntryQueue leftQueue = parentNodes[0].getEntryQueue();
		IREntryQueue rightQueue = parentNodes[1].getEntryQueue();
		int updateCount = 0;

		for (String primayKey : keysList) {

			List<Integer> rightList = rightPrimaryMap.get(primayKey);
			if (rightList == null) {
				continue;
			}

			List<Integer> leftList = leftPrimaryMap.get(primayKey);
			Iterator<Integer> leftIter = leftList.iterator();

			NEXT_LEFT: while (leftIter.hasNext()) {

				int leftIndex = leftIter.next();

				// ignore used main inherit entry
				if (_isIgnoreLeftIndex(leftIndex)) {
					continue NEXT_LEFT;
				}

				IRReteEntry leftEntry = leftQueue.getEntryAt(leftIndex);

				Iterator<Integer> rightIter = rightList.iterator();

				while (rightIter.hasNext()) {

					int rightIndex = rightIter.next();
					IRReteEntry rightEntry = rightQueue.getEntryAt(rightIndex);

					if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {

						++updateCount;

						// The main inherit entry will generate one child entry
						// ignore other right entry
						_ignoreLeftIndex(leftIndex);
						continue NEXT_LEFT;
					}
				}
			}

		}

		return updateCount;
	}

}
