package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRInstance;

public interface IRConstraint extends IRInstance {

	public void close();

	public String getConstraintExpression();

	public RConstraintType getConstraintType();
}