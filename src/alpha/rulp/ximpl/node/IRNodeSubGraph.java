package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRNodeSubGraph {

	public void activate() throws RException;

	public void addNode(IRReteNode node) throws RException;

	public boolean containNode(IRReteNode node);

	public int getCacheCount();

	public int getNewPriority(IRReteNode node);

	public List<IRReteNode> getNodes();

	public void incCacheCount();

	public boolean isEmpty();

	public void rollback() throws RException;

	public void setGraphPriority(int newPriority) throws RException;

	public void setNodePriority(IRReteNode node, int newPriority) throws RException;
}
