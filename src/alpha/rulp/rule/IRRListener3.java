package alpha.rulp.rule;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;

public interface IRRListener3<T1 extends IRObject, T2 extends IRObject, T3 extends IRObject> {
	public void doAction(T1 o1, T2 o2, T3 o3) throws RException;
}