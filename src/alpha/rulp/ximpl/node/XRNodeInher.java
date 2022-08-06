package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.entry.REntryQueueType;

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

	@Override
	public boolean addReteEntry(IRReteEntry entry) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			newElements[i] = entry.get(inheritIndexs[i]);
		}

		String uniqName = ReteUtil.uniqName(RulpUtil.toList(entry.getNamedName(), newElements));
		IRReteEntry oldEntry = null;
		if (entryQueue.getQueueType() == REntryQueueType.UNIQ) {
			oldEntry = entryQueue.getStmt(uniqName);
		}

		// old entry exist
		if (!ReteUtil.isRemovedEntry(oldEntry)) {
			entryTable.addReference(oldEntry, this, entry);
			return false;
		}

		IRReteEntry newEntry = entryTable.createEntry(null, newElements, entry.getStatus(), false);
		if (!super.addReteEntry(newEntry)) {
			entryTable.removeEntry(newEntry);
			return false;
		}

		entryTable.addReference(newEntry, this, entry);
		return true;
	}
}
