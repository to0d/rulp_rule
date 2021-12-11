package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRObject;

public interface IRConstraint1OneValue extends IRConstraint1 {

	public IRObject getValue();

	public int getColumnIndex();
}
