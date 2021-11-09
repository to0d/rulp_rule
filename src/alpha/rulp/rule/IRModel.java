package alpha.rulp.rule;

import java.util.List;

import alpha.rulp.lang.IRInstance;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.cache.IRCacheWorker;
import alpha.rulp.ximpl.cache.IRStmtLoader;
import alpha.rulp.ximpl.cache.IRStmtSaver;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRResultQueue;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.IRNodeGraph;

public interface IRModel extends IRInstance, IRRunnable, IRContext {

	public static class RNodeContext {

		public int actualAddStmt = 0;

		public IRReteEntry currentEntry;

		public IRReteNode currentNode;

		public int tryAddStmt = 0;

	}

	public void addLoadNodeListener(IRRListener2<IRReteNode, IRObject> listener);

	public IRRule addRule(String ruleName, IRList condList, IRList actionList) throws RException;

	public void addRuleExecutedListener(IRRListener1<IRRule> listener);

	public void addRuleFailedListener(IRRListener1<IRRule> listener);

	public void addSaveNodeListener(IRRListener2<IRReteNode, IRObject> listener);

	public int addStatement(IRList stmt) throws RException;

	public void addStatementListener(IRList condList, IRRListener1<IRList> listener) throws RException;

	public int addStatements(IRIterator<? extends IRList> stmtIterator) throws RException;

	public void addUpdateNode(IRReteNode node) throws RException;

	public int assumeStatement(IRList stmt) throws RException;

	public int assumeStatements(IRIterator<? extends IRList> stmtIterator) throws RException;

	public void beginTransaction() throws RException;

	public IRIterator<? extends IRList> buildStatementIterator(IRList filter) throws RException;

	public int doGC() throws RException;

	public int execute(IRReteNode node) throws RException;

	public IRReteNode findNode(IRList condList) throws RException;

	public int fixStatement(IRList stmt) throws RException;

	public int fixStatements(IRIterator<? extends IRList> stmtIterator) throws RException;

//	public IRObject backSearch(IRList condList, IRObject rstExpr) throws RException;

	public String getCachePath();

	public IRModelCounter getCounter();

	public IREntryTable getEntryTable();

	public String getModelName();

	public IRNodeGraph getNodeGraph();

	public void getTransaction();

	public IRVar getVar(String name) throws RException;

	public boolean hasStatement(IRList filter) throws RException;

	public boolean isCacheEnable();

	public List<? extends IRCacheWorker> listCacheWorkers();

	public List<? extends IRList> listStatements(IRList filter, int statusMask, int listCount) throws RException;

	public void query(IRResultQueue resultQueue, IRList condList, int limit) throws RException;

	public IRRule removeRule(String ruleName) throws RException;

	public List<? extends IRList> removeStatement(IRList stmt) throws RException;

	public int save() throws RException;

	public void setModelCachePath(String cachePath) throws RException;

	public void setNodeCache(IRReteNode node, IRStmtLoader loader, IRStmtSaver saver, IRObject cacheKey)
			throws RException;

	public void setNodeContext(RNodeContext nodeContext);

	public boolean tryAddStatement(IRList stmt) throws RException;

}
