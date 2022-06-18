package alpha.rulp.ximpl.entry;

import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.rule.IRRule;
import alpha.rulp.utils.ReteUtil.OrderEntry;
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

			XREntryQueueSingle queue = new XREntryQueueSingle(node.getEntryLength());

			// Should not bind alpha node
			if (!RReteType.isAlphaType(node.getReteType())) {
				queue.setBindNode(node);
				queue.setEntryTable(node.getModel().getEntryTable());
			}

			return queue;

		case UNIQ:
			return new XREntryQueueUniq(node.getEntryLength());

		case ACTION:
			throw new RException("Unsupport type: " + type);

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
