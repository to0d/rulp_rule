package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public abstract class AbsRConstraint2 extends AbsRConstraint implements IRConstraint2 {

	protected abstract boolean _addEntry(IRReteEntry left, IRReteEntry right, IRInterpreter interpreter, IRFrame frame)
			throws RException;

	public boolean addEntry(IRReteEntry left, IRReteEntry right, IRInterpreter interpreter, IRFrame frame)
			throws RException {

		this.matchCount++;

		boolean rc = _addEntry(left, right, interpreter, frame);
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
		return RConstraintKind.C2;
	}

	@Override
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}
}
