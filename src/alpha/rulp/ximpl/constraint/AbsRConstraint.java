package alpha.rulp.ximpl.constraint;

import alpha.rulp.ximpl.rclass.AbsRInstance;

public abstract class AbsRConstraint extends AbsRInstance implements IRConstraint {

	public String toString() {
		return getConstraintExpression();
	}
}
