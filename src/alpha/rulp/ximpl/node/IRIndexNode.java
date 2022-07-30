package alpha.rulp.ximpl.node;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.utils.ReteUtil.OrderEntry;

public interface IRIndexNode extends IRReteNode {

	public List<OrderEntry> getOrderList();
}
