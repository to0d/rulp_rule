/* Copyright Prolog                                  */
/*                                                   */
/* RULP(Run a Lisp Processer) on Java                */
/* 													 */
/* Copyright (C) 2020 Todd (to0d@outlook.com)        */
/* This program comes with ABSOLUTELY NO WARRANTY;   */
/* This is free software, and you are welcome to     */
/* redistribute it under certain conditions.         */

package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class DList<T> {

	public static interface DCursor<T> {

		public int getIndex();

		public T getValue();

		public boolean hasNext();

		public T next();

		public T next(int d);

		public void setIndex(int index);
	}

	class XDCursor implements DCursor<T> {

		AtomicInteger d;

		private int index = 0;

		public XDCursor(int index) {
			super();
			this.index = index;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public T getValue() {
			return get(index);
		}

		@Override
		public boolean hasNext() {
			return index < size;
		}

		@Override
		public T next() {

			if (index >= size) {
				return null;
			}

			return get(index++);
		}

		@Override
		public T next(int d) {

			if (index >= size) {
				return null;
			}

			T v = get(index);
			index += d;
			return v;
		}

		@Override
		public void setIndex(int index) {
			this.index = index;
		}

	}

	private ArrayList<XDCursor> cursorList = null;

	private ArrayList<T> elementList = null;

	private int size = 0;

	public void add(T v) {

		if (elementList == null) {
			elementList = new ArrayList<>();
		}

		elementList.add(v);
		++size;
	}

	public T get(int index) {
		return index < size ? elementList.get(index) : null;
	}

	public DCursor<T> newCursor() {

		if (cursorList == null) {
			cursorList = new ArrayList<>();
		}

		XDCursor cursor = new XDCursor(0);
		cursorList.add(cursor);
		return cursor;
	}

	public void set(int index, T v) {
		elementList.set(index, v);
	}

	public int shrink() {

		int srcPos = 0;

		/*******************************************************/
		// Find first null element
		/*******************************************************/
		while (srcPos < size && elementList.get(srcPos) != null) {
			++srcPos;
		}

		// no null found
		if (srcPos == size) {
			return 0;
		}

		/*******************************************************/
		// Move elements
		/*******************************************************/
		int dstPos = srcPos++;
		for (; srcPos < size; ++srcPos) {
			T v = elementList.get(srcPos);
			if (v != null) {
				elementList.set(dstPos++, v);
			}
		}

		/*******************************************************/
		// Reorder cursor
		/*******************************************************/
		if (cursorList != null) {
			Collections.sort(cursorList, (i1, i2) -> {
				return i2.index - i1.index;
			});
		}

		/*******************************************************/
		// Remove tail null elements
		/*******************************************************/
		int removePos = size - 1;
		int removeCount = 0;
		while (removePos >= dstPos) {
			elementList.remove(removePos--);
			++removeCount;
		}

		size = elementList.size();
		return removeCount;
	}

	public int size() {
		return size;
	}

	public String toString() {
		return size == 0 ? "[]" : elementList.toString();
	}

}
