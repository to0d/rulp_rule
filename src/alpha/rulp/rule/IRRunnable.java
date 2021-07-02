package alpha.rulp.rule;

import alpha.rulp.lang.RException;

public interface IRRunnable {

	public int getPriority();

	public RRunState getRunState();

	public RRunState halt() throws RException;

	public int start(int priority, int limit) throws RException;

}
