package beta.rulp.utils;

import java.util.Comparator;

import alpha.rulp.utils.HeapStack;
import junit.framework.TestCase;

public class HeapStackTest extends TestCase {

	static class Item {

		public String name;
		public int value;

		public Item(String name, int value) {
			super();
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public void test_1() {

		HeapStack<Item> heapArray = new HeapStack<>(new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				return o1.value - o2.value;
			}
		});

		heapArray.push(new Item("a", 10));
		heapArray.push(new Item("b", 5));
		heapArray.push(new Item("c", 8));
		assertEquals(3, heapArray.size());
		assertEquals("[a,b,c]", heapArray.toString());

		assertEquals("a", heapArray.peek().name);
		assertEquals(3, heapArray.size());
		assertEquals("[a,b,c]", heapArray.toString());

		assertEquals("a", heapArray.pop().name);
		assertEquals(2, heapArray.size());
		assertEquals("[c,b]", heapArray.toString());

		heapArray.push(new Item("d", 1));
		assertEquals("[c,b,d]", heapArray.toString());

		assertEquals("c", heapArray.pop().name);
		assertEquals(2, heapArray.size());
		assertEquals("[d,b]", heapArray.toString());

		assertEquals("b", heapArray.pop().name);
		assertEquals(1, heapArray.size());
		assertEquals("[d]", heapArray.toString());
	}

}
