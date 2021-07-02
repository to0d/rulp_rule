package alpha.rulp.ximpl.scope;

import alpha.rulp.lang.IRObject;
import alpha.rulp.runtime.IRIterator;

public interface IValueListFactory {

	public IRIterator<? extends IRObject> iterator();
}
