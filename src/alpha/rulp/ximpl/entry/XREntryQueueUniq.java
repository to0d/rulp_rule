package alpha.rulp.ximpl.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueUniq extends XREntryQueueMulit {

	protected Map<String, IRReteEntry> uniqEntryMap = new HashMap<>();

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

		IRReteEntry oldEntry = uniqEntryMap.get(uniqName);

		/*******************************************************/
		// Entry not exist
		/*******************************************************/
		if (oldEntry == null || oldEntry.isDroped()) {

			uniqEntryMap.put(uniqName, newEntry);
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

		Iterator<Entry<String, IRReteEntry>> it = uniqEntryMap.entrySet().iterator();
		while (it.hasNext()) {

			Entry<String, IRReteEntry> e = it.next();

			IRReteEntry entry = e.getValue();
			if (entry == null || entry.getStatus() == null) {
				it.remove();
				continue;
			}
		}

		return super.doGC();
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.UNIQ;
	}

	public IRReteEntry getStmt(String uniqName) throws RException {
		return uniqEntryMap.get(uniqName);
	}
}
