//package alpha.rulp.ximpl.node;
//
//import alpha.rulp.lang.RException;
//import alpha.rulp.ximpl.entry.XREntryQueueSingle;
//
//public class XRNodeAlph1 extends XRNodeRete1 {
//
//	@Override
//	public int update() throws RException {
//
//		if (this.isTrace()) {
//			System.out.println("update: " + this);
//		}
//
//		++nodeExecCount;
//
//		// Single node's parent must be single entry queue
//		XREntryQueueSingle parentEntryQueue = (XREntryQueueSingle) parentNodes[0].getEntryQueue();
//
//		/*********************************************/
//		// idle
//		/*********************************************/
//		int parentSingleMaxCount = parentEntryQueue.size();
//		if (lastParentVisitIndex >= parentSingleMaxCount) {
//			++nodeIdleCount;
//			return 0;
//		}
//
//		/*********************************************/
//		// Process
//		/*********************************************/
//		addReteEntry(parentEntryQueue.getEntryAt(parentSingleMaxCount - 1));
//
//		lastParentVisitIndex = parentSingleMaxCount;
//		return 1;
//	}
//}
