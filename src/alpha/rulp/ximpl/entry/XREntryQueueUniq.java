package alpha.rulp.ximpl.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueUniq extends XREntryQueueMulit implements IREntryQueueUniq {

	static class UniqEntry {

		public IRReteEntry entry;

		public int index;

		public UniqEntry(IRReteEntry entry, int index) {
			super();
			this.entry = entry;
			this.index = index;
		}
	}

	protected Map<String, UniqEntry> uniqEntryMap = new HashMap<>();

	public XREntryQueueUniq(int entryLength) {
		super(entryLength);
	}

	@Override
	public boolean addEntry(IRReteEntry newEntry) throws RException {

		String uniqName = ReteUtil.uniqName(newEntry);
		if (RuleUtil.isModelTrace()) {
			System.out.println("\t(" + uniqName + ")");
		}

		++nodeUpdateCount;

		UniqEntry oldEntry = uniqEntryMap.get(uniqName);

		/*******************************************************/
		// Entry not exist
		/*******************************************************/
		if (oldEntry == null || oldEntry.entry.isDroped()) {

			int index = size();

			uniqEntryMap.put(uniqName, new UniqEntry(newEntry, index));
			if (entryList == null) {
				entryList = new ArrayList<>();
			}

			entryList.add(newEntry);

			return true;

		} else {

			++entryRedundant;
			return false;

		}
	}

	@Override
	public void cleanCache() {
		uniqEntryMap.clear();
		super.cleanCache();
	}

	@Override
	public int doGC() {

		Iterator<Entry<String, UniqEntry>> it = uniqEntryMap.entrySet().iterator();
		while (it.hasNext()) {

			Entry<String, UniqEntry> e = it.next();

			IRReteEntry entry = e.getValue().entry;
			if (entry == null || entry.getStatus() == null) {
				it.remove();
				continue;
			}
		}

		return super.doGC();
	}

	public String getCacheInfo() {

		if (uniqEntryMap == null || uniqEntryMap.isEmpty()) {
			return super.getCacheInfo();
		}

		return ReteUtil.combine(super.getCacheInfo(), "uniqEntryMap: size=" + uniqEntryMap.size());
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.UNIQ;
	}

	@Override
	public IRReteEntry getStmt(String uniqName) throws RException {
		UniqEntry entry = uniqEntryMap.get(uniqName);
		return entry == null ? null : entry.entry;
	}

	@Override
	public int getStmtIndex(String uniqName) throws RException {
		UniqEntry entry = uniqEntryMap.get(uniqName);
		return entry == null ? -1 : entry.index;
	}

	@Override
	public int size() {
		return relocateSize != -1 ? relocateSize : super.size();
	}

	protected int relocateSize = -1;

	@Override
	public void relocate(int relocatePos, List<Integer> stmtIndexs) throws RException {

		if (relocatePos == -1) {
			relocateSize = -1;
			return;
		}

		if (stmtIndexs == null || stmtIndexs.isEmpty()) {
			relocateSize = relocatePos;
			return;
		}

		// no need relocated since all statements are used
		if ((stmtIndexs.size() + relocatePos) == super.size()) {
			relocateSize = -1;
			return;
		}

		Collections.sort(stmtIndexs);

		relocateSize = relocatePos;

		for (int stmtIndex : stmtIndexs) {

			// no need update
			if (stmtIndex == relocateSize) {
				relocateSize++;
				continue;
			}

			// Swap entries
			IRReteEntry a = entryList.get(relocateSize);
			IRReteEntry b = entryList.get(stmtIndex);

			entryList.set(stmtIndex, a);
			entryList.set(relocateSize, b);

			uniqEntryMap.get(ReteUtil.uniqName(a)).index = stmtIndex;
			uniqEntryMap.get(ReteUtil.uniqName(b)).index = relocateSize;
			relocateSize++;
		}
	}
}
