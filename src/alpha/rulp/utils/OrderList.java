package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import alpha.rulp.runtime.IRIterator;

public class OrderList<T> {

	public static class XOrderEntry<T> {

		private T obj;

		public XOrderEntry(T obj) {
			super();
			this.obj = obj;
		}

		private int index = -1;
	}

	private Comparator<? super T> comparator;

	private ArrayList<XOrderEntry<T>> objList = new ArrayList<>();

	private int update = 0;

	private boolean _isUpdate() {
		return update > 0;
	}

	private int _compare(T o1, T o2) {
		return comparator.compare(o1, o2);
	}

	public OrderList(Comparator<? super T> comparator) {
		super();
		this.comparator = comparator;
	}

	public void add(T obj) {
		objList.add(new XOrderEntry<>(obj));
		update++;
	}

	public int size() {
		return objList.size();
	}

	public IRIterator<T> iterator() {

		_rebuild();

		XOrderEntry<T> entry = null;
		if (size() > 0) {
			entry = objList.get(0);
		}

		return new XIterator(entry, null);
	}

	public IRIterator<T> iterator(T obj) {

		_rebuild();

		return new XIterator(_find(obj), obj);
	}

	private void _rebuild() {

		if (update == 0) {
			return;
		}

		int size = size();

		if (size > 1) {
			Collections.sort(objList, (e1, e2) -> {
				return _compare(e1.obj, e2.obj);
			});
		}

		for (int i = 0; i < size; ++i) {
			objList.get(i).index = i;
		}

		update = 0;
	}

	private class XIterator implements IRIterator<T> {

		private XOrderEntry<T> entry;

		public XIterator(XOrderEntry<T> entry, T obj) {
			super();
			this.entry = entry;
			this.obj = obj;
		}

		private T obj;

		@Override
		public boolean hasNext() {

			if (entry == null) {
				if (_isUpdate()) {
					if (obj == null) {
						if (size() > 0) {
							entry = objList.get(0);
						}
					} else {

					}
				}
			}

			return false;
		}

		@Override
		public T next() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private XOrderEntry<T> _find(T obj) {

		int len = size();
		if (len == 0) {
			return null;
		}

		// half search

		int low = 0;
		int high = len - 1;
		while (low <= high) {

			if ((low + 1) >= high) {

				XOrderEntry<T> entry = objList.get(low);
				int d = _compare(entry.obj, obj);
				if (d == 0) {
					return entry;
				}

				if (d < 0) {
					low++;
					continue;
				}

				return null;
			}

			int mid = (high + low) / 2;
			XOrderEntry<T> entry = objList.get(mid);
			int d = _compare(entry.obj, obj);
			if (d == 0) {
				return entry;
			}

			if (d < 0) {
				low = mid + 1;
			} else {
				high = mid - 1;
			}
		}

		return null;
	}
}
