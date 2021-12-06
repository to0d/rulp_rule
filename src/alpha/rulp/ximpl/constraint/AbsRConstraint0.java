package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class AbsRConstraint0 extends AbsRConstraint implements IRConstraint0 {

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
	public String asString() {
		return getConstraintExpression();
	}

	@Override
	public void close() {

	}

	@Override
	public void decRef() throws RException {

	}

	@Override
	public RConstraintKind getConstraintKind() {
		return RConstraintKind.C0;
	}

	@Override
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}
}
