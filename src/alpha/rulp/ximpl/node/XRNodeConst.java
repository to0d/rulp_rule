package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeConst extends XRNodeRete0 implements IRListener1<IRReteEntry> {

	private IRReteEntry constEntry = null;

	public XRNodeConst(String instanceName) {
		super(instanceName);
	}

	protected int _process() throws RException {

		nodeExecCount++;

		// no need to process
		if (constEntry != null && !constEntry.isDroped()) {
			++nodeIdleCount;
			return 0;
		}

		IRReteNode parentNode = this.getParentNodes()[0];
		String constUniqName = this.getUniqName();
//		XREntryQueueRootStmtList rootQueue = (XREntryQueueRootStmtList) parentNode.getEntryQueue();

		constEntry = parentNode.getEntryQueue().getStmt(constUniqName);
		if (constEntry == null) {
			return 0;
		}

		this.addReteEntry(constEntry);

		/*****************************************************/
		// Once the const be found, no need to update this node
		/*****************************************************/
		parentNode.setChildNodeUpdateMode(this, false);
		constEntry.addEntryRemovedListener(this);

		return 1;
	}

	@Override
	public void doAction(IRReteEntry entry) throws RException {

		// this entry was removed
		if (constEntry == entry) {

			IRReteNode parentNode = this.getParentNodes()[0];

			// activate this node again
			parentNode.setChildNodeUpdateMode(this, true);

			// force update once
			_process();
		}
	}

	@Override
	public int update() throws RException {
		return _process();
	}

}