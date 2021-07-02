package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IMatchNode2 {

	public boolean match(IRReteEntry leftEntry, IRReteEntry rightEntry) throws RException;
}
