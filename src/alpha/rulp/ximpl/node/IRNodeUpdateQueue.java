package alpha.rulp.ximpl.node;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRNodeUpdateQueue {

	public int getMaxNodeCount();

	public boolean hasNext();

	public IRReteNode pop() throws RException;

	public void push(IRReteNode node, boolean force) throws RException;

	public void updateNodePriority(IRReteNode node, int oldPriority, int newPriority) throws RException;
}
