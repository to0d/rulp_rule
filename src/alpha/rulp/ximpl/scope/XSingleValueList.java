package alpha.rulp.ximpl.scope;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.search.IValueList;
import alpha.rulp.ximpl.search.SVLType;

public class XSingleValueList implements IValueList {

	final IRObject singleValue;

	public XSingleValueList(IRObject singleValue) {
		super();
		this.singleValue = singleValue;
	}

	@Override
	public IRIterator<IRObject> iterator() {

		return new IRIterator<IRObject>() {

			IRObject curValue = singleValue;

			@Override
			public boolean hasNext() throws RException {
				return curValue != null;
			}

			@Override
			public IRObject next() throws RException {
				IRObject value = curValue;
				curValue = null;
				return value;
			}
		};
	}

	public String toString() {
		return String.format("(%s)", singleValue);
	}

	@Override
	public SVLType getSVType() {
		return SVLType.SINGLE;
	}

	protected String _des;

	@Override
	public String getDescription() {

		if (_des == null) {
			try {
				_des = RulpUtil.toString(singleValue);
			} catch (RException e) {
				e.printStackTrace();
				_des = e.toString();
			}
		}
		return _des;
	}
}
