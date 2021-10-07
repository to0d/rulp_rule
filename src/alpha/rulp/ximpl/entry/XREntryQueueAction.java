package alpha.rulp.ximpl.entry;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.rule.IRRule;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.action.RActionType;

public class XREntryQueueAction extends XREntryQueueEmpty implements IREntryQueue, IRContext {

	protected LinkedList<IAction> actionStmtList = new LinkedList<>();

	protected RNodeContext defaultNodeConext = new RNodeContext();

	protected int entryRedundant = 0;

	protected boolean needVar = false;

	protected IRRule node;

	protected int nodeUpdateCount = 0;

	protected IRFrame queueFrame;

	public XREntryQueueAction(IRRule node) {
		super();
		this.node = node;
	}

	public void addActions(List<IAction> actions) {

		this.actionStmtList.addAll(actions);
		if (!needVar) {
			for (IAction action : actions) {
				if (action.getActionType() == RActionType.EXPR) {
					needVar = true;
					break;
				}
			}
		}
	}

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

		++nodeUpdateCount;

		/******************************************************/
		// Update variable value
		/******************************************************/
		if (needVar) {
			IRVar[] _vars = node.getVars();
			for (int i = 0; i < entry.size(); ++i) {
				IRVar var = _vars[i];
				if (var != null) {
					var.setValue(entry.get(i));
				}
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

			for (IAction action : actionStmtList) {

				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + action.toString());
				}

				action.doAction(entry, this);
			}

		} finally {

			// Update running count from context
			entryRedundant += context.tryAddStmt - context.actualAddStmt;

			// remove running context
			model.setNodeContext(null);
			context.currentEntry = null;

			// clean frame
			this.clean();
		}

		return true;
	}

	@Override
	public void clean() throws RException {
		if (queueFrame != null) {
			queueFrame.release();
			RulpUtil.decRef(queueFrame);
			queueFrame = null;
		}
	}

	@Override
	public int doGC() {
		return 0;
	}

	@Override
	public IRFrame findFrame() {
		return queueFrame;
	}

	public LinkedList<IAction> getActionStmtList() {
		return actionStmtList;
	}

	public RNodeContext getDefaultNodeContext() {
		defaultNodeConext.currentEntry = null;
		defaultNodeConext.currentNode = node;
		defaultNodeConext.tryAddStmt = 0;
		defaultNodeConext.actualAddStmt = 0;
		return defaultNodeConext;
	}

	@Override
	public IRFrame getFrame() throws RException {

		if (queueFrame == null) {
			queueFrame = RulpFactory.createFrame(node.getFrame(), "QF-" + node.getNodeName());
			RulpUtil.incRef(queueFrame);
		}

		return queueFrame;
	}

	@Override
	public IRInterpreter getInterpreter() {
		return node.getModel().getInterpreter();
	}

	@Override
	public IRModel getModel() {
		return node.getModel();
	}

	@Override
	public REntryQueueType getQueueType() {
		return REntryQueueType.ACTION;
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
