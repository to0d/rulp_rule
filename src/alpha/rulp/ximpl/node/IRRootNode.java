package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRRootNode extends IRReteNode {

	public boolean addStmt(IRList stmt, RReteStatus newStatus) throws RException;

	public IRReteEntry getStmt(String uniqName) throws RException;

}
