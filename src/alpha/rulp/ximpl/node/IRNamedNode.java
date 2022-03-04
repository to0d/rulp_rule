package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRNamedNode extends IRReteNode {

	public void cleanCache() throws RException;
}
