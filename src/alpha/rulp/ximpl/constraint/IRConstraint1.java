package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraint1 extends IRInstance {

	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException;

	public void close();

	public String getConstraintExpression();

	public int[] getConstraintIndex();

	public RConstraintType getConstraintType();

//	public boolean tryMatchConstraint(List<IRConstraint1> constraints, List<IRConstraint1> incompatibleConstraints);
}
