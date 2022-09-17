package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraintX extends IRConstraint {

	public int getEntryLength();

	public boolean addEntry(IRReteEntry[] entries, IRContext context) throws RException;

	public int[] getConstraintIndex();

	public void setNode(IRReteNode node);
}
