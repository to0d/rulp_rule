package alpha.rulp.utils;

import java.util.LinkedList;
import java.util.List;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListenerX;

public class XRRListenerXAdapter<T extends IRObject> implements IRRListenerX<T> {

	@SuppressWarnings("unchecked")
	public static <T> T[] make(T o0) throws RException {
		Object[] objs = new Object[1];
		objs[0] = o0;
		return (T[]) objs;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] make(T o0, T o1) throws RException {
		Object[] objs = new Object[2];
		objs[0] = o0;
		objs[1] = o1;
		return (T[]) objs;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] make(T o0, T o1, T o2) throws RException {
		Object[] objs = new Object[3];
		objs[0] = o0;
		objs[1] = o1;
		objs[2] = o2;
		return (T[]) objs;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] make(T o0, T o1, T o2, T o3) throws RException {
		Object[] objs = new Object[4];
		objs[0] = o0;
		objs[1] = o1;
		objs[2] = o2;
		objs[3] = o3;
		return (T[]) objs;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] make(T o0, T o1, T o2, T o3, T o4) throws RException {
		Object[] objs = new Object[5];
		objs[0] = o0;
		objs[1] = o1;
		objs[2] = o2;
		objs[3] = o3;
		objs[4] = o4;
		return (T[]) objs;
	}

	private List<IRRListenerX<T>> listenerList = new LinkedList<>();

	public void addListener(IRRListenerX<T> listener) {
		listenerList.add(listener);
	}

	@Override
	public void doAction(T[] objs) throws RException {
		for (IRRListenerX<T> listener : listenerList) {
			listener.doAction(objs);
		}
	}

}
