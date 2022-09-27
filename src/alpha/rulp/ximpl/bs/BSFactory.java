package alpha.rulp.ximpl.bs;

import static alpha.rulp.lang.Constant.F_B_AND;
import static alpha.rulp.lang.Constant.F_B_OR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.node.SourceNode;

public class BSFactory {

	protected static int bscCacheResult = 0;

	protected static int bscCircularProof = 0;

	protected static int bscNodeLogicAnd = 0;

	protected static int bscNodeLogicOr = 0;

	protected static int bscNodeStmtAnd = 0;

	protected static int bscNodeStmtOr = 0;

	protected static int bscNodeStmtQuery = 0;

	protected static int bscOpLoop = 0;

	protected static int bscOpRelocate = 0;

	protected static int bscOpSearch = 0;

	static List<String> bsCountKeyList = new ArrayList<>();

	protected static int bscStatusComplete = 0;

	protected static int bscStatusDuplicate = 0;

	protected static int bscStatusInit = 0;

	protected static int bscStatusProcess = 0;

	public static final String CK_BSC_CACHE_RESULT = "bsc-cache-result";

	public static final String CK_BSC_CIRCULAR_PROOF = "bsc-circular-proof";

	public static final String CK_BSC_NODE_LOGIC_AND = "bsc-node-logic-and";

	public static final String CK_BSC_NODE_LOGIC_OR = "bsc-node-logic-or";

	public static final String CK_BSC_NODE_STMT_AND = "bsc-node-stmt-and";

	public static final String CK_BSC_NODE_STMT_OR = "bsc-node-stmt-or";

	public static final String CK_BSC_NODE_STMT_QUERY = "bsc-node-stmt-query";

	public static final String CK_BSC_OP_LOOP = "bsc-op-loop";

	public static final String CK_BSC_OP_RELOCATE = "bsc-op-relocate";

	public static final String CK_BSC_OP_SEARCH = "bsc-op-search";

	public static final String CK_BSC_STATUS_COMPLETE = "bsc-status-complete";

	public static final String CK_BSC_STATUS_DUPLICATE = "bsc-status-Duplicate";

	public static final String CK_BSC_STATUS_INIT = "bsc-status-init";

	public static final String CK_BSC_STATUS_PROCESS = "bsc-status-process";

	static {

		bsCountKeyList.add(CK_BSC_NODE_STMT_AND);
		bsCountKeyList.add(CK_BSC_NODE_STMT_OR);
		bsCountKeyList.add(CK_BSC_NODE_STMT_QUERY);
		bsCountKeyList.add(CK_BSC_NODE_LOGIC_AND);
		bsCountKeyList.add(CK_BSC_NODE_LOGIC_OR);
		bsCountKeyList.add(CK_BSC_STATUS_INIT);
		bsCountKeyList.add(CK_BSC_STATUS_PROCESS);
		bsCountKeyList.add(CK_BSC_STATUS_COMPLETE);
		bsCountKeyList.add(CK_BSC_STATUS_DUPLICATE);
		bsCountKeyList.add(CK_BSC_OP_LOOP);
		bsCountKeyList.add(CK_BSC_OP_RELOCATE);
		bsCountKeyList.add(CK_BSC_OP_SEARCH);
		bsCountKeyList.add(CK_BSC_CIRCULAR_PROOF);
		bsCountKeyList.add(CK_BSC_CACHE_RESULT);

		bsCountKeyList = Collections.unmodifiableList(bsCountKeyList);
	}

	public static void addChild(IRBSEngine engine, AbsBSNode parent, AbsBSNode child) throws RException {

		if (engine.isTrace()) {
			engine.trace_outln(parent, String.format("add child, type=%s, name=%s", child.getType(), child.nodeName));
		}

		parent.addChild(child);
	}

	public static AbsBSEngine createEngine(IRModel model, BSSearchType st) throws RException {

		if (st != null && st != BSSearchType.DFS) {
			throw new RException("unsupport search type: " + st);
		}

		return new XRBSEngineDFS(model);
	}

	public static AbsBSNode createNode(IRBSEngine engine, IRList tree) throws RException {

		switch (tree.getType()) {
		case LIST:
//			if (ReteUtil.isReteStmtNoVar(tree)) {
				return createNodeStmtOr(engine, tree);
//			}

		case EXPR:

			IRExpr expr = (IRExpr) tree;

			ArrayList<IRList> stmtList = new ArrayList<>();
			IRIterator<? extends IRObject> it = expr.listIterator(1);
			while (it.hasNext()) {
				stmtList.add((IRList) it.next());
			}

			switch (expr.get(0).asString()) {
			case F_B_AND:
				return createNodeLogicAnd(engine, stmtList);

			case F_B_OR:
				return createNodeLogicOr(engine, stmtList);

			default:
			}

			break;

		default:
		}

		throw new RException("invalid bs tree: " + tree);
	}

	public static AbsBSNode createNodeBetaQuery(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeBetaQuery node = new XRBSNodeBetaQuery();
		node.queryReteNodeTree = RulpFactory.createList(stmtList);
		engine.addNode(node);

		bscNodeStmtQuery++;

		return node;
	}

	public static AbsBSNode createNodeLogicAnd(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeLogicAnd node = new XRBSNodeLogicAnd();
		node.stmtList = stmtList;
		engine.addNode(node);

		bscNodeLogicAnd++;

		return node;
	}

	public static AbsBSNode createNodeLogicOr(IRBSEngine engine, List<IRList> stmtList) throws RException {

		XRBSNodeLogicOr node = new XRBSNodeLogicOr();
		node.stmtList = stmtList;
		engine.addNode(node);

		bscNodeLogicOr++;

		return node;
	}

	public static AbsBSNode createNodeStmtAnd(IRBSEngine engine, IRList stmt, SourceNode sourceNode, IAction action)
			throws RException {

		XRBSNodeStmtAnd node = new XRBSNodeStmtAnd();
		node.stmt = stmt;
		node.sourceNode = sourceNode;
		node.action = action;
		engine.addNode(node);

		bscNodeStmtAnd++;

		return node;
	}

	public static AbsBSNode createNodeStmtOr(IRBSEngine engine, IRList stmt) throws RException {

		XRBSNodeStmtOr node = new XRBSNodeStmtOr();
		node.stmt = stmt;
		engine.addNode(node);

		bscNodeStmtOr++;

		return node;
	}

	public static int getBscCacheResult() {
		return bscCacheResult;
	}

	public static int getBscCircularProof() {
		return bscCircularProof;
	}

	public static int getBscNodeLogicAnd() {
		return bscNodeLogicAnd;
	}

	public static int getBscNodeLogicOr() {
		return bscNodeLogicOr;
	}

	public static int getBscNodeStmtAnd() {
		return bscNodeStmtAnd;
	}

	public static int getBscNodeStmtOr() {
		return bscNodeStmtOr;
	}

	public static int getBscNodeStmtQuery() {
		return bscNodeStmtQuery;
	}

	public static int getBscOpLoop() {
		return bscOpLoop;
	}

	public static int getBscOpRelocate() {
		return bscOpRelocate;
	}

	public static int getBscOpSearch() {
		return bscOpSearch;
	}

	public static int getBscStatusComplete() {
		return bscStatusComplete;
	}

	public static int getBscStatusDuplicate() {
		return bscStatusDuplicate;
	}

	public static int getBscStatusInit() {
		return bscStatusInit;
	}

	public static int getBscStatusProcess() {
		return bscStatusProcess;
	}

	public static List<String> getCounterKeyList() {
		return bsCountKeyList;
	}

	public static long getCounterValue(String countkey) {

		switch (countkey) {

		case CK_BSC_NODE_STMT_AND:
			return bscNodeStmtAnd;

		case CK_BSC_NODE_STMT_OR:
			return bscNodeStmtOr;

		case CK_BSC_NODE_STMT_QUERY:
			return bscNodeStmtQuery;

		case CK_BSC_NODE_LOGIC_AND:
			return bscNodeLogicAnd;

		case CK_BSC_NODE_LOGIC_OR:
			return bscNodeLogicOr;

		case CK_BSC_STATUS_INIT:
			return bscStatusInit;

		case CK_BSC_STATUS_PROCESS:
			return bscStatusProcess;

		case CK_BSC_STATUS_COMPLETE:
			return bscStatusComplete;

		case CK_BSC_STATUS_DUPLICATE:
			return bscStatusDuplicate;

		case CK_BSC_OP_LOOP:
			return bscOpLoop;

		case CK_BSC_OP_RELOCATE:
			return bscOpRelocate;

		case CK_BSC_CIRCULAR_PROOF:
			return bscCircularProof;

		case CK_BSC_OP_SEARCH:
			return bscOpSearch;

		case CK_BSC_CACHE_RESULT:
			return bscCacheResult;

		default:
			return 0;

		}
	}

	public static void incBscCacheResult() {
		++bscCacheResult;
	}

	public static void incBscCircularProof() {
		++bscCircularProof;
	}

	public static void incBscOpLoop(int count) {
		bscOpLoop += count;
	}

	public static void incBscOpRelocate(int count) {
		bscOpRelocate += count;
	}

	public static void incBscOpSearch(int count) {
		bscOpSearch += count;
	}

	public static void incBscStatusComplete(int count) {
		bscStatusComplete += count;
	}

	public static void incBscStatusDuplicate(int count) {
		bscStatusDuplicate += count;
	}

	public static void incBscStatusInit(int count) {
		bscStatusInit += count;
	}

	public static void incBscStatusProcess(int count) {
		bscStatusProcess += count;
	}

	public static void reset() {

		bscCircularProof = 0;
		bscNodeLogicAnd = 0;
		bscNodeLogicOr = 0;
		bscNodeStmtAnd = 0;
		bscNodeStmtOr = 0;
		bscNodeStmtQuery = 0;
		bscOpLoop = 0;
		bscOpRelocate = 0;
		bscOpSearch = 0;
		bscStatusComplete = 0;
		bscStatusInit = 0;
		bscStatusProcess = 0;
		bscStatusDuplicate = 0;
		bscCacheResult = 0;
	}
}
