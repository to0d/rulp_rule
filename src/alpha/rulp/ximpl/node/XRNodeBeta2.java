package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.HashMap;
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

	protected int lastLeftUnMatchEntryCount = 0;

	protected ArrayList<IRReteEntry> leftUnMatchEntryList = new ArrayList<>();

	protected Boolean sameInherit = null;

	public XRNodeBeta2(String instanceName) {
		super(instanceName);
	}

	protected Map<String, List<Integer>> _buildPrimaryIndexMap(IREntryQueue entryQueue, int begin, int end,
			int primayIndex, Set<String> supportKeys) throws RException {

		Map<String, List<Integer>> primaryMap = new HashMap<>();

		for (int i = begin; i <= end; ++i) {

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
			}

			list.add(i);
		}

		return primaryMap;
	}

	protected Map<String, List<Integer>> _buildPrimaryIndexMap(List<IRReteEntry> entryQueue, int begin, int end,
			int primayIndex, List<String> keysList) throws RException {

		Map<String, List<Integer>> primaryMap = new HashMap<>();

		for (int i = begin; i <= end; ++i) {

			IRReteEntry entry = entryQueue.get(i);
			if (entry == null || entry.isDroped()) {
				continue;
			}

			IRObject primayObj = entry.get(primayIndex);
			String primaryKey = RulpUtil.toUniqString(primayObj);

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

	protected boolean _isSameInherit() {

		if (sameInherit == null) {
			sameInherit = ReteUtil.isSameInheritIndex(inheritIndexs);
		}

		return sameInherit;
	}

	protected int _update_no_primary(int limit) throws RException {

		int leftParentEntryCount = leftUnMatchEntryList.size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();

		int updateCount = 0;

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*(B+Bx)
		/*****************************************************/
		updateCount += _update_no_primary(lastLeftUnMatchEntryCount, leftParentEntryCount - 1, 0,
				rightParentEntryCount - 1);

		/*****************************************************/
		// A*Bx
		/*****************************************************/
		updateCount += _update_no_primary(0, lastLeftUnMatchEntryCount - 1, lastRightEntryCount,
				rightParentEntryCount - 1);

		lastLeftUnMatchEntryCount = leftParentEntryCount;
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

		IREntryQueue rightQueue = parentNodes[1].getEntryQueue();
		int updateCount = 0;

		NEXT_LEFT: for (int leftIndex = leftBegin; leftIndex <= leftEnd; ++leftIndex) {

			IRReteEntry leftEntry = leftUnMatchEntryList.get(leftIndex);
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
					leftUnMatchEntryList.set(leftIndex, null);
					continue NEXT_LEFT;
				}
			}
		}

		return updateCount;
	}

	protected int _update_primary(int limit) throws RException {

		int leftParentEntryCount = leftUnMatchEntryList.size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();
		int updateCount = 0;

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*(B+Bx)
		/*****************************************************/
		updateCount += _update_primary(lastLeftUnMatchEntryCount, leftParentEntryCount - 1, 0,
				rightParentEntryCount - 1);

		/*****************************************************/
		// A*Bx
		/*****************************************************/
		updateCount += _update_primary(0, lastLeftUnMatchEntryCount - 1, lastRightEntryCount,
				rightParentEntryCount - 1);

		lastLeftUnMatchEntryCount = leftParentEntryCount;
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

		Map<String, List<Integer>> leftPrimaryMap = _buildPrimaryIndexMap(leftUnMatchEntryList, leftBegin, leftEnd,
				_getPrimaryJoinIndex().leftIndex, keysList);
		if (leftPrimaryMap.isEmpty()) {
			return 0;
		}

		Map<String, List<Integer>> rightPrimaryMap = _buildPrimaryIndexMap(parentNodes[1].getEntryQueue(), rightBegin,
				rightEnd, _getPrimaryJoinIndex().rightIndex, leftPrimaryMap.keySet());
		if (rightPrimaryMap.isEmpty()) {
			return 0;
		}

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

				IRReteEntry leftEntry = leftUnMatchEntryList.get(leftIndex);
				if (leftEntry == null || leftEntry.isDroped()) {
					continue NEXT_LEFT;
				}

				Iterator<Integer> rightIter = rightList.iterator();

				while (rightIter.hasNext()) {

					int rightIndex = rightIter.next();
					IRReteEntry rightEntry = rightQueue.getEntryAt(rightIndex);

					if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {

						++updateCount;

						// The main inherit entry will generate one child entry
						// ignore other right entry
						leftUnMatchEntryList.set(leftIndex, null);
						continue NEXT_LEFT;
					}
				}
			}

		}

		return updateCount;
	}

	protected boolean _useMapMatch() {

		if (!RETE_BETA_HASHMAP_MATCH_MODE) {
			return false;
		}

		if (_getPrimaryJoinIndex() == null) {
			return false;
		}

		int leftParentEntryCount = leftUnMatchEntryList.size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*B + Ax*Bx
		/*****************************************************/
		int maxUpdateCount = leftParentEntryCount * rightParentEntryCount
				- lastLeftUnMatchEntryCount * lastRightEntryCount;

		return maxUpdateCount > MAP_MATCH_MIN_COUNT;
	}

	@Override
	public String getCacheInfo() {

		if (leftUnMatchEntryList.isEmpty()) {
			return super.getCacheInfo();
		}

		return ReteUtil.combine(super.getCacheInfo(), "leftUnMatchEntryList: size=" + leftUnMatchEntryList.size());
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

		/*********************************************/
		// Update leftUnMatchEntry
		/*********************************************/
		IREntryQueue leftQueue = parentNodes[0].getEntryQueue();
		for (; lastLeftEntryCount < leftParentEntryCount; ++lastLeftEntryCount) {

			IRReteEntry leftEntry = leftQueue.getEntryAt(lastLeftEntryCount);
			if (leftEntry == null || leftEntry.isDroped()) {
				continue;
			}

			leftUnMatchEntryList.add(leftEntry);
		}

		if (lastLeftUnMatchEntryCount == leftUnMatchEntryList.size() && lastRightEntryCount == rightParentEntryCount) {
			++nodeIdleCount;
			return 0;
		}

		/*********************************************/
		// Process
		/*********************************************/
		int updateCount = 0;
		if (_useMapMatch()) {
			updateCount = _update_primary(limit);
		} else {
			updateCount = _update_no_primary(limit);
		}

		/*********************************************/
		// Clean null entries
		/*********************************************/
		if (updateCount > 0) {

			int size = 0;

			for (int i = 0; i < lastLeftUnMatchEntryCount; ++i) {
				IRReteEntry entry = leftUnMatchEntryList.get(i);
				if (entry != null) {
					if (i != size) {
						leftUnMatchEntryList.set(size, entry);
					}
					++size;
				}
			}

			if (size == 0) {

				leftUnMatchEntryList.clear();
				lastLeftUnMatchEntryCount = 0;

			} else {

				while (lastLeftUnMatchEntryCount > size) {
					leftUnMatchEntryList.remove(--lastLeftUnMatchEntryCount);
				}
			}
		}

		return updateCount;
	}

}
