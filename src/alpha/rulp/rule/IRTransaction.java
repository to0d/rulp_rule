package alpha.rulp.rule;

import alpha.rulp.lang.RException;

public interface IRTransaction {

	public void commit() throws RException;

	public void rollback() throws RException;
}
