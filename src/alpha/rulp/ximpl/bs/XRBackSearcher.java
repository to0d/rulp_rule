package alpha.rulp.ximpl.bs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.model.XRModel;
import alpha.rulp.ximpl.node.IRNodeGraph;
import alpha.rulp.ximpl.node.SourceNode;

public class XRBackSearcher {

	protected int bscCircularProof = 0;

	protected int bscNodeAnd = 0;

	protected int bscNodeOr = 0;

	protected int bscNodeQuery = 0;

	protected int bscOpLoop = 0;

	protected int bscOpRelocate = 0;

	protected int bscOpSearch = 0;

	protected int bscStatusComplete = 0;

	protected int bscStatusInit = 0;

	protected int bscStatusProcess = 0;

	protected IRNodeGraph graph;

	protected IRInterpreter interpreter;

	protected XRModel model;

	protected int nodeId = 0;

	protected Map<String, IRBSNode> nodeMap = new HashMap<>();

	protected ArrayList<IRBSNode> queryStack = new ArrayList<>();

	protected IRBSNode rootNode;

	protected boolean trace = false;

	protected Map<String, AbsBSNode> visitingOrNodeMap = new HashMap<>();

	public XRBackSearcher(XRModel model) {
		super();
		this.model = model;
		this.graph = model.getNodeGraph();
		this.interpreter = model.getInterpreter();
	}

	protected IRNodeGraph _getGraph() {
		return graph;
	}

	protected XRModel _getModel() {
		return model;
	}

	protected boolean _hasStmt(IRBSNode node, IRList stmt) throws RException {

		boolean rc = model.hasStatement(stmt);
		if (trace) {
			_outln(node, "has stmt, stmt=" + stmt + ", rst=" + rc);
		}

		return rc;
	}

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return visitingOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected boolean _isTrace() {
		return trace;
	}

	protected AbsBSNode _newAndNode(IRList stmt, SourceNode sourceNode, IAction action) {

		int nodeId = _nextNodeId();
		String nodeName = BSUtil.getBSNodeName(BSType.STMT_AND, nodeId);

		XRBSNodeStmtAnd node = new XRBSNodeStmtAnd(this, nodeId, nodeName);
		node.stmt = stmt;
		node.status = BSStats.INIT;
		node.sourceNode = sourceNode;
		node.action = action;

		this.bscNodeAnd++;

		return node;
	}

	protected AbsBSNode _newOrNode(IRList stmt) throws RException {

		int nodeId = _nextNodeId();
		String nodeName = BSUtil.getBSNodeName(BSType.STMT_OR, nodeId);

		XRBSNodeStmtOr node = new XRBSNodeStmtOr(this, nodeId, nodeName);
		node.stmt = stmt;
		node.status = BSStats.INIT;

		this.bscNodeOr++;

		visitingOrNodeMap.put(ReteUtil.uniqName(stmt), node);

		return node;
	}

	protected AbsBSNode _newQueryNode(List<IRList> stmtList) throws RException {

		int nodeId = _nextNodeId();
		String nodeName = BSUtil.getBSNodeName(BSType.STMT_QUERY, nodeId);

		XRBSNodeStmtQuery node = new XRBSNodeStmtQuery(this, nodeId, nodeName);
		node.queryReteNodeTree = RulpFactory.createList(stmtList);
		node.status = BSStats.INIT;
		this.bscNodeQuery++;

		return node;
	}

	protected int _nextNodeId() {
		return nodeId++;
	}

	protected void _outln(IRBSNode node, String line) {
		_outln(String.format("%05d %s%s: %s", getBscOpLoop(), RulpUtil.getSpaceLine(node.getLevel()),
				node.getNodeName(), line));
	}

	protected void _outln(String line) {
//		System.out.println(line);
		interpreter.out(line + "\n");
	}

	public int getBscCircularProof() {
		return bscCircularProof;
	}

	public int getBscNodeAnd() {
		return bscNodeAnd;
	}

	public int getBscNodeOr() {
		return bscNodeOr;
	}

	public int getBscNodeQuery() {
		return bscNodeQuery;
	}

	public int getBscOpLoop() {
		return bscOpLoop;
	}

	public int getBscOpRelocate() {
		return bscOpRelocate;
	}

	public int getBscOpSearch() {
		return bscOpSearch;
	}

	public int getBscStatusComplete() {
		return bscStatusComplete;
	}

	public int getBscStatusInit() {
		return bscStatusInit;
	}

	public int getBscStatusProcess() {
		return bscStatusProcess;
	}

//	protected AbsBSNode _buildTree(IRList tree) throws RException {
//
//		switch (tree.getType()) {
//		case LIST:
//			return ReteUtil.isReteStmt(obj);
//
//		case EXPR:
//
//			IRExpr expr = (IRExpr) obj;
//			if (expr.size() < 2) {
//				return false;
//			}
//
//			switch (expr.get(0).asString()) {
//			case F_B_AND:
//			case F_B_OR:
//				break;
//			default:
//				return false;
//			}
//
//			IRIterator<? extends IRObject> it = expr.listIterator(1);
//			while (it.hasNext()) {
//				if (!isBSTree(it.next())) {
//					return false;
//				}
//			}
//
//			return true;
//
//		default:
//			return false;
//		}
//	}

	public IRList search(IRList tree, boolean explain) throws RException {

		if (!BSUtil.isBSTree(tree)) {
			throw new RException("invalid bs tree: " + tree);
		}

		trace = BSUtil.isBSTrace(model.getFrame());
		rootNode = _newOrNode(tree);

		if (trace) {
			_outln(rootNode, "create_root, " + rootNode.toString());
		}

		IRBSNode curNode = rootNode;
		IRBSNode lastNode = null;

		this.bscOpSearch++;

		while (rootNode.getStatus() != BSStats.COMPLETE) {

			IRBSNode oldNode = curNode;
			BSStats oldStatus = curNode.getStatus();

			switch (oldStatus) {

			case INIT:
				this.bscStatusInit++;

				if (trace) {
					_outln(curNode, "init begin, " + curNode.toString());
				}

				try {
					curNode.init();
				} finally {
					if (trace) {
						_outln(curNode, String.format("init end, rst=%s, status=%s, %s", "" + curNode.isSucc(),
								curNode.getStatus(), curNode.getStatusString()));
					}
				}

				if (curNode.getStatus() == BSStats.PROCESS && curNode.getChildCount() > 0) {
					curNode = curNode.getChild(0);
				}

				break;

			case PROCESS:
				this.bscStatusProcess++;

				if (lastNode != curNode && lastNode.getParentNode() != curNode) {
					throw new RException(
							String.format("%s is not child of %s", lastNode.getNodeName(), curNode.getNodeName()));
				}

				if (trace) {
					_outln(curNode, "process begin");
				}

				int nextChildIndex = lastNode.getIndexInParent() + 1;

				try {
					curNode.process(lastNode);
				} finally {
					if (trace) {
						_outln(curNode,
								String.format("process end, rst=%s, status=%s, child=%d/%d, %s", "" + curNode.isSucc(),
										curNode.getStatus(), nextChildIndex, curNode.getChildCount(),
										curNode.getStatusString()));
					}
				}

				// Process next node if have more child
				if (curNode.getStatus() == BSStats.PROCESS && nextChildIndex < curNode.getChildCount()) {
					curNode = curNode.getChild(nextChildIndex);
				}

				break;

			case COMPLETE:
				this.bscStatusComplete++;

				// re-check
				if (curNode.isSucc()) {

					if (trace) {
						_outln(curNode, "complete begin");
					}

					try {
						curNode.complete();
					} finally {
						if (trace) {
							_outln(curNode, String.format("complete end, rst=%s", "" + curNode.isSucc()));
						}
					}
				}

				if (curNode == rootNode) {
					break;
				}

				curNode = curNode.getParentNode();
				break;

			default:
				throw new RException("unknown status: " + curNode.getStatus());

			}

			if (curNode == oldNode && curNode.getStatus() == oldStatus) {
				throw new RException("dead loop found: " + curNode);

			} else {

				lastNode = oldNode;

				if (trace) {
					if (curNode != oldNode) {
						_outln(oldNode, "route to " + curNode.getNodeName());
					}
				}
			}

			this.bscOpLoop++;
		}

		rootNode.complete();
		if (trace) {
			_outln(rootNode, String.format("complete end, rst=%s", "" + rootNode.isSucc()));
		}

		if (!rootNode.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		return rootNode.buildResultTree(explain);
	}
}
