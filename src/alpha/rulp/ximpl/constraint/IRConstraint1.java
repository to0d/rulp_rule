package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraint1 extends IRConstraint {

	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException;

	public int[] getConstraintIndex();

}
