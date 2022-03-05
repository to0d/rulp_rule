package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Asc;
import static alpha.rulp.rule.Constant.A_Desc;
import static alpha.rulp.rule.Constant.A_Order_by;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRListener1;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.ReteUtil.OrderEntry;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1OrderBy extends AbsRConstraint1 implements IRConstraint1, IRListener1<IRReteEntry> {

	static String _toString(OrderEntry orderEntry) {
		return String.format("%s ?%d %s", A_Order_by, orderEntry.index, orderEntry.asc ? A_Asc : A_Desc);
	}

	private String _constraintExpression = null;

	private IRReteEntry lastEntry = null;

	private IRReteNode node;

	private final int[] orderColumnIndexs;

	private List<OrderEntry> orderList;

	private Map<String, IRReteEntry> uniqEntryMap = null;

	public XRConstraint1OrderBy(List<OrderEntry> orderList) {

		super();

		this.orderList = orderList;
		this.orderColumnIndexs = new int[orderList.size()];

		int index = 0;
		for (OrderEntry orderEntry : orderList) {
			this.orderColumnIndexs[index++] = orderEntry.index;
		}
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		IRReteEntry _last = _getLastEntry();
		String uniqName = null;

		if (_last != null) {

			int d = ReteUtil.compareEntry(_last, entry, orderList);
			if (d < 0) {
				return false;
			}

			if (d == 0) {

				if (uniqEntryMap != null) {
					uniqName = ReteUtil.uniqName(entry);
					if (uniqEntryMap.containsKey(uniqName)) {
						return false;
					}
				}

			} else {
				this.uniqEntryMap = null;
			}
		}

		if (uniqEntryMap == null) {
			uniqEntryMap = new HashMap<>();
		}

		if (uniqName == null) {
			uniqName = ReteUtil.uniqName(entry);
		}

		uniqEntryMap.put(uniqName, entry);
		lastEntry = entry;
		entry.addEntryRemovedListener(this);
		return true;
	}

	private IRReteEntry _getLastEntry() {

		if (lastEntry != null && lastEntry.isDroped()) {
			lastEntry = null;
		}

		if (lastEntry == null) {
			lastEntry = ReteUtil.getLastEntry(node.getEntryQueue());
		}

		return lastEntry;
	}

	@Override
	public void close() {

		if (uniqEntryMap == null) {
			return;
		}

		for (IRReteEntry entry : uniqEntryMap.values()) {
			entry.removeEntryRemovedListener(this);
		}

		uniqEntryMap = null;
	}

	@Override
	public void doAction(IRReteEntry obj) throws RException {

		if (uniqEntryMap == null) {
			return;
		}

		uniqEntryMap.remove(ReteUtil.uniqName(obj));
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {

			if (orderList.size() == 1) {
				_constraintExpression = "(" + _toString(orderList.get(0)) + ")";

			} else {

				_constraintExpression = "(";

				int index = 0;
				for (OrderEntry orderEntry : orderList) {
					if (index++ != 0) {
						_constraintExpression += " ";
					}
					_constraintExpression += _toString(orderEntry);
				}

				_constraintExpression += ")";
			}

//			if (orderColumnIndexs.length == 1) {
//
//			} else {
//
//				ArrayList<IRObject> indexs = new ArrayList<>();
//				for (int i : orderColumnIndexs) {
//					indexs.add(RulpFactory.createAtom("?" + i));
//				}
//
//				try {
//					_constraintExpression = String.format("(%s %s %s)", getConstraintName(),
//							RulpFactory.createNativeList(indexs), asc ? A_Asc : A_Desc);
//				} catch (RException e) {
//					e.printStackTrace();
//				}
//			}

		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return orderColumnIndexs;
	}

	@Override
	public String getConstraintName() {
		return A_Order_by;
	}

	public int[] getUniqColumnIndexs() {
		return orderColumnIndexs;
	}

	public int getUniqIndexCount() {
		return orderColumnIndexs.length;
	}

	@Override
	public void setNode(IRReteNode node) {
		this.node = node;
	}
}
