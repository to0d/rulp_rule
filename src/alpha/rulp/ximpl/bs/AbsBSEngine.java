package alpha.rulp.ximpl.bs;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.node.IRNodeGraph;

public abstract class AbsBSEngine implements IRBSEngine {

	protected int bscCircularProof = 0;

	protected int bscNodeLogicAnd = 0;

	protected int bscNodeLogicOr = 0;

	protected int bscNodeStmtAnd = 0;

	protected int bscNodeStmtOr = 0;

	protected int bscNodeStmtQuery = 0;

	protected int bscOpLoop = 0;

	protected int bscOpRelocate = 0;

	protected int bscOpSearch = 0;

	protected int bscStatusComplete = 0;

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

	protected abstract void _addNode(AbsBSNode node) throws RException;

	protected int _nextNodeId() {
		return nodeId++;
	}

	protected void _outln(String line) {
//		System.out.println(line);
		interpreter.out(line + "\n");
	}

	protected abstract IRList _search(IRList tree, boolean explain) throws RException;

	public void addNode(AbsBSNode node) throws RException {

		_addNode(node);

		switch (node.getType()) {
		case LOGIC_AND:
			this.bscNodeLogicAnd++;
			break;

		case LOGIC_OR:
			this.bscNodeLogicOr++;
			break;

		case STMT_AND:
			this.bscNodeStmtAnd++;
			break;

		case STMT_OR:
			this.bscNodeStmtOr++;
			break;

		case STMT_QUERY:
			this.bscNodeStmtQuery++;
			break;

		default:
			break;
		}

		int nodeId = _nextNodeId();
		String nodeName = BSUtil.getBSNodeName(node.getType(), nodeId);

		node.setNodeId(nodeId);
		node.setNodeName(nodeName);
		node.setStatus(BSStats.INIT);
		node.setEngine(this);

	}

	public int getBscCircularProof() {
		return bscCircularProof;
	}

	public int getBscNodeLogicAnd() {
		return bscNodeLogicAnd;
	}

	public int getBscNodeLogicOr() {
		return bscNodeLogicOr;
	}

	public int getBscNodeStmtAnd() {
		return bscNodeStmtAnd;
	}

	public int getBscNodeStmtOr() {
		return bscNodeStmtOr;
	}

	public int getBscNodeStmtQuery() {
		return bscNodeStmtQuery;
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

		boolean rc = model.hasStatement(stmt);
		if (trace) {
			trace_outln(node, "has stmt, stmt=" + stmt + ", rst=" + rc);
		}

		return rc;
	}

	@Override
	public void incBscOpRelocate(int count) {
		this.bscOpRelocate += count;
	}

	@Override
	public boolean isTrace() {
		return trace;
	}

	public IRList search(IRList tree, boolean explain) throws RException {

		if (!BSUtil.isBSTree(tree)) {
			throw new RException("invalid bs tree: " + tree);
		}

		this.trace = BSUtil.isBSTrace(model.getFrame());

		return _search(tree, explain);
	}

	@Override
	public void trace_outln(IRBSNode node, String line) {
		_outln(String.format("%05d %s%s: %s", getBscOpLoop(), RulpUtil.getSpaceLine(node.getLevel()),
				node.getNodeName(), line));
	}
}
