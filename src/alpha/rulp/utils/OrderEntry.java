package alpha.rulp.utils;

import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Order_by;

import java.util.List;

public class OrderEntry {

	public static String toString(List<OrderEntry> orderList) {

		if (orderList.size() == 1) {
			return "(" + orderList.get(0) + ")";

		} else {

			StringBuffer sb = new StringBuffer();
			sb.append("(");

			int index = 0;
			for (OrderEntry orderEntry : orderList) {
				if (index++ != 0) {
					sb.append(" ");
				}
				sb.append(A_Order_by);
				sb.append(" ");
				sb.append(orderEntry.index);
				sb.append(" ");
				sb.append(orderEntry.asc ? A_Asc : A_Desc);
			}

			sb.append(")");

			return sb.toString();
		}
	}

	public boolean asc;

	public int index;

	public String toString() {
		return String.format("%s ?%d %s", A_Order_by, index, asc ? A_Asc : A_Desc);
	}
}
