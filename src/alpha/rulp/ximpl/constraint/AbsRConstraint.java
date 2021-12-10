package alpha.rulp.ximpl.constraint;

import alpha.rulp.ximpl.rclass.AbsRInstance;

public abstract class AbsRConstraint extends AbsRInstance implements IRConstraint {

	protected int failCount = 0;

	protected int matchCount = 0;

	@Override
	public int getFailCount() {
		return failCount;
	}

	@Override
	public int getMatchCount() {
		return matchCount;
	}

	public String toString() {
		return getConstraintExpression();
	}

	@Override
	public String asString() {
		return getConstraintExpression();
	}

}
