package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import alpha.rulp.runtime.IRIterator;

public class OrderList<K, T extends K> {

	private class XIterator implements IRIterator<T> {

		private boolean ended = false;

		private XOrderEntry<T> entry = null;

		private XOrderEntry<T> lastEntry = null;

		private int lastUpdate = -1;

		private K obj = null;

		public XIterator(XOrderEntry<T> entry, K obj) {
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

					entry = _findFirst(obj);

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

	private int build = 0;

	private Comparator<? super K> comparator;

	private ArrayList<XOrderEntry<T>> objList = new ArrayList<>();

	private int update = 0;

	public OrderList(Comparator<? super K> comparator) {
		super();
		this.comparator = comparator;
	}

	private int _compare(K o1, K o2) {
		return comparator.compare(o1, o2);
	}

	private XOrderEntry<T> _findAny(K obj) {

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

	private XOrderEntry<T> _findFirst(K obj) {

		XOrderEntry<T> entry = _findAny(obj);
		if (entry != null) {

			while (entry.index > 0) {
				XOrderEntry<T> prevousEntry = objList.get(entry.index - 1);
				if (_compare(prevousEntry.obj, obj) == 0) {
					entry = prevousEntry;
				} else {
					break;
				}
			}
		}

		return entry;
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

	public IRIterator<T> iterator(K obj) {

		_rebuild();

		return new XIterator(_findFirst(obj), obj);
	}

	public int size() {
		return objList.size();
	}
}
