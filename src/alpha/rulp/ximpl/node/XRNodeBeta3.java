package alpha.rulp.ximpl.node;

import static alpha.rulp.rule.RReteStatus.REMOVE;

import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.constraint.IRConstraint2;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeBeta3 extends XRNodeRete2 implements IRBetaNode {

	protected boolean _addNewEntry(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		/*******************************************************/
		// New entry
		/*******************************************************/
		IRReteEntry newEntry = _getNewEntry(leftEntry, rightEntry);

		if (addReteEntry(newEntry)) {
			entryTable.addReference(newEntry, this, leftEntry, rightEntry);
			return true;
		}

		return false;
	}

	protected boolean _match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException {

		if (leftEntry == null || leftEntry.getStatus() == REMOVE) {
			return false;
		}

		if (rightEntry == null || rightEntry.getStatus() == REMOVE) {
			return false;
		}

		if (this.constraint2List != null) {

			IRFrame consFrame = RNodeFactory.createNodeFrame(this);
			RulpUtil.incRef(consFrame);

			try {
				for (IRConstraint2 constraint : constraint2List) {
					if (!constraint.addEntry(leftEntry, rightEntry, this.getModel().getInterpreter(), consFrame)) {
						return false;
					}
				}
			} finally {
				consFrame.release();
				RulpUtil.decRef(consFrame);
			}
		}

		return true;
	}

	@Override
	public List<JoinIndex> getJoinIndexList() {
		return null;
	}

}
