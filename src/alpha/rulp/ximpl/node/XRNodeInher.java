package alpha.rulp.ximpl.node;

import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeInher extends XRNodeRete1 {

	protected int[] inheritIndexs;

	protected IREntryTable entryTable;

	public void setEntryTable(IREntryTable entryTable) {
		this.entryTable = entryTable;
	}

	public XRNodeInher(String instanceName) {
		super(instanceName);
	}

	public void setInheritIndexs(int[] inheritIndexs) {
		this.inheritIndexs = inheritIndexs;
	}

	protected Map<String, IRReteEntry> uniqEntryMap = new HashMap<>();

	@Override
	public boolean addReteEntry(IRReteEntry entry) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			newElements[i] = entry.get(inheritIndexs[i]);
		}

		String uniqName = "'(" + ReteUtil.uniqName(newElements) + ")";

		// Check old entry
		{
			IRReteEntry oldEntry = uniqEntryMap.get(uniqName);

			// old entry exist
			if (!ReteUtil.isRemovedEntry(oldEntry)) {
				entryTable.addReference(oldEntry, this, entry);
				entryQueue.incNodeUpdateCount();
				entryQueue.incEntryRedundant();
				return false;
			}
		}

		IRReteEntry newEntry = entryTable.createEntry(null, newElements, entry.getStatus(), false);
		incEntryCreateCount();

		if (!super.addReteEntry(newEntry)) {
			entryTable.deleteEntry(newEntry);
			incEntryDeleteCount();
			return false;
		}

		entryTable.addReference(newEntry, this, entry);
		uniqEntryMap.put(uniqName, newEntry);
		return true;
	}
}
