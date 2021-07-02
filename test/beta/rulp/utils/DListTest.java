package beta.rulp.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.DList;
import alpha.rulp.utils.DList.DCursor;

class DListTest {

	@Test
	void test1() {

		{
			DList<Integer> list = new DList<>();
			assertEquals(0, list.size());
			assertEquals("[]", list.toString());

			list.add(1);
			list.add(2);
			list.add(3);
			assertEquals(3, list.size());
			assertEquals("[1, 2, 3]", list.toString());

			list.set(1, null);
			assertEquals(3, list.size());
			assertEquals("[1, null, 3]", list.toString());

			assertEquals(1, list.shrink());
			assertEquals(2, list.size());
			assertEquals("[1, 3]", list.toString());
		}

		{
			DList<Integer> list = new DList<>();
			list.add(1);
			list.add(2);
			list.add(3);
			list.add(4);
			list.add(5);
			assertEquals(5, list.size());
			assertEquals("[1, 2, 3, 4, 5]", list.toString());

			list.set(1, null);
			list.set(3, null);
			assertEquals(5, list.size());
			assertEquals("[1, null, 3, null, 5]", list.toString());

			assertEquals(2, list.shrink());
			assertEquals(3, list.size());
			assertEquals("[1, 3, 5]", list.toString());
		}

		{
			DList<Integer> list = new DList<>();
			list.add(1);
			list.add(2);
			list.add(3);
			list.add(4);
			list.add(5);
			list.set(0, null);
			list.set(4, null);
			assertEquals(5, list.size());
			assertEquals("[null, 2, 3, 4, null]", list.toString());

			assertEquals(2, list.shrink());
			assertEquals(3, list.size());
			assertEquals("[2, 3, 4]", list.toString());
		}
	}

	@Test
	void test2() {

		{
			DList<Integer> list = new DList<>();
			list.add(1);
			list.add(2);
			list.add(3);

			DCursor<Integer> cursor = list.newCursor();
			assertEquals(1, cursor.getValue());

			assertEquals(true, cursor.hasNext());
			assertEquals(1, cursor.next());

			assertEquals(true, cursor.hasNext());
			assertEquals(2, cursor.next());

			assertEquals(true, cursor.hasNext());
			assertEquals(3, cursor.next());

			assertEquals(false, cursor.hasNext());
		}
	}

	void test3_not_support() {

		{
			DList<Integer> list = new DList<>();
			list.add(1);
			list.add(2);
			list.add(3);

			DCursor<Integer> cursor = list.newCursor();
			assertEquals(1, cursor.getValue());

			list.set(0, null);
			assertEquals(2, cursor.getValue());
		}
	}
}
