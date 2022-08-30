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

	protected boolean _isCircularProof(IRList stmt) throws RException {
		return visitingOrNodeMap.containsKey(ReteUtil.uniqName(stmt));
	}

	protected AbsBSNode _newAndNode(IRList stmt, SourceNode sourceNode, IAction action) {

		int nodeId = _nextNodeId();

		XRBSNodeAnd node = new XRBSNodeAnd(this, nodeId, String.format("A%04d", nodeId));
		node.stmt = stmt;
		node.status = BSStats.INIT;
		node.sourceNode = sourceNode;
		node.action = action;

		this.bscNodeAnd++;

		return node;
	}

	protected AbsBSNode _newOrNode(IRList stmt) throws RException {

		int nodeId = _nextNodeId();

		XRBSNodeOr node = new XRBSNodeOr(this, nodeId, String.format("O%04d", nodeId));
		node.stmt = stmt;
		node.status = BSStats.INIT;

		this.bscNodeOr++;

		visitingOrNodeMap.put(ReteUtil.uniqName(stmt), node);

		return node;
	}

	protected AbsBSNode _newQueryNode(List<IRList> stmtList) throws RException {

		int nodeId = _nextNodeId();

		XRBSNodeQuery node = new XRBSNodeQuery(this, nodeId, String.format("Q%04d", nodeId));
		node.queryReteNodeTree = RulpFactory.createList(stmtList);
		node.status = BSStats.INIT;
		this.bscNodeQuery++;

		return node;
	}

	protected int _nextNodeId() {
		return nodeId++;
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

	public IRNodeGraph getGraph() {
		return graph;
	}

	public XRModel getModel() {
		return model;
	}

	public boolean hasStmt(IRBSNode node, IRList stmt) throws RException {

		boolean rc = model.hasStatement(stmt);
		if (trace) {
			outln(node, "has stmt, stmt=" + stmt + ", rst=" + rc);
		}

		return rc;
	}

	public boolean isTrace() {
		return trace;
	}

	public void outln(IRBSNode node, String line) {
		outln(String.format("%05d %s%s: %s", getBscOpLoop(), RulpUtil.getSpaceLine(node.getLevel()), node.getNodeName(),
				line));
	}

	public void outln(String line) {
//		System.out.println(line);
		interpreter.out(line + "\n");
	}

//	protected BSNode _buildTree(IRList tree) throws RException {
//
//	}

	public IRList search(IRList tree, boolean explain) throws RException {

		if (!BSUtil.isBSTree(tree)) {
			throw new RException("invalid bs tree: " + tree);
		}

		trace = BSUtil.isBSTrace(model.getFrame());
		rootNode = _newOrNode(tree);

		if (trace) {
			outln(rootNode, "create_root, " + rootNode.toString());
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
					outln(curNode, "init begin, " + curNode.toString());
				}

				try {
					curNode.init();
				} finally {
					if (trace) {
						outln(curNode, String.format("init end, rst=%s, status=%s, %s", "" + curNode.isSucc(),
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
					outln(curNode, "process begin");
				}

				int nextChildIndex = lastNode.getIndexInParent() + 1;

				try {
					curNode.process(lastNode);
				} finally {
					if (trace) {
						outln(curNode,
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
						outln(curNode, "complete begin");
					}

					try {
						curNode.complete();
					} finally {
						if (trace) {
							outln(curNode, String.format("complete end, rst=%s", "" + curNode.isSucc()));
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
						outln(oldNode, "route to " + curNode.getNodeName());
					}
				}
			}

			this.bscOpLoop++;
		}

		rootNode.complete();
		if (trace) {
			outln(rootNode, String.format("complete end, rst=%s", "" + rootNode.isSucc()));
		}

		if (!rootNode.isSucc()) {
			return RulpFactory.emptyConstList();
		}

		return rootNode.buildResultTree(explain);
	}
}
