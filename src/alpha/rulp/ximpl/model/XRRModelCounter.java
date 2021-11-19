package alpha.rulp.ximpl.model;

import static alpha.rulp.rule.RReteStatus.REMOVE;

import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModelCounter;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IREntryQueue.IREntryCounter;
import alpha.rulp.ximpl.node.RReteType;

public class XRRModelCounter implements IRModelCounter {

	private XRModel model;

	public XRRModelCounter(XRModel model) {
		super();
		this.model = model;
	}

	private int nodeExecCount = 0;

	private int nodeIdleCount = 0;

	private int queryMatchCount = 0;

	private int stateChangeCount = 0;

	public void incQueryMatchCount() {
		queryMatchCount++;
	}

	public void incNodeExecCount() {
		nodeExecCount++;
	}

	public void incStateChangeCount() {
		stateChangeCount++;
	}

	public void incNodeIdleCount() {
		nodeIdleCount++;
	}

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

	@Override
	public IRModel getModel() {
		return model;
	}

	@Override
	public int getProcessQueueMaxNodeCount() {
		return model.updateQueue.getMaxNodeCount();
	}

	@Override
	public int getQueryFetchCount() {

		int totalCount = 0;

		for (IRReteNode node : model.nodeGraph.getNodeMatrix().getAllNodes()) {
			totalCount += node.getEntryQueue().getQueryFetchCount();
		}

		return totalCount;
	}

	@Override
	public int getRuleCount() {
		return model.nodeGraph.listNodes(RReteType.RULE).size();
	}

	@Override
	public int getStatementCount() {

		int totalCount = 0;
		int nullCount = 0;
		int dropCount = 0;

		for (IRReteNode rootNode : model.nodeGraph.listNodes(RReteType.ROOT0)) {
			IREntryCounter rootEntryCounter = rootNode.getEntryQueue().getEntryCounter();
			totalCount += rootEntryCounter.getEntryTotalCount();
			nullCount += rootEntryCounter.getEntryNullCount();
			dropCount += rootEntryCounter.getEntryCount(REMOVE);
		}

		return totalCount - nullCount - dropCount;
	}

}
