package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRModel;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.ximpl.action.ActionUtil;
import alpha.rulp.ximpl.entry.XREntryIteratorBuilderOrderBy.OrderEntry;

public class REntryFactory {

	public static IREntryIteratorBuilder defaultBuilder() {
		return new XREntryIteratorBuilderDefault();
	}

	public static IREntryIteratorBuilder orderByBuilder(List<OrderEntry> orders) {
		return new XREntryIteratorBuilderOrderBy(orders);
	}

	public static IREntryIteratorBuilder reverseBuilder() {
		return new XREntryIteratorBuilderReverse();
	}

	public static IREntryQueue createSingleQueue(int entryLength, IRReteNode node, IREntryTable entryTable)
			throws RException {

		XREntryQueueSingle queue = new XREntryQueueSingle(entryLength);
		queue.setBindNode(node);
		queue.setEntryTable(entryTable);

		return queue;
	}

	public static IREntryQueue createActionQueue(IRRule node, List<IRExpr> actionList) throws RException {

		IRModel model = node.getModel();
		IRObject[] varEntry = node.getVarEntry();

		XREntryQueueAction queue = new XREntryQueueAction(node);

		for (IRExpr actionStmt : actionList) {
			queue.addActions(ActionUtil.buildActions(model, varEntry, actionStmt));
		}

		return queue;
	}

	public static IREntryQueue createQueue(REntryQueueType type, int entryLength) throws RException {

		switch (type) {

		case EMPTY:
			return new XREntryQueueEmpty();

		case MULTI:
			return new XREntryQueueMulit(entryLength);

		case SINGLE:
			return createSingleQueue(entryLength, null, null);

		case UNIQ:
			return new XREntryQueueUniq(entryLength);

		case ACTION:
			throw new RException("Unsupport type: " + type);

		default:
			throw new RException("Unknown type: " + type);
		}
	}
}
