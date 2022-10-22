package beta.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRIterator;
import alpha.rulp.utils.OrderList;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.utils.RulpUtil;

class OrderListTest extends RuleTestBase {

	static void _add(OrderList<IRList, IRList> list, String key, int value) {
		try {
			list.add(RulpFactory.createList(RulpFactory.createAtom(key), RulpFactory.createInteger(value)));
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	static void _add(OrderList<IRList, IRList> list, int key, int value) {
		try {
			list.add(RulpFactory.createList(RulpFactory.createInteger(key), RulpFactory.createInteger(value)));
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	static OrderList<IRList, IRList> _makeList() {

		OrderList<IRList, IRList> list = new OrderList<>((e1, e2) -> {
			try {
				return RulpUtil.compare(e1.get(0), e2.get(0));
			} catch (RException e) {
				e.printStackTrace();
				return 0;
			}
		});

		return list;
	}

	static String _toList(IRIterator<IRList> it) {

		StringBuffer sb = new StringBuffer();

		try {
			while (it.hasNext()) {
				sb.append(" " + it.next());
			}
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		return sb.toString().trim();
	}

	static String _toList(OrderList<IRList, IRList> list) {

		StringBuffer sb = new StringBuffer();

		try {
			IRIterator<IRList> it = list.iterator();
			while (it.hasNext()) {
				sb.append(" " + it.next());
			}
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		return sb.toString().trim();
	}

	static String _toList(OrderList<IRList, IRList> list, String key) {

		StringBuffer sb = new StringBuffer();

		try {
			IRIterator<IRList> it = list
					.iterator(RulpFactory.createList(RulpFactory.createAtom(key), RulpFactory.createInteger(0)));
			while (it.hasNext()) {
				sb.append(" " + it.next());
			}
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		return sb.toString().trim();
	}

	static String _toList(OrderList<IRList, IRList> list, int key) {

		StringBuffer sb = new StringBuffer();

		try {
			IRIterator<IRList> it = list
					.iterator(RulpFactory.createList(RulpFactory.createInteger(key), RulpFactory.createInteger(0)));
			while (it.hasNext()) {
				sb.append(" " + it.next());
			}
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		return sb.toString().trim();
	}

	@Test
	void test_order_list_1() {

		_setup();

		OrderList<IRList, IRList> list = _makeList();

		_add(list, "a", 1);
		_add(list, "c", 1);
		_add(list, "b", 1);
		_add(list, "c", 2);

		assertEquals(4, list.size());
		assertEquals("'(a 1) '(b 1) '(c 1) '(c 2)", _toList(list));
	}

	@Test
	void test_order_list_5() {

		_setup();

		OrderList<IRList, IRList> list = _makeList();

		_add(list, 2, 3);
		_add(list, 2, 5);
		_add(list, 2, 4);
		_add(list, 3, 2);

		assertEquals(4, list.size());
		assertEquals("'(2 3) '(2 5) '(2 4)", _toList(list, 2));
	}

	@Test
	void test_order_list_2_stable_sorting() {

		_setup();

		OrderList<IRList, IRList> list = _makeList();

		_add(list, "a", 1);
		_add(list, "c", 2);
		_add(list, "b", 1);
		_add(list, "c", 1);

		assertEquals(4, list.size());
		assertEquals("'(a 1) '(b 1) '(c 2) '(c 1)", _toList(list));
	}

	@Test
	void test_order_list_3_stable_iterator_1() {

		_setup();

		OrderList<IRList, IRList> list = _makeList();

		_add(list, "a", 1);
		_add(list, "c", 2);
		_add(list, "b", 1);
		_add(list, "c", 1);

		assertEquals(4, list.size());
		assertEquals("'(b 1)", _toList(list, "b"));
		assertEquals("'(c 2) '(c 1)", _toList(list, "c"));
	}

	@Test
	void test_order_list_4_iterator_dynamic_change() {

		_setup();

		OrderList<IRList, IRList> list = _makeList();

		_add(list, "a", 1);
		_add(list, "c", 2);
		_add(list, "b", 1);

		IRIterator<IRList> it = null;
		try {
			it = list.iterator(RulpFactory.createList(RulpFactory.createAtom("c"), RulpFactory.createInteger(0)));
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}

		assertEquals("'(c 2)", _toList(it));

		_add(list, "c", 1);
		assertEquals("'(c 1)", _toList(it));
	}
}
