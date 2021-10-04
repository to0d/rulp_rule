package alpha.rulp.ximpl.entry;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRModel.RNodeContext;
import alpha.rulp.utils.RuleUtil;
import alpha.rulp.ximpl.action.IAction;
import alpha.rulp.ximpl.node.IRReteNode;

public class XREntryQueueAction extends XREntryQueueEmpty implements IREntryQueue {

	protected LinkedList<IAction> actionStmtList = new LinkedList<>();

	protected RNodeContext defaultNodeConext = new RNodeContext();

	protected int entryRedundant = 0;

	protected IRReteNode node;

	protected int nodeUpdateCount = 0;

	@Override
	public String getQueueDescription() {

		StringBuffer sb = new StringBuffer();

		int index = 0;
		for (IAction action : actionStmtList) {
			if (index++ != 0) {
				sb.append(" ");
			}
			sb.append(action.toString());
		}

		return sb.toString();
	}

	public XREntryQueueAction(IRReteNode node) {
		super();
		this.node = node;
	}

	public void addActionNodes(List<IAction> actionNodes) {
		this.actionStmtList.addAll(actionNodes);
	}

	@Override
	public boolean addEntry(IRReteEntry entry) throws RException {

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

			for (IAction action : actionStmtList) {

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
		defaultNodeConext.currentNode = node;
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
