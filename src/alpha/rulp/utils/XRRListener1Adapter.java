package alpha.rulp.utils;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener1;

public class XRRListener1Adapter<T extends IRObject> implements IRRListener1<T> {

	private List<IRRListener1<T>> listenerList = new LinkedList<>();

	public void addListener(IRRListener1<T> listener) {
		listenerList.add(listener);
	}

	public void removeListener(IRRListener1<T> listener) {
		listenerList.remove(listener);
	}

	@Override
	public void doAction(T obj) throws RException {
		for (IRRListener1<T> listener : listenerList) {
			listener.doAction(obj);
		}
	}

}
