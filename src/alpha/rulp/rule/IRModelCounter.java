package alpha.rulp.rule;

import alpha.rulp.lang.RException;

public interface IRModelCounter {

	public IRModel getModel();

	public int getNodeExecuteCount();

	public int getNodeIdleCount();

	public int getProcessQueueMaxNodeCount();

	public int getQueryFetchCount();

	public int getQueryMatchCount();

	public int getRuleCount();

	public int getStateChangeCount();

	public int getStatementCount() throws RException;

}
