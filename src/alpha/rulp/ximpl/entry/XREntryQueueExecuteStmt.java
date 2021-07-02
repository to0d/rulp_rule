package alpha.rulp.ximpl.entry;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.node.IRReteNode;

public class XREntryQueueExecuteStmt extends XREntryQueueEmpty implements IREntryQueue {

	protected LinkedList<IRExpr> actionStmtList = new LinkedList<>();

	protected RNodeContext defaultNodeConext = new RNodeContext() {
		public RReteStatus getNewStmtStatus() {
			return RReteStatus.REASONED;
		}
	};

	protected int entryRedundant = 0;

	protected IRReteNode node;

	protected int nodeUpdateCount = 0;

	protected IRVar[] ruleVars = null;

	protected IRObject[] varEntry;

	public XREntryQueueExecuteStmt(IRReteNode node) {
		super();
		this.node = node;
	}

	public void addActionStmts(List<IRExpr> actionStmtList) {
		this.actionStmtList.addAll(actionStmtList);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		++nodeUpdateCount;

		IRVar[] _vars = getVars();

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

		try {

			// Update running context
			context.currentEntry = entry;
			model.setNodeContext(context);

			for (IRExpr actionExpr : actionStmtList) {

				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + actionExpr.toString());
				}

				interpreter.compute(frame, actionExpr);
			}

		} finally {

			// Update running count from context
			entryRedundant += context.tryAddStmt - context.actualAddStmt;

			// remove running context
			model.setNodeContext(null);
			context.currentEntry = null;
		}

		return true;
	}

	@Override
	public int doGC() {
		return 0;
	}

	public RNodeContext getDefaultNodeContext() {
		defaultNodeConext.currentEntry = null;
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

	public IRVar[] getVars() throws RException {

		if (ruleVars == null) {

			ruleVars = new IRVar[varEntry.length];

			IRFrame frame = node.getNodeFrame(true);

			for (int i = 0; i < varEntry.length; ++i) {

				IRObject obj = varEntry[i];
				if (obj != null) {
					ruleVars[i] = frame.addVar(RulpUtil.asAtom(obj).getName());
				}
			}
		}

		return ruleVars;
	}

	public void setVarEntry(IRObject[] varEntry) {
		this.varEntry = varEntry;
	}

}
