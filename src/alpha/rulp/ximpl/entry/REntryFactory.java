package alpha.rulp.ximpl.entry;

import java.util.List;

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
}
