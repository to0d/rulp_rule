package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.OptimizeUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint2;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRBeta0Node extends XRReteNode2 implements IRBetaNode {

	public static int MAP_MATCH_MIN_COUNT = 100;

	static final boolean RETE_BETA_HASHMAP_MATCH_MODE = true;

	protected static Map<String, List<IRReteEntry>> _buildPrimaryMap(IREntryQueue entryQueue, int begin, int end,
			int primayIndex, Set<String> supportKeys, List<String> keysList) throws RException {

		Map<String, List<IRReteEntry>> primaryMap = new HashMap<>();

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

			List<IRReteEntry> list = primaryMap.get(primaryKey);
			if (list == null) {
				list = new LinkedList<>();
				primaryMap.put(primaryKey, list);
				if (keysList != null) {
					keysList.add(primaryKey);
				}
			}

			list.add(entry);
		}

		return primaryMap;
	}

	protected ArrayList<JoinIndex> joinIndexList = new ArrayList<>();

	protected JoinIndex primaryJoinIndex = null;

	@Override
	protected boolean _match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		this.nodeMatchCount++;

		if (primaryJoinIndex != null) {
			if (!ReteUtil.equal(leftEntry.get(primaryJoinIndex.leftIndex),
					rightEntry.get(primaryJoinIndex.rightIndex))) {
				return false;
			}
		}

		for (JoinIndex joinIndex : joinIndexList) {
			if (!ReteUtil.equal(leftEntry.get(joinIndex.leftIndex), rightEntry.get(joinIndex.rightIndex))) {
				return false;
			}
		}

		if (this.constraint2List != null) {

			IRFrame consFrame = RNodeFactory.createNodeFrame(this);
			RulpUtil.incRef(consFrame);

			try {
				for (IRConstraint2 constraint : constraint2List) {
					if (!constraint.addEntry(leftEntry, rightEntry, this.getModel().getInterpreter(), consFrame)) {
						return false;
					}
				}
			} finally {
				consFrame.release();
				RulpUtil.decRef(consFrame);
			}
		}

		return true;
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
		updateCount += _update_primary(0, lastLeftEntryCount - 1, lastRightEntryCount, rightParentEntryCount - 1);

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

		Map<String, List<IRReteEntry>> leftPrimaryMap = _buildPrimaryMap(parentNodes[0].getEntryQueue(), leftBegin,
				leftEnd, primaryJoinIndex.leftIndex, null, keysList);
		if (leftPrimaryMap.isEmpty()) {
			return 0;
		}

		Map<String, List<IRReteEntry>> rightPrimaryMap = _buildPrimaryMap(parentNodes[1].getEntryQueue(), rightBegin,
				rightEnd, primaryJoinIndex.rightIndex, leftPrimaryMap.keySet(), null);
		if (rightPrimaryMap.isEmpty()) {
			return 0;
		}

		int updateCount = 0;

		for (String primayKey : keysList) {

			List<IRReteEntry> rightList = rightPrimaryMap.get(primayKey);
			if (rightList == null) {
				continue;
			}

			List<IRReteEntry> leftList = leftPrimaryMap.get(primayKey);
			Iterator<IRReteEntry> leftIter = leftList.iterator();

			while (leftIter.hasNext()) {

				IRReteEntry leftEntry = leftIter.next();
				Iterator<IRReteEntry> rightIter = rightList.iterator();

				while (rightIter.hasNext()) {

					IRReteEntry rightEntry = rightIter.next();
					if (_match(leftEntry, rightEntry) && _addNewEntry(leftEntry, rightEntry)) {
						++updateCount;
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

		if (primaryJoinIndex == null) {
			return false;
		}

		int leftParentEntryCount = parentNodes[0].getEntryQueue().size();
		int rightParentEntryCount = parentNodes[1].getEntryQueue().size();

		/*****************************************************/
		// (A+Ax)*(B+Bx) - A*B ==> Ax*B + A*Bx + Ax*Bx
		/*****************************************************/

		/*****************************************************/
		// Ax*B + Ax*Bx
		/*****************************************************/
		int maxUpdateCount = leftParentEntryCount * rightParentEntryCount - lastLeftEntryCount * lastRightEntryCount;

		return maxUpdateCount > MAP_MATCH_MIN_COUNT;
	}

	public void addJoinIndex(JoinIndex joinIndex) {

		if (primaryJoinIndex == null) {
			primaryJoinIndex = joinIndex;
		}

		joinIndexList.add(joinIndex);
	}

	@Override
	public List<JoinIndex> getJoinIndexList() {
		return joinIndexList;
	}

	@Override
	public String getMatchDescription() {

		if (joinIndexList == null) {
			return super.getMatchDescription();
		}

		String des = super.getMatchDescription();
		if (des == null) {
			des = "";
		} else {
			des += ",";
		}

		des += OptimizeUtil.toString(joinIndexList);

		return des;
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
		if (_useMapMatch()) {
			return _update_primary();
		} else {
			return _update_no_primary();
		}
	}

}
