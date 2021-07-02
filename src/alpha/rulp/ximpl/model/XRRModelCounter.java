package alpha.rulp.ximpl.model;

public class XRRModelCounter implements IRRModelCounter {

	public int nodeExecCount = 0;

	public int nodeIdleCount = 0;

	public int queryMatchCount = 0;

	public int stateChangeCount = 0;

	public int getNodeExecuteCount() {
		return nodeExecCount;
	}

	public int getNodeIdleCount() {
		return nodeIdleCount;
	}

	public int getQueryMatchCount() {
		return queryMatchCount;
	}

	public int getStateChangeCount() {
		return stateChangeCount;
	}

}
