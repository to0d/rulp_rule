package alpha.rulp.ximpl.search;

import alpha.rulp.lang.RException;

public interface ICheckValue<T> {

	public boolean isValid(T obj) throws RException;
}