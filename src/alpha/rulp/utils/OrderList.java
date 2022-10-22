package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import alpha.rulp.runtime.IRIterator;

public class OrderList<T> {

	private class XIterator implements IRIterator<T> {

		private XOrderEntry<T> entry = null;

		private XOrderEntry<T> lastEntry = null;

		private T obj = null;

		private int lastUpdate = -1;

		private boolean ended = false;

		public XIterator(XOrderEntry<T> entry, T obj) {
			super();
			this.entry = entry;
			this.obj = obj;
		}

		@Override
		public boolean hasNext() {

			if (entry == null) {

				// rebuild order
				_rebuild();

				// Find first element
				if (lastEntry == null) {

					entry = _find(obj);

				} else {

					// list has been updated
					if (ended && lastUpdate != update) {
						ended = false;
						lastUpdate = update;
					}

					if (!ended) {

						int nextIndex = lastEntry.index + 1;
						if (nextIndex < objList.size()) {
							entry = objList.get(nextIndex);
						} else {
							ended = true;
						}

						if (obj != null && !ended && _compare(obj, entry.obj) != 0) {
							entry = null;
							ended = true;
						}
					}
				}
			}

			return entry != null;
		}

		@Override
		public T next() {

			if (!hasNext()) {
				return null;
			}

			lastEntry = entry;
			entry = null;

			return lastEntry.obj;
		}

	}

	public static class XOrderEntry<T> {

		public int index = -1;

		public T obj;

		public XOrderEntry(T obj) {
			super();
			this.obj = obj;
		}

		public String toString() {
			return obj.toString();

		}
	}

	private Comparator<? super T> comparator;

	private ArrayList<XOrderEntry<T>> objList = new ArrayList<>();

	private int update = 0;

	private int build = 0;

	public OrderList(Comparator<? super T> comparator) {
		super();
		this.comparator = comparator;
	}

	private int _compare(T o1, T o2) {
		return comparator.compare(o1, o2);
	}

	private XOrderEntry<T> _find(T obj) {

		int len = size();
		if (len == 0) {
			return null;
		}

		if (obj == null) {
			return objList.get(0);
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

	private void _rebuild() {

		if (update == build) {
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

		update = build;
	}

	public void add(T obj) {
		objList.add(new XOrderEntry<>(obj));
		update++;
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

	public int size() {
		return objList.size();
	}
}
