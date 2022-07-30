package alpha.rulp.ximpl.constraint;

import java.util.List;

import alpha.rulp.utils.ReteUtil.OrderEntry;

public interface IRConstraint1OrderBy extends IRConstraint1 {

	public List<OrderEntry> getOrderList();
}
