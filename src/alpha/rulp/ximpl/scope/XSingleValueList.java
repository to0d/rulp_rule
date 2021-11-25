package alpha.rulp.ximpl.scope;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.search.IValueList;

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
}
