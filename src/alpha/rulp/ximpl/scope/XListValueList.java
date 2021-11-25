package alpha.rulp.ximpl.scope;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.ximpl.search.IValueList;

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

}