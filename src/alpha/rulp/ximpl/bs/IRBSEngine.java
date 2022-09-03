package alpha.rulp.ximpl.bs;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.ximpl.node.IRNodeGraph;

public interface IRBSEngine {

	public void addNode(AbsBSNode node) throws RException;

	public IRNodeGraph getGraph();

	public IRModel getModel();

	public boolean hasStmt(IRBSNode node, IRList stmt) throws RException;

	public boolean isTrace();

	public IRList search(IRList tree, boolean explain) throws RException;

	public void trace_outln(IRBSNode node, String line);

}
