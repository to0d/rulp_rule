package alpha.rulp.ximpl.node;

import java.util.Collection;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.ximpl.model.IReteNodeMatrix;

public interface IRNodeGraph {

	public IRRule addRule(String ruleName, IRList condList, IRList actionList, int priority) throws RException;

	public IRReteNode addWorker(String name, IRWorker worker) throws RException;

	public void bindNode(IRReteNode fromNode, IRReteNode toNode) throws RException;

	public int doOptimize() throws RException;

	public IRNamedNode findNamedNode(String name) throws RException;

	public IRRootNode findRootNode(int stmtLen) throws RException;

	public List<IRReteNode> getBindFromNodes(IRReteNode node) throws RException;

	public List<IRReteNode> getBindToNodes(IRReteNode node) throws RException;

	public int getMaxRootStmtLen();

	public IRNamedNode getNamedNode(String name, int stmtLen) throws RException;

	public IRReteNode getNodeById(int nodeId);

	public IRReteNode getNodeByTree(IRList tree) throws RException;

	public IReteNodeMatrix getNodeMatrix();;

	public List<IRRule> getRelatedRules(IRReteNode node) throws RException;;

	public IRRootNode getRootNode(int stmtLen) throws RException;

	public IRRule getRule(String ruleName);

	public int getUniqueObjectCount();

	public List<? extends IRReteNode> listNodes(RReteType reteType);

	public Collection<? extends IRReteNode> listSourceNodes(IRReteNode node) throws RException;
}
