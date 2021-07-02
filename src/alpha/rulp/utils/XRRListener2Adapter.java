package alpha.rulp.utils;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener2;

public class XRRListener2Adapter<T1 extends IRObject, T2 extends IRObject> implements IRRListener2<T1, T2> {

	private List<IRRListener2<T1, T2>> listenerList = new LinkedList<>();

	public void addListener(IRRListener2<T1, T2> listener) {
		listenerList.add(listener);
	}

	@Override
	public void doAction(T1 o1, T2 o2) throws RException {
		for (IRRListener2<T1, T2> listener : listenerList) {
			listener.doAction(o1, o2);
		}
	}

}
