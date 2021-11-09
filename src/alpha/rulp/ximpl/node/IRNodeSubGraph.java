package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRNodeSubGraph {

	public List<IRReteNode> getAllNodes();

	public void rollback() throws RException;

}
