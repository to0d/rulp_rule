package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.OrderEntry;

public interface IRIndexNode extends IRReteNode {

	public List<OrderEntry> getOrderList();
}
