package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.utils.OrderEntry;
import alpha.rulp.ximpl.node.IRIndexNode;
import alpha.rulp.ximpl.node.RReteType;

public class REntryFactory {

	public static IREntryQueue createActionQueue(IRRule node) throws RException {
		return new XREntryQueueAction(node);
	}

	public static IREntryQueue createQueue(REntryQueueType type, IRReteNode node) throws RException {

		switch (type) {

		case EMPTY:
			return new XREntryQueueEmpty();

		case MULTI:
			return new XREntryQueueMulit(node.getEntryLength());

		case SINGLE:
			XREntryQueueSingle singleQueue = new XREntryQueueSingle(node.getEntryLength());

			// Should not bind alpha node
			if (!RReteType.isAlphaType(node.getReteType())) {
				singleQueue.setBindNode(node);
				singleQueue.setEntryTable(node.getModel().getEntryTable());
			}

			return singleQueue;

		case UNIQ:
			return new XREntryQueueUniq(node.getEntryLength());

		case ACTION:
			throw new RException("Unsupport type: " + type);

		case ORDER:
			XREntryQueueOrder orderQueue = new XREntryQueueOrder(node.getEntryLength());
			orderQueue.setOrderList(((IRIndexNode) node).getOrderList());
			return orderQueue;

		default:
			throw new RException("Unknown type: " + type);
		}
	}

	public static IREntryIteratorBuilder defaultBuilder() {
		return new XREntryIteratorBuilderDefault();
	}

	public static IREntryIteratorBuilder orderByBuilder(List<OrderEntry> orders) {
		return new XREntryIteratorBuilderOrderBy(orders);
	}

	public static IREntryIteratorBuilder reverseBuilder() {
		return new XREntryIteratorBuilderReverse();
	}
}
