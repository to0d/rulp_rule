package alpha.rulp.ximpl.entry;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.rule.RReteStatus;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.action.IActionNode1;
import alpha.rulp.ximpl.node.IRReteNode;

public class XREntryQueueAction extends XREntryQueueEmpty implements IREntryQueue {

	protected LinkedList<IActionNode1> actionStmtList = new LinkedList<>();

	protected RNodeContext defaultNodeConext = new RNodeContext() {
		public RReteStatus getNewStmtStatus() {
			return RReteStatus.REASONED;
		}
	};

	protected int entryRedundant = 0;

	protected IRReteNode node;

	protected int nodeUpdateCount = 0;

	public XREntryQueueAction(IRReteNode node) {
		super();
		this.node = node;
	}

	public void addActionNodes(List<IActionNode1> actionNodes) {
		this.actionStmtList.addAll(actionNodes);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		++nodeUpdateCount;

		/******************************************************/
		// Run actions
		/******************************************************/

		RNodeContext context = getDefaultNodeContext();
		IRModel model = node.getModel();

		try {

			// Update running context
			context.currentEntry = entry;
			model.setNodeContext(context);

			for (IActionNode1 action : actionStmtList) {

				if (RuleUtil.isModelTrace()) {
					System.out.println("\t" + action.toString());
				}

				action.doAction(node, entry);
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
