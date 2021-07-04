package alpha.rulp.ximpl.entry;

import alpha.rulp.lang.RException;

public interface IFixEntryArray<T> {

	public int getCreatedCount();

	public T getEntry(int entryId) throws RException;

	public int getEntryCount();

	public int getEntryMaxId();

	public int getRemovedCount();
}
