package alpha.rulp.ximpl.entry;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

public class XREntryQueueExecuteStmt extends XREntryQueueEmpty implements IREntryQueue {

	protected LinkedList<IRExpr> actionStmtList = new LinkedList<>();

	protected RNodeContext defaultNodeConext = new RNodeContext();

	protected int entryRedundant = 0;

	protected IRRule node;

	protected int nodeUpdateCount = 0;

	@Override
	public String getQueueDescription() {

		StringBuffer sb = new StringBuffer();

		int index = 0;
		for (IRExpr actionExpr : actionStmtList) {
			if (index++ != 0) {
				sb.append(" ");
			}
			sb.append(actionExpr.toString());
		}

		return sb.toString();
	}

	public XREntryQueueExecuteStmt(IRRule node) {
		super();
		this.node = node;
	}

	public void addActionStmts(List<IRExpr> actionStmtList) {
		this.actionStmtList.addAll(actionStmtList);
	}

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		++nodeUpdateCount;

		IRVar[] _vars = node.getVars();

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int i = 0; i < entry.size(); ++i) {
			IRVar var = _vars[i];
			if (var != null) {
				var.setValue(entry.get(i));
			}
		}

		/******************************************************/
		// Run actions
		/******************************************************/

		RNodeContext context = getDefaultNodeContext();

		IRModel model = node.getModel();
		IRInterpreter interpreter = model.getInterpreter();
		IRFrame execFrame = RulpFactory.createFrame(node.getNodeFrame(true), "NF-" + node.getNodeName());
		RulpUtil.incRef(execFrame);

		try {

			// Update running context
			context.currentEntry = entry;
			model.setNodeContext(context);

			for (IRExpr actionExpr : actionStmtList) {

				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + actionExpr.toString());
				}

				interpreter.compute(execFrame, actionExpr);
			}

		} finally {

			// Update running count from context
			entryRedundant += context.tryAddStmt - context.actualAddStmt;

			// remove running context
			model.setNodeContext(null);
			context.currentEntry = null;

			execFrame.release();
			RulpUtil.decRef(execFrame);
		}

		return true;
	}

	@Override
	public int doGC() {
		return 0;
	}

	public RNodeContext getDefaultNodeContext() {
		defaultNodeConext.currentEntry = null;
		defaultNodeConext.currentNode = node;
		defaultNodeConext.tryAddStmt = 0;
		defaultNodeConext.actualAddStmt = 0;
		return defaultNodeConext;
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.EXEC;
	}

	@Override
	public int getRedundantCount() {
		return entryRedundant;
	}

	@Override
	public int getUpdateCount() {
		return nodeUpdateCount;
	}

}
