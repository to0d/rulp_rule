package alpha.rulp.ximpl.model;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRSubNodeGraph {

	public List<IRReteNode> getAllNodes();

	public void rollback() throws RException;

}
