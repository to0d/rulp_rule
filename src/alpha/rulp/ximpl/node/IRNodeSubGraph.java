package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRNodeSubGraph {

	public void activate(int priority) throws RException;

	public List<IRReteNode> getNodes();

	public boolean isEmpty();

	public void rollback() throws RException;
}
