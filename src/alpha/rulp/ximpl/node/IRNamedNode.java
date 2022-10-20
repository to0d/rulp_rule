package alpha.rulp.ximpl.node;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.constraint.IRConstraint1Uniq;

public interface IRNamedNode extends IRReteNode {

	public void cleanCache() throws RException;

	public IRList computeFuncEntry(IRList stmt) throws RException;

	public IRObject[] getFuncEntry();

	public IRConstraint1Uniq getFuncUniqConstraint();
}
