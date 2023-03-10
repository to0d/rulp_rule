package alpha.rulp.rule;

import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.ximpl.cache.IRBufferWorker;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.constraint.IRConstraint1;
import alpha.rulp.ximpl.entry.IREntryIteratorBuilder;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.IRNodeUpdateQueue;

public interface IRModel extends IRInstance, IRRunnable, IRContext {

	public static class RNodeContext {

		public int actualAddStmt = 0;

		public IRReteEntry currentEntry;

		public IRReteNode currentNode;

		public int tryAddStmt = 0;

	}

	public boolean addConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public void addLoadNodeListener(IRListener1<IRReteNode> listener);

	public IRRule addRule(String ruleName, IRList condList, IRList actionList) throws RException;

	public void addRuleExecutedListener(IRListener1<IRRule> listener);

	public void addRuleFailedListener(IRListener1<IRRule> listener);

	public void addSaveNodeListener(IRListener1<IRReteNode> listener);

	public boolean addStatement(IRList stmt) throws RException;

	public void addStatementListener(IRList condList, IRListener1<IRList> listener) throws RException;

	public void addUpdateNode(IRReteNode node) throws RException;

	public boolean assumeStatement(IRList stmt) throws RException;

	public IRIterator<? extends IRList> buildStatementIterator(IRList filter) throws RException;

	public int doGC() throws RException;

	public int execute(IRReteNode node, int limit) throws RException;

	public IRReteNode findNode(IRList condList) throws RException;

	public IRReteEntry findReteEntry(IRList filter) throws RException;

	public IRReteEntry findReteEntry(IRList filter, List<OrderEntry> orderList) throws RException;

	public boolean fixStatement(IRList stmt) throws RException;

	public String getCachePath();

	public IRModelCounter getCounter();

	public List<String> getCounterKeyList();

	public long getCounterValue(String countkey);

	public IREntryTable getEntryTable();

	public String getModelName();

	public IRNodeGraph getNodeGraph();

	public IRNodeUpdateQueue getNodeUpdateQueue();

	public IRReteNode getTopExecuteNode();

	public IRVar getVar(String name) throws RException;

	public boolean isBufferEnable();

	public List<? extends IRBufferWorker> listCacheWorkers();

	public int listStatements(IRList filter, int statusMask, int limit, boolean reverse, IREntryIteratorBuilder builder,
			IREntryAction action) throws RException;

	public void query(IREntryAction result, IRList condList, Map<String, IRObject> whereVarMap, int limit,
			boolean backward, boolean gc) throws RException;

	public IRIterator<IRReteEntry> query(IRList condList, int limit, boolean backward) throws RException;

	public IRObject removeConstraint(IRReteNode node, IRConstraint1 constraint) throws RException;

	public IRRule removeRule(String ruleName) throws RException;

	public boolean removeStatement(IRList stmt) throws RException;

	public int save() throws RException;

	public void setModelBufferPath(String bufferPath) throws RException;

	public void pushNodeContext(RNodeContext nodeContext);

	public void popNodeContext(RNodeContext nodeContext) throws RException;

	public void setNodeLoader(IRReteNode node, IRStmtLoader loader) throws RException;

	public void setNodeSaver(IRReteNode node, IRStmtSaver saver) throws RException;

	public boolean tryAddStatement(IRList stmt) throws RException;
}
