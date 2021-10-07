package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraint1 extends IRConstraint {

	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException;

	public int[] getConstraintIndex();

}
