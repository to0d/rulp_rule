package alpha.rulp.ximpl.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;

public class XREntryQueueUniqEntryList extends XREntryQueueMulitEntryList implements IRRListener1<IRReteEntry> {

	protected Map<String, IRReteEntry> uniqEntryMap = new HashMap<>();

	public XREntryQueueUniqEntryList(int entryLength) {
		super(entryLength);
	}

	@Override
	public boolean addEntry(IRReteEntry newEntry, IRInterpreter interpreter, IRFrame frame) throws RException {

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
			newEntry.addEntryRemovedListener(this);
			return true;

		} else {

			++entryRedundant;
			return false;

		}
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.UNIQ;
	}

	@Override
	public void doAction(IRReteEntry entry) throws RException {
		uniqEntryMap.remove(ReteUtil.uniqName(entry));
	}
}
