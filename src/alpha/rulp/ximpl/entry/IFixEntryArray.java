package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;

public interface IFixEntryArray<T> {

	public int getEntryMaxId();

	public T getEntry(int entryId) throws RException;
}
