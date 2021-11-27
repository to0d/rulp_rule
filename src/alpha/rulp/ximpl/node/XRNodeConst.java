package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeConst extends XRNodeRete0 implements IRRListener1<IRReteEntry> {

	private IRReteEntry constEntry = null;

	protected int _process() throws RException {

		nodeExecCount++;

		// no need to process
		if (constEntry != null && !constEntry.isDroped()) {
			++nodeIdleCount;
			return 0;
		}

		IRRootNode parentNode = (IRRootNode) this.getParentNodes()[0];
		String constUniqName = this.getUniqName();
//		XREntryQueueRootStmtList rootQueue = (XREntryQueueRootStmtList) parentNode.getEntryQueue();

		constEntry = parentNode.getStmt(constUniqName);
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