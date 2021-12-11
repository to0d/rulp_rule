package alpha.rulp.ximpl.search;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpUtil;

public class XListValueList implements IValueList {

	final IRList values;

	public XListValueList(IRList values) {
		super();
		this.values = values;
	}

	@Override
	public IRIterator<? extends IRObject> iterator() {
		return values.iterator();
	}

	public String toString() {
		return "" + values;
	}

	@Override
	public SVLType getSVType() {
		return SVLType.OBJ_LIST;
	}

	protected String _des;

	@Override
	public String getDescription() {

		if (_des == null) {
			try {
				_des = RulpUtil.toString(values);
			} catch (RException e) {
				e.printStackTrace();
				_des = e.toString();
			}
		}
		return _des;
	}

}