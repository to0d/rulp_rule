package alpha.rulp.ximpl.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

	public void addNewNodeListener(IRNewNodeListener listener) throws RException;
	
	public void removeNewNodeListener(IRNewNodeListener listener) throws RException;

	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public void bindNode(IRReteNode fromNode, IRReteNode toNode) throws RException;

	public IRReteNode createNodeByTree(IRList tree) throws RException;

	public IRReteNode createNodeIndex(IRReteNode node, List<OrderEntry> orderList) throws RException;

	public IRReteNode createNodeRoot(String name, int stmtLen) throws RException;

	public IRReteNode createNodeRule(String ruleName, IRList condList, IRList actionList, int priority)
			throws RException;

	public IRNodeSubGraph createSubGraphForConstraintCheck(IRReteNode rootNode) throws RException;

	public IRNodeSubGraph createSubGraphForQueryNode(IRReteNode queryNode, boolean backward) throws RException;

	public IRNodeSubGraph createSubGraphForRuleGroup(String ruleGroupName) throws RException;

	public IRReteNode createWorkNode(String name, IRWorker worker) throws RException;

	public void doGc() throws RException;

	public int doOptimize() throws RException;

	public IRReteNode findNodeByUniqName(String uniqName) throws RException;

	public IRReteNode findRootNode(String name, int stmtLen) throws RException;

	public IRRule findRule(String ruleName);

	public Map<String, Map<String, IRNodeSubGraph>> getCachedSubGraphMap();

	public List<String> getCounterKeyList();

	public long getCounterValue(String countkey);

	public long getGcNodeRemoveCount(RCountType countType) throws RException;

	public int getGcRemoveNodeCount();

	public int getMaxRootStmtLen();

	public IReteNodeMatrix getNodeMatrix();

	public int getUniqueObjectCount();

	public int getUseCount(IRReteNode node);

	public List<IRReteNode> listBindFromNodes(IRReteNode node) throws RException;

	public List<IRReteNode> listBindToNodes(IRReteNode node) throws RException;

	public List<? extends IRReteNode> listNodes(RReteType reteType);

	public List<IRRule> listRelatedRules(IRReteNode node) throws RException;

	public Collection<SourceNode> listSourceNodes(IRList cond) throws RException;

	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public void removeNode(IRReteNode node) throws RException;

	public void setNodePriority(IRReteNode node, int priority) throws RException;

	public void setRulePriority(IRRule rule, int priority) throws RException;

}
