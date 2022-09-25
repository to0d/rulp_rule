package beta.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.IndexUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.StringUtil;

class IndexUtilTest extends RuleTestBase {

	int[] _toIndx(String idxStr) {

		List<String> idxStrs = StringUtil.splitStringByChar(idxStr, ',');

		int[] idxs = new int[idxStrs.size()];
		for (int i = 0; i < idxStrs.size(); ++i) {
			idxs[i] = Integer.valueOf(idxStrs.get(i));
		}

		return idxs;

	}

	void _test_match_indexs(String idxStr1, String idxStr2, boolean expectMatch) {

		int[] idx1 = _toIndx(idxStr1);
		int[] idx2 = _toIndx(idxStr2);

		try {

			boolean match = IndexUtil.matchIndexs(idx1, idx2);
			assertEquals(expectMatch, match);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	void _test_has_part_indexs(String idxStr1, String idxStr2, boolean expectMatch) {

		int[] idx1 = _toIndx(idxStr1);
		int[] idx2 = _toIndx(idxStr2);

		try {

			boolean match = IndexUtil.hasPartIndexs(idx1, idx2);
			assertEquals(expectMatch, match);
		} catch (RException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	void test_has_part_indexs_1() {

		_setup();

		_test_has_part_indexs("", "", false);
		_test_has_part_indexs("1", "1", true);
		_test_has_part_indexs("0,1", "1", true);
		_test_has_part_indexs("0,1", "0,1", true);
		_test_has_part_indexs("0", "0,1", false);
		_test_has_part_indexs("1,2,3", "1,3", true);
		_test_has_part_indexs("0,2", "2,3", false);
	}

	@Test
	void test_match_indexs_1() {

		_setup();

		_test_match_indexs("", "", true);
		_test_match_indexs("1", "1", true);
		_test_match_indexs("1", "1,1", false);
		_test_match_indexs("1,2,3", "1,2,3", true);
		_test_match_indexs("1,2,3", "-1,2,3", true);
		_test_match_indexs("1,2,3", "-1,-1,3", true);
		_test_match_indexs("1,2,3", "-1,1,3", true);
		_test_match_indexs("1,2,3", "-1,1,2", true);
		_test_match_indexs("1,2,3", "-1,-1,1", true);

	}
}
