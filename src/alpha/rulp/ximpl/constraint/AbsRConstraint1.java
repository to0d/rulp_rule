package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.rclass.AbsRInstance;

public abstract class AbsRConstraint1 extends AbsRInstance implements IRConstraint1 {

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
