package alpha.rulp.ximpl.entry;

import java.util.ArrayList;
import java.util.LinkedList;

import alpha.rulp.lang.RException;

public class XFixEntryArray<T extends IFixEntry> implements IFixEntryArray<T> {

	public static boolean REUSE_ENTRY_ID = true;

	protected ArrayList<T> entryArray = new ArrayList<>();

	protected int entryCount = 0;

	protected int createdCount = 0;

	protected int removedCount = 0;

	public int getCreatedCount() {
		return createdCount;
	}

	public int getRemovedCount() {
		return removedCount;
	}

	protected LinkedList<Integer> freeEntryIdList = new LinkedList<>();

	public void addEntry(T entry) {

		int entryId = -1;

		if (REUSE_ENTRY_ID && !freeEntryIdList.isEmpty()) {

			entryId = freeEntryIdList.pollFirst();
			if (XREntryTable.TRACE) {
				System.out.println("    reuse entry: id=" + entryId + ", entry=" + entry);
			}

			entryArray.set(entryId - 1, entry);

		} else {

			entryId = entryArray.size() + 1;
			if (XREntryTable.TRACE) {
				System.out.println("    new-entry: id=" + entryId + ", entry=" + entry);
			}

			entryArray.add(entry);
		}

		entry.setEntryId(entryId);
		++entryCount;
		++createdCount;
	}

	public int doGC() throws RException {

		int update = 0;

		int size = entryArray.size();
		for (int i = 0; i < size; ++i) {
			T entry = entryArray.get(i);
			if (entry != null && entry.isDroped()) {
				entryArray.set(i, null);
				++update;
			}
		}

		return update;
	}

	public T getEntry(int entryId) throws RException {

		if (entryId == 0 || entryId > entryArray.size()) {
			return null;
		}

		return entryArray.get(entryId - 1);
	}

	public int getEntryCount() {
		return entryCount;
	}

	public int getEntryMaxId() {
		return entryArray.size();
	}

	public void removeEntry(T entry) throws RException {

		int entryId = entry.getEntryId();

		this.entryArray.set(entryId - 1, null);
		--entryCount;
		++removedCount;

		if (REUSE_ENTRY_ID) {
			this.freeEntryIdList.addLast(entryId);
		}
	}
}