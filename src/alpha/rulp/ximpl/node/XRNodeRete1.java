package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeRete1 extends AbsReteNode {

	protected int lastParentVisitIndex = 0;

	public XRNodeRete1(String instanceName) {
		super(instanceName);
	}

	@Override
	public int getParentVisitIndex(int index) {
		return index == 0 ? lastParentVisitIndex : -1;
	}

	@Override
	public boolean isNodeFresh() {
		return lastParentVisitIndex == 0;
	}

	@Override
	public int update(int limit) throws RException {
		return _update(0);
	}

//	@Override
//	protected List<RUniqInfo> _rebuildUniqInfoList() {
//
//		List<RUniqInfo> superUniqList = super._rebuildUniqInfoList();
//
//		List<RUniqInfo> parentUniqList = parentNodes[0].listUniqInfos();
//		if (parentUniqList.isEmpty()) {
//			return superUniqList;
//		}
//
//		if (superUniqList.isEmpty()) {
//			return parentUniqList;
//		}
//
//		ArrayList<RUniqInfo> uniqList = new ArrayList<>();
//		uniqList.addAll(superUniqList);
//		uniqList.addAll(parentUniqList);
//
//		return uniqList;
//	}

	public int _update(int maxParentCount) throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this + ", max=" + maxParentCount);
		}

		++nodeExecCount;

		/*********************************************/
		// idle
		/*********************************************/
		IREntryQueue parentEntryQueue = parentNodes[0].getEntryQueue();
		int parentEntryCount = parentEntryQueue.size();
		if (maxParentCount > 0 && parentEntryCount > maxParentCount) {
			parentEntryCount = maxParentCount;
		}

		if (lastParentVisitIndex == parentEntryCount) {
			++nodeIdleCount;
			return 0;
		}

		/*********************************************/
		// Process
		/*********************************************/
		int updateCount = 0;
		for (; lastParentVisitIndex < parentEntryCount; ++lastParentVisitIndex) {

			IRReteEntry entry = parentEntryQueue.getEntryAt(lastParentVisitIndex);
			if (entry == null || entry.getStatus() == RReteStatus.REMOVE) {
				continue;
			}

			if (addReteEntry(entry)) {
				++updateCount;
			}
		}

		return updateCount;

	}

}
