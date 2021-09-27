package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public abstract class AbsRConstraint2 extends AbsRInstance implements IRConstraint2 {

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
	public void incRef() throws RException {
	}

	@Override
	public boolean isDeleted() {
		return false;
	}
}
