package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IRRootNode extends IRReteNode {

	public IRReteEntry getStmt(String uniqName) throws RException;

}
