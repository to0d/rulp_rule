package alpha.rulp.rule;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRRListenerX<T extends IRObject> {
	public void doAction(T[] objs) throws RException;
}