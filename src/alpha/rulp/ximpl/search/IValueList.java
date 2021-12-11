package alpha.rulp.ximpl.search;

import alpha.rulp.lang.IRObject;
import alpha.rulp.runtime.IRIterator;

public interface IValueList {

	public IRIterator<? extends IRObject> iterator();

	public SVLType getSVType();

	public String getDescription();
}
