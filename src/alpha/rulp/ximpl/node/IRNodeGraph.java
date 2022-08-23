package alpha.rulp.ximpl.node;

import java.util.Collection;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.rule.IRWorker;
import alpha.rulp.rule.RCountType;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.model.IReteNodeMatrix;

public interface IRNodeGraph {

	public interface IRNodeSubGraph {

		public void activate(int priority) throws RException;

		public List<IRReteNode> getSubNodes();

		public boolean isEmpty();

		public void rollback() throws RException;
	}

	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public IRRule addRule(String ruleName, IRList condList, IRList actionList, int priority) throws RException;

	public IRReteNode addWorker(String name, IRWorker worker) throws RException;

	public void bindNode(IRReteNode fromNode, IRReteNode toNode) throws RException;

	public IRNodeSubGraph buildConstraintCheckSubGraph(IRReteNode rootNode) throws RException;

	public IRReteNode buildIndex(IRReteNode node, List<OrderEntry> orderList) throws RException;

	public IRNodeSubGraph buildRuleGroupSubGraph(String ruleGroupName) throws RException;

	public IRNodeSubGraph buildSourceSubGraph(IRReteNode queryNode) throws RException;

	public int doOptimize() throws RException;

	public IRReteNode findNodeByUniqName(String uniqName) throws RException;

	public IRReteNode findRootNode(String name, int stmtLen) throws RException;

	public void gc() throws RException;

	public List<IRReteNode> getBindFromNodes(IRReteNode node) throws RException;

	public List<IRReteNode> getBindToNodes(IRReteNode node) throws RException;

	public int getGcCacheCount();

	public int getGcCleanNodeCount();

	public int getGcCount();

	public int getGcInactiveLeafCount();

	public long getGcNodeRemoveCount(RCountType countType) throws RException;

	public int getGcRemoveNodeCount();

	public int getMaxRootStmtLen();

	public IRReteNode getNodeById(int nodeId);

	public IRReteNode getNodeByTree(IRList tree) throws RException;

	public IReteNodeMatrix getNodeMatrix();

	public List<IRRule> getRelatedRules(IRReteNode node) throws RException;

	public IRReteNode getRootNode(String name, int stmtLen) throws RException;

	public IRRule getRule(String ruleName);

	public int getUniqueObjectCount();

	public int getUseCount(IRReteNode node);

	public List<? extends IRReteNode> listNodes(RReteType reteType);

	public Collection<SourceNode> listSourceNodes(IRList cond) throws RException;

	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public boolean removeNode(IRReteNode node) throws RException;

	public void setGcMaxCacheNodeCount(int gcMaxCacheNodeCount);

	public void setGcMaxInactiveLeafCount(int gcMaxInactiveLeafCount);

	public void setNodePriority(IRReteNode node, int priority) throws RException;

	public void setRulePriority(IRRule rule, int priority) throws RException;

}
