package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRConstraint1Uniq extends IRConstraint1 {

	public IRReteEntry getReteEntry(String uniqName) throws RException;

	public String getUniqString(IRList entry) throws RException;

}
