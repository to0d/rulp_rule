package alpha.rulp.rule;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRRListener1<T extends IRObject> {
	public void doAction(T obj) throws RException;
}