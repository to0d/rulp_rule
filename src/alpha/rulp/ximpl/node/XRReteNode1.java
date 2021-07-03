package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.ximpl.entry.IREntryQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRReteNode1 extends AbsReteNode {

	protected int lastParentVisitIndex = 0;

//	protected List<IRConstraint1> matchNodeList = null;

//	protected boolean _match(IRReteEntry entry) throws RException {
//
//		if (entry == null || entry.getStatus() == REMOVED) {
//			return false;
//		}
//
//		if (constraintList == null) {
//			return true;
//		}
//
//		++nodeMatchCount;
//
//		for (IRConstraint1 matchNode : constraintList) {
//			if (!matchNode.addEntry(entry, this.getModel().getInterpreter(), this.getNodeFrame(true))) {
//				return false;
//			}
//		}
//
//		return true;
//	}

//	public void addMatchNode(IRConstraint1 matchNode) {
//
//		if (matchNodeList == null) {
//			matchNodeList = new LinkedList<>();
//		}
//
//		matchNodeList.add(matchNode);
//	}

//	@Override
//	public String getMatchDescription() {
//
//		if (matchNodeList == null) {
//			return null;
//		}
//
//		return OptimizeUtil.toString(matchNodeList);
//	}

//	public List<IRConstraint1> getMatchNodeList() {
//		return matchNodeList;
//	}

	@Override
	public int getParentVisitIndex(int index) {
		return index == 0 ? lastParentVisitIndex : -1;
	}

	@Override
	public boolean isNodeFresh() {
		return lastParentVisitIndex == 0;
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
		IREntryQueue parentEntryQueue = parentNodes[0].getEntryQueue();
		int parentEntryCount = parentEntryQueue.size();
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
