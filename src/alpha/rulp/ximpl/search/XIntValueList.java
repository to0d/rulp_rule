package alpha.rulp.ximpl.search;

import alpha.rulp.lang.IRInteger;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.RulpFactory;

public class XIntValueList implements IValueList {

	final int fromValue;

	final int step;

	final int toValue;

	public XIntValueList(int fromValue, int toValue, int step) {
		super();
		this.fromValue = fromValue;
		this.toValue = toValue;
		this.step = step;
	}

	@Override
	public IRIterator<IRObject> iterator() {

		return new IRIterator<IRObject>() {

			int curValue = fromValue;

			@Override
			public boolean hasNext() throws RException {

				if (step > 0) {
					return curValue <= toValue;
				} else {
					return curValue >= toValue;
				}
			}

			@Override
			public IRInteger next() throws RException {

				int value = curValue;
				curValue += step;
				return RulpFactory.createInteger(value);
			}
		};
	}

	public String toString() {
		return String.format("[%d, %d](%d)", fromValue, toValue, step);
	}

}
