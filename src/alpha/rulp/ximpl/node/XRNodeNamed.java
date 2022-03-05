package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.Constant.F_FIX_STMT;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.AttrUtil;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeNamed extends XRNodeRete0 implements IRNamedNode {

	public XRNodeNamed(String instanceName) {
		super(instanceName);
	}

	public void cleanCache() throws RException {

		if (cache == null) {
			throw new RException("not cacher found");
		}

		this.cache.cleanCache();

		if (!AttrUtil.containAttribute(this, F_FIX_STMT)) {
			IREntryTable entryTable = this.getModel().getEntryTable();
			for (IRReteEntry entry : ReteUtil.getAllEntries(this.getEntryQueue())) {
				entryTable.removeEntryReference(entry, this);
			}
		}

		this.entryQueue.cleanCache();
		this.lastEntryCount = 0;
		this.reteStage = RReteStage.InActive;
	}
}
