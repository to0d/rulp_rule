package alpha.rulp.ximpl.bs;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.node.IRNodeGraph;

public abstract class AbsBSEngine implements IRBSEngine {

	protected int bscOpLoop = 0;

	protected int bscOpSearch = 0;

	protected int bscStatusComplete = 0;

	protected int bscStatusDuplicate = 0;

	protected int bscStatusInit = 0;

	protected int bscStatusProcess = 0;

	protected IRNodeGraph graph;

	protected IRInterpreter interpreter;

	protected IRModel model;

	protected int nodeId = 0;

	protected boolean trace = false;

	public AbsBSEngine(IRModel model) {
		super();
		this.model = model;
		this.graph = model.getNodeGraph();
		this.interpreter = model.getInterpreter();
	}

	protected abstract void _addNode(IRBSNode node) throws RException;

	protected int _nextNodeId() {
		return nodeId++;
	}

	protected void _outln(String line) {
//		System.out.println(line);
		interpreter.out(line + "\n");
	}

	protected abstract IRList _search(IRList tree, boolean explain) throws RException;

	public void addNode(AbsBSNode node) throws RException {

		int nodeId = _nextNodeId();
		String nodeName = BSUtil.getBSNodeName(node.getType(), nodeId);

		node.setNodeId(nodeId);
		node.setNodeName(nodeName);
		node.setStatus(BSStats.INIT);
		node.setEngine(this);

		_addNode(node);
	}

	@Override
	public IRNodeGraph getGraph() {
		return graph;
	}

	@Override
	public IRModel getModel() {
		return model;
	}

	@Override
	public boolean hasStmt(IRBSNode node, IRList stmt) throws RException {

		boolean rc = model.findReteEntry(stmt) != null;
		if (trace) {
			trace_outln(node, "has stmt, stmt=" + stmt + ", rst=" + rc);
		}

		return rc;
	}

	@Override
	public boolean isTrace() {
		return trace;
	}

	public IRList search(IRList tree, boolean explain) throws RException {

		if (!BSUtil.isBSTree(tree)) {
			throw new RException("invalid bs tree: " + tree);
		}

		this.bscOpLoop = 0;
		this.bscOpSearch = 0;
		this.bscStatusInit = 0;
		this.bscStatusProcess = 0;
		this.bscStatusComplete = 0;
		this.bscStatusDuplicate = 0;

		try {

			this.trace = BSUtil.isBSTrace(model.getFrame());
			return _search(tree, explain);

		} finally {

			BSFactory.incBscOpLoop(bscOpLoop);
			BSFactory.incBscOpSearch(bscOpSearch);
			BSFactory.incBscStatusInit(bscStatusInit);
			BSFactory.incBscStatusProcess(bscStatusProcess);
			BSFactory.incBscStatusComplete(bscStatusComplete);
			BSFactory.incBscStatusDuplicate(bscStatusDuplicate);
		}
	}

	@Override
	public void trace_outln(IRBSNode node, String line) {
		_outln(String.format("%05d %s%s: %s", bscOpLoop, RulpUtil.getSpaceLine(node.getLevel()), node.getNodeName(),
				line));
	}
}
