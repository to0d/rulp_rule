package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeNamed extends XRNodeRete0 implements IRNamedNode {

	protected IRObject[] funcEntry;

	protected IRConstraint1Uniq funcUniqConstraint;

	public XRNodeNamed(String instanceName) {
		super(instanceName);
	}

	@Override
	public void cleanCache() throws RException {

		if (cache == null) {
			throw new RException("not cacher found");
		}

		this.cache.cleanBuffer();

		IREntryTable entryTable = this.getModel().getEntryTable();
		for (IRReteEntry entry : ReteUtil.getAllEntries(this.getEntryQueue())) {
			entryTable.deleteEntryReference(entry, this);
		}

		this.entryQueue.cleanCache();
		this.lastEntryCount = 0;
		this.reteStage = RReteStage.InActive;
	}

	@Override
	public IRObject[] getFuncEntry() {
		return funcEntry;
	}

	@Override
	public IRConstraint1Uniq getFuncUniqConstraint() {
		return funcUniqConstraint;
	}

	public void setFuncEntry(IRObject[] funcEntry) throws RException {

		if (funcEntry.length != this.getEntryLength()) {
			throw new RException(String.format("unmatch entry length: funcEntryLen=%d, NodeEntryLen=%d",
					funcEntry.length, this.getEntryLength()));
		}

		this.funcEntry = funcEntry;
	}

	public void setFuncUniqConstraint(IRConstraint1Uniq funcUniqConstraint) throws RException {
		this.funcUniqConstraint = funcUniqConstraint;
	}
}
