package alpha.rulp.utils;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener4;

public class XRRListener4Adapter<T1 extends IRObject, T2 extends IRObject, T3 extends IRObject, T4 extends IRObject>
		implements IRRListener4<T1, T2, T3, T4> {

	private List<IRRListener4<T1, T2, T3, T4>> listenerList = new LinkedList<>();

	public void addListener(IRRListener4<T1, T2, T3, T4> listener) {
		listenerList.add(listener);
	}

	@Override
	public void doAction(T1 o1, T2 o2, T3 o3, T4 o4) throws RException {
		for (IRRListener4<T1, T2, T3, T4> listener : listenerList) {
			listener.doAction(o1, o2, o3, o4);
		}
	}

}
