package alpha.rulp.ximpl.bs;

import static alpha.rulp.rule.Constant.O_QUERY_STMT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.utils.RuntimeUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.IActionSimpleStmt;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBSNodeStmtAnd extends AbsBSNode implements IRBSNodeStmt {

	static interface IRBSQueryNodeIterator {

		public boolean hasMore() throws RException;

		public List<IRList> next() throws RException;
	}

	static class XRBSQueryNodeIterator implements IRBSQueryNodeIterator {

		private boolean completed = false;

		private int resultIndex = 0;

		private List<List<IRList>> resultList = new ArrayList<>();

		private final boolean[] subCompleted;

		private final int subNodeCount;

		private List<List<List<IRList>>> subNodeResultList = null;

		private List<IRBSNodeQuery> subNodes;

		private int subScanIndex = 0;

		private final int[] subVisitIndexs;

		public String getVisitIndexs() {
			return "" + RulpUtil.toArray2(subVisitIndexs);
		}

		public XRBSQueryNodeIterator(List<IRBSNodeQuery> subNodes) {

			super();

			this.subNodes = subNodes;
			this.subNodeCount = subNodes.size();
			this.subCompleted = new boolean[this.subNodeCount];
			this.subVisitIndexs = new int[this.subNodeCount];

			for (int i = 0; i < this.subNodeCount; ++i) {
				this.subCompleted[i] = false;
			}

		}

		public List<List<IRList>> getSubResultList() {

			List<List<IRList>> resultList = new ArrayList<>();

			for (int i = 0; i < this.subNodeCount; ++i) {
				resultList.add(subNodeResultList.get(i).get(subVisitIndexs[i]));
			}

			return resultList;
		}

		public List<IRList> buildResultList() throws RException {

			List<IRList> result = new ArrayList<>();

			for (int i = 0; i < this.subNodeCount; ++i) {
				result.addAll(subNodeResultList.get(i).get(subVisitIndexs[i]));
			}

			return result;
		}

		@Override
		public boolean hasMore() throws RException {

			if (resultIndex < resultList.size()) {
				return true;
			}

			if (completed) {
				return false;
			}

			if (subNodeResultList == null) {

				subNodeResultList = new ArrayList<>();

				for (int i = 0; i < this.subNodeCount; ++i) {

					IRBSNodeQuery subNode = subNodes.get(i);
					if (!subNode.hasMore()) {
						this.completed = true;
						return false;
					}

					List<List<IRList>> subResult = new ArrayList<>();
					subResult.add(subNode.next());
					subNodeResultList.add(subResult);
					subVisitIndexs[i] = 0;
				}

				subScanIndex = 0;
				resultList.add(buildResultList());
				return true;
			}

			boolean carry = false;
			boolean found = false;

			while (!found && subScanIndex < subNodeCount) {

				int curIndex = subVisitIndexs[subScanIndex];
				List<List<IRList>> subResultList = subNodeResultList.get(subScanIndex);
				if ((curIndex + 1) < subResultList.size()) {
					found = true;
					break;
				}

				if (!subCompleted[subScanIndex]) {

					IRBSNodeQuery subNode = subNodes.get(subScanIndex);
					if (!subNode.hasMore()) {
						subCompleted[subScanIndex] = true;
					} else {
						found = true;
						break;
					}
				}

				carry = true;
				subVisitIndexs[subScanIndex++] = 0;
			}

			if (found) {

				subVisitIndexs[subScanIndex]++;
				resultList.add(buildResultList());

				if (carry) {
					subScanIndex = 0;
				}
			}

			return found;
		}

		public List<IRList> next() throws RException {

			if (hasMore()) {
				return resultList.get(resultIndex++);
			}

			return null;

		}
	}

	protected IAction action;

	protected IRBSNode failChild = null;

	protected ArrayList<IRBSNodeQuery> queryNodes;

	protected boolean rst;

	protected SourceNode sourceNode;

	protected IRList stmt;

	protected List<IRList> queryStmts = null;

	public void addChild(AbsBSNode child) {

		super.addChild(child);

		if (_isQueryNode(child)) {

			if (queryNodes == null) {
				queryNodes = new ArrayList<>();
			}

			queryNodes.add((IRBSNodeQuery) child);
		}
	}

	static boolean _isQueryNode(AbsBSNode node) {
		return node.getType() == BSNodeType.ENTRY_QUERY || node.getType() == BSNodeType.STMT_QUERY;
	}

	public IRList buildResultTree(boolean explain) throws RException {

		if (!this.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		if (!explain) {
			return RulpFactory.createList(stmt);
		}

		ArrayList<IRObject> treeList = new ArrayList<>();
		treeList.add(RulpFactory.createString(sourceNode.rule.getRuleName()));

		if (childNodes != null) {

			for (AbsBSNode child : childNodes) {
				if (!_isQueryNode(child)) {
					treeList.add(child.buildResultTree(explain));
				}
			}

			int queryNodeCount = getQueryNodeCount();
			if (queryNodeCount > 0) {
				List<List<IRList>> resultlist = subNodeIterator.getSubResultList();
				for (int i = 0; i < queryNodeCount; ++i) {
					ArrayList<IRObject> rtList = new ArrayList<>();
					rtList.add(O_QUERY_STMT);
					rtList.addAll(resultlist.get(i));
					treeList.add(RulpFactory.createExpression(rtList));
				}
			}
		}

		return RulpFactory.createExpression(treeList);
	}

	XRBSQueryNodeIterator subNodeIterator;

	public void complete() throws RException {

		List<IRList> andStmts = listAllChildAndStmts();

		if (getQueryNodeCount() == 0) {
			// need trigger all related rete-node
			this.rst = execute(andStmts);
			return;
		}

		subNodeIterator = new XRBSQueryNodeIterator(queryNodes);
		while (subNodeIterator.hasMore()) {

			List<IRList> queryStmts = subNodeIterator.next();
			if (engine.isTrace()) {
				engine.trace_outln(this, String.format("query: iterator=%s, result=%s",
						subNodeIterator.getVisitIndexs(), "" + queryStmts));
			}

			List<IRList> execStmts = queryStmts;
			if (andStmts != null) {
				execStmts = new ArrayList<>(andStmts);
				execStmts.addAll(queryStmts);
			}

			if (execute(execStmts)) {
				this.rst = true;
				this.queryStmts = queryStmts;
				return;
			}
		}

		this.rst = false;
		return;
	}

	public boolean execute(List<IRList> childStmts) throws RException {

		int rc = RuleUtil.executeRule(this.sourceNode.rule, childStmts);
		if (engine.isTrace()) {
			engine.trace_outln(this, String.format("execute rule: %s, stmt=%s, rc=%d",
					this.sourceNode.rule.getRuleName(), "" + childStmts, rc));
		}

		return engine.hasStmt(this, this.stmt);
	}

	public int getQueryNodeCount() {
		return queryNodes == null ? 0 : queryNodes.size();
	}

	public SourceNode getSourceNode() {
		return sourceNode;
	}

	public String getStatusString() {
		return String.format("fail-child=%s", failChild == null ? "null" : failChild.getNodeName());
	}

	public IRList getStmt() {
		return stmt;
	}

	@Override
	public BSNodeType getType() {
		return BSNodeType.STMT_AND;
	}

	public void init() throws RException {

		IActionSimpleStmt addAction = (IActionSimpleStmt) action;
		int[] inheritIndexs = addAction.getInheritIndexs();
		int inheritSize = inheritIndexs.length;
		if (inheritSize != stmt.size()) {
			throw new RException("invalid action: " + addAction);
		}

		Map<String, IRObject> varValueMap = new HashMap<>();

		for (int i = 0; i < inheritSize; ++i) {
			int inheritIndex = inheritIndexs[i];
			if (inheritIndex != -1) {

				IRObject var = sourceNode.rule.getVarEntry()[inheritIndex];
				if (var == null) {
					throw new RException("invalid inheritIndex: " + inheritIndex);
				}

				varValueMap.put(RulpUtil.toString(var), stmt.get(i));
			}
		}

		ArrayList<IRList> queryStmtList = null;

		for (IRList list : sourceNode.rule.getMatchStmtList()) {

			if (ReteUtil.isAlphaMatchTree(list)) {

				IRList newStmt = (IRList) RuntimeUtil.rebuild(list, varValueMap);

				if (ReteUtil.isReteStmtNoVar(newStmt)) {
					// The and should fail once circular proof be found
					BSFactory.addChild(engine, this, BSFactory.createNodeStmtOr(engine, newStmt));
				}
				// '(?a p b) should be used in query node
				else {

					if (queryStmtList == null) {
						queryStmtList = new ArrayList<>();
					}

					queryStmtList.add(newStmt);
				}
			}
		}

		if (queryStmtList != null) {

			if (queryStmtList.size() > 1) {

//				IRList queryTree = MatchTree.build(queryStmtList, engine.getModel().getInterpreter(),
//						engine.getModel().getFrame());
//
//				if (ReteUtil.isBetaTree(queryTree, queryTree.size())) {

				BSFactory.addChild(engine, this, BSFactory.createNodeEntryQuery(engine, queryStmtList));
				queryStmtList = null;
//				}
			}

			if (queryStmtList != null) {
				throw new RException("invalid queryStmtList: " + queryStmtList);
			}

		}

		// no child
		if (this.getChildCount() == 0) {

			if (engine.isTrace()) {
				engine.trace_outln(this, "not child, return false");
			}

			this.status = BSStats.COMPLETE;
			this.rst = false;
			return;
		}

		this.status = BSStats.PROCESS;

	}

	@Override
	public boolean isSucc() {
		return this.rst;
	}

	public List<IRList> listAllChildAndStmts() {

		List<IRList> stmts = null;

		for (AbsBSNode childNode : childNodes) {

			if (childNode.getType() == BSNodeType.STMT_OR) {

				XRBSNodeStmtOr orNode = (XRBSNodeStmtOr) childNode;
				if (stmts == null) {
					stmts = new ArrayList<>();
				}

				stmts.add(orNode.getStmt());
			}
		}

		if (stmts == null) {
			stmts = Collections.emptyList();
		}

		return stmts;
	}

	@Override
	public boolean needComplete() {
		return true;
	}

	public void process(IRBSNode lastNode) throws RException {

		// (and false xx xx) ==> false
		if (!lastNode.isSucc()) {
			this.failChild = lastNode;
			this.status = BSStats.COMPLETE;
			this.rst = false;
			return;
		}
	}

	@Override
	public void setSucc(boolean succ) {
		this.rst = succ;
	}

	@Override
	public String toString() {
		return String.format("stmt=%s, rule=%s, action=%s(%d), type=%s, status=%s", stmt, sourceNode.rule,
				action.toString(), action.getIndex(), "" + this.getType(), "" + this.status);
	}

}
