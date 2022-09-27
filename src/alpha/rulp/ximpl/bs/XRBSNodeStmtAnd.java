package alpha.rulp.ximpl.bs;

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

	protected IAction action;

	protected IRBSNode failChild = null;

	protected boolean rst;

	protected SourceNode sourceNode;

	protected IRList stmt;

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
				treeList.add(child.buildResultTree(explain));
			}
		}

		return RulpFactory.createExpression(treeList);
	}

	public void complete() throws RException {

		// need trigger all related rete-node
		if (execute(listAllChildAndStmts())) {
			this.rst = true;
			return;
		}

		AbsBSNode lastChild = this.childNodes.get(childNodes.size() - 1);
		if (lastChild.getType() == BSNodeType.ENTRY_QUERY) {

			boolean hasMore = ((XRBSNodeBetaQuery) lastChild).hasMore();

			if (engine.isTrace()) {
				engine.trace_outln(this, String.format("complete-query, hasMore=%s", "" + hasMore));
			}

			if (!hasMore) {
				this.rst = false;
				return;
			}
		}

		this.sourceNode.rule.start(-1, -1);
		this.rst = engine.hasStmt(this, this.stmt);
	}

	public boolean execute(List<IRList> childStmts) throws RException {

		int rc = RuleUtil.executeRule(this.sourceNode.rule, childStmts);
		if (engine.isTrace()) {
			engine.trace_outln(this, String.format("execute rule: %s, rc=%d", this.sourceNode.rule.getRuleName(), rc));
		}

		return engine.hasStmt(this, this.stmt);
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

					BSFactory.addChild(engine, this, BSFactory.createNodeBetaQuery(engine, queryStmtList));
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
