package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class AbsRConstraint1 extends AbsRConstraint implements IRConstraint1 {

	protected abstract boolean _addEntry(IRReteEntry entry, IRContext context) throws RException;

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {

		this.matchCount++;

		boolean rc = _addEntry(entry, context);
		if (!rc) {
			this.failCount++;
		}

		return rc;
	}

	@Override
	public void close() {

	}

	@Override
	public void decRef() throws RException {

	}

	public String getCacheInfo() {
		return "";
	}

	@Override
	public RConstraintKind getConstraintKind() {
		return RConstraintKind.C1;
	}

	@Override
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	public void setNode(IRReteNode node) {

	}
}
