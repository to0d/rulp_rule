package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Comparator;

public class HeapStack<T> {

	private ArrayList<T> array = new ArrayList<>();

	private Comparator<T> comparator;

	private boolean ordered = false;

	private int size;

	public HeapStack(Comparator<T> comparator) {

		super();

		this.comparator = comparator;
		this.ordered = false;
		this.size = 0;
	}

	private void _adjustHeap() {

		if (!ordered) {

			for (int h = size / 2; h >= 0; --h) {
				_adjustHeap(h, size - 1);
			}

			ordered = true;
		}
	}

	private void _adjustHeap(int s, int m) {

		if ((2 * s + 1) > m) {
			return;
		}

		T guard = array.get(s);
		int count = 0;

		for (int j = 2 * s + 1; j <= m; j = 2 * j + 1) {

			if (j < m && _compareIt(array.get(j), array.get(j + 1)) < 0) {
				++j;
			}

			if (_compareIt(guard, array.get(j)) >= 0) {
				break;
			}

			array.set(s, array.get(j));
			s = j;
			++count;
		}

		if (count > 0) {
			array.set(s, guard);
		}
	}

	private int _compareIt(T o1, T o2) {
		return comparator.compare(o1, o2);
	}

	public void clean() {

		this.size = 0;
		this.ordered = false;
		this.array.clear();
	}

	public T peek() {

		if (size == 0) {
			return null;
		}

		_adjustHeap();

		return array.get(0);
	}

	public T pop() {

		if (size == 0) {
			return null;
		}

		_adjustHeap();

		T obj = array.get(0);

		if (size > 1) {
			array.set(0, array.get(size - 1));
			array.set(size - 1, null);
		}

		this.size--;
		this.ordered = false;

		return obj;
	}

	public void push(T obj) {

		if (array.size() == size) {
			array.add(obj);
		} else {
			array.set(size, obj);
		}

		this.size++;
		this.ordered = false;
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("[");
		for (int i = 0; i < size; ++i) {

			T e = array.get(i);
			if (i != 0) {
				sb.append(",");
			}

			sb.append("" + e);
		}
		sb.append("]");

		return sb.toString();
	}

}
