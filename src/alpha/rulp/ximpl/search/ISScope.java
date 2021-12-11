package alpha.rulp.ximpl.search;

import alpha.rulp.lang.RException;

public interface ISScope<T> {

	public T curValue();

	public int getEvalCount();

	public int getMoveCount();

	public int getResetCount();

	public boolean moveNext() throws RException;

	public void reset();

	public void setChecker(ICheckValue<T> checker) throws RException;
}