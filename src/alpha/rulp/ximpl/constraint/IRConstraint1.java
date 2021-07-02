package alpha.rulp.ximpl.constraint;

import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraint1 extends IRInstance {

	public boolean addConstraint(List<IRConstraint1> constraints, List<IRConstraint1> incompatibleConstraints);

	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException;

	public void close();

	public String getConstraintExpression();

	public int[] getConstraintIndex();

	public RConstraintType getConstraintType();
}
