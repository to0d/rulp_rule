package alpha.rulp.rule;

import alpha.rulp.lang.RException;

public interface IRRuleCounter {

	public IRRule getRule();

	public int getExecuteCount() throws RException;

	public int getNodeCount() throws RException;

	public int getStatementCount() throws RException;

	public int getEntryCount() throws RException;

	public int getUpdateCount() throws RException;
}
