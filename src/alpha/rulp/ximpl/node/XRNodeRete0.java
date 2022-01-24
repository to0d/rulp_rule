package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.RRunState;

public class XRNodeRete0 extends AbsReteNode {

	protected int lastEntryCount = 0;

	protected int nodeExecCount = 0;

	public XRNodeRete0(String instanceName) {
		super(instanceName);
	}

	@Override
	public int getNodeExecCount() {
		return nodeExecCount;
	}

	@Override
	public int getParentVisitIndex(int index) {
		return -1;
	}

	@Override
	public RRunState halt() throws RException {
		throw new RException("invalid operation: start root node");
	}

	@Override
	public boolean isNodeFresh() {
		return lastEntryCount == 0 && nodeExecCount == 0;
	}

	@Override
	public int update() throws RException {

		if (this.isTrace()) {
			System.out.println("update: " + this);
		}

		++nodeExecCount;

		int totalEntryCount = this.entryQueue.size();
		if (lastEntryCount >= totalEntryCount) {
			++nodeIdleCount;
			return 0;
		}

		int updateCount = 0;
		if (lastEntryCount < totalEntryCount) {
			updateCount = totalEntryCount - lastEntryCount;
			lastEntryCount = totalEntryCount;
		}

		return updateCount;
	}
}
