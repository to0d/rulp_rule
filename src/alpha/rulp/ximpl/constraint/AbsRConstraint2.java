package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;

public abstract class AbsRConstraint2 extends AbsRConstraint implements IRConstraint2 {

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
