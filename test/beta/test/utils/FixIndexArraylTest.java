package beta.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import alpha.rulp.utils.FileUtil;
import alpha.rulp.utils.FixIndexArray;
import alpha.rulp.utils.RuleTestBase;

class FixIndexArraylTest extends RuleTestBase {

	static String toString(FixIndexArray fa, int index) {
		StringBuffer sb = new StringBuffer();
		while (index != 0) {
			sb.append("" + index + ",");
			index = fa.getNextIndex(index);
		}
		return sb.toString();
	}

	protected void _dump(FixIndexArray fa, String outPath) {

		try {

			ArrayList<String> content = new ArrayList<>();

			content.add(String.format("capacity=%d, max-index=%d, use-size=%d, free-size=%d, free-index=%d",
					fa.getCapacity(), fa.getMaxIndex(), fa.getUsedSize(), fa.getFreeSize(), fa.getFreeIndex()));

			int maxIndex = fa.getMaxIndex();
			for (int index = 1; index <= maxIndex; ++index) {
				content.add(
						String.format("%04d: f=%04d n=%04d", index, fa.getFrontIndex(index), fa.getNextIndex(index)));
			}

			FileUtil.saveTxtFile(outPath, content);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test1_def_cap() {

		_setup();

		FixIndexArray fa = new FixIndexArray(0);

		{
			// malloc 3
			int index1 = 0;
			for (int i = 0; i < 3; ++i) {
				index1 = fa.malloc(index1);
			}

			assertEquals("3,2,1,", toString(fa, index1));
			_dump(fa, "result/utils/FixIndexArraylTest/test1_1.txt");

			// free all
			while (index1 != 0) {
				index1 = fa.free(index1);
			}

			_dump(fa, "result/utils/FixIndexArraylTest/test1_2.txt");
		}

		{
			// malloc 3
			int index2 = 0;
			for (int i = 0; i < 3; ++i) {
				index2 = fa.malloc(index2);
			}

			assertEquals("3,2,1,", toString(fa, index2));
			_dump(fa, "result/utils/FixIndexArraylTest/test1_3.txt");

			// free all
			while (index2 != 0) {
				index2 = fa.free(index2);
			}

			_dump(fa, "result/utils/FixIndexArraylTest/test1_4.txt");
		}
	}

	@Test
	void test2_expand() {

		_setup();

		FixIndexArray fa = new FixIndexArray(4);

		{
			// malloc 3
			int index1 = 0;
			for (int i = 0; i < 10; ++i) {
				index1 = fa.malloc(index1);
			}

			assertEquals("10,9,8,7,6,5,4,3,2,1,", toString(fa, index1));
			_dump(fa, "result/utils/FixIndexArraylTest/test2_1.txt");

			// free all
			while (index1 != 0) {
				index1 = fa.free(index1);
			}

			_dump(fa, "result/utils/FixIndexArraylTest/test2_2.txt");
		}

		{
			// malloc 3
			int index2 = 0;
			for (int i = 0; i < 10; ++i) {
				index2 = fa.malloc(index2);
			}

			assertEquals("10,9,8,7,6,5,4,3,2,1,", toString(fa, index2));
			_dump(fa, "result/utils/FixIndexArraylTest/test2_3.txt");

			// free all
			while (index2 != 0) {
				index2 = fa.free(index2);
			}

			_dump(fa, "result/utils/FixIndexArraylTest/test2_4.txt");
		}
	}

	@Test
	void test3_two_list() {

		_setup();

		FixIndexArray fa = new FixIndexArray(4);

		{
			// malloc 3
			int index1 = 0;
			int index2 = 0;
			for (int i = 0; i < 3; ++i) {
				index1 = fa.malloc(index1);
				index2 = fa.malloc(index2);
			}

			assertEquals("5,3,1,", toString(fa, index1));
			assertEquals("6,4,2,", toString(fa, index2));
			_dump(fa, "result/utils/FixIndexArraylTest/test3_1.txt");

			// free all
			while (index1 != 0) {
				index1 = fa.free(index1);
			}

			// free all
			while (index2 != 0) {
				index2 = fa.free(index2);
			}

			_dump(fa, "result/utils/FixIndexArraylTest/test3_2.txt");
		}

	}
}
