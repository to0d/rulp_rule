package beta.test.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import alpha.rulp.lang.RException;
import alpha.rulp.utils.IndexUtil;
import alpha.rulp.utils.RuleTestBase;
import alpha.rulp.utils.StringUtil;

class IndexUtilTest extends RuleTestBase {

	String _compare_indexs(String idxStr) throws RException {

		List<int[]> idxStrs = _toIndxList(idxStr);
		int[] idx1 = idxStrs.get(0);
		int[] idx2 = idxStrs.size() > 1 ? idxStrs.get(1) : new int[0];

		return "" + IndexUtil.compareIndexs(idx1, idx2);
	}

	String _format_indexs(String inputList) throws RException, IOException {

		int[] idx1 = _toIndx(inputList);
		return IndexUtil.formatIndexs(idx1);
	}

	String _has_part_indexs(String idxStr) throws RException {

		List<int[]> idxStrs = _toIndxList(idxStr);
		int[] idx1 = idxStrs.get(0);
		int[] idx2 = idxStrs.size() > 1 ? idxStrs.get(1) : new int[0];

		return "" + IndexUtil.hasPartIndexs(idx1, idx2);
	}

	String _match_indexs(String idxStr) throws RException {

		List<int[]> idxStrs = _toIndxList(idxStr);
		int[] idx1 = idxStrs.get(0);
		int[] idx2 = idxStrs.size() > 1 ? idxStrs.get(1) : new int[0];

		return "" + IndexUtil.matchIndexs(idx1, idx2);
	}

	int[] _toIndx(String idxStr) {

		if (idxStr.trim().isEmpty()) {
			return new int[0];
		}

		List<String> idxStrs = StringUtil.splitStringByChar(idxStr, ',');

		int[] idxs = new int[idxStrs.size()];
		for (int i = 0; i < idxStrs.size(); ++i) {
			idxs[i] = Integer.valueOf(idxStrs.get(i));
		}

		return idxs;

	}

	List<int[]> _toIndxList(String idxStr) {

		List<int[]> idxStrs = new ArrayList<>();
		for (String str : StringUtil.splitStringByChar(idxStr, ';')) {
			idxStrs.add(_toIndx(str));
		}

		return idxStrs;
	}

	@Test
	void test_compare_indexs_1() {

		_setup();

		_test((input) -> {
			return _compare_indexs(input);
		});

	}

	@Test
	void test_format_indexs() {

		_setup();

		_test((input) -> {
			return _format_indexs(input);
		});

	}

	@Test
	void test_has_part_indexs_1() {

		_setup();

		_test((input) -> {
			return _has_part_indexs(input);
		});

	}
	
	@Test
	void test_match_indexs_1() {

		_setup();

		_test((input) -> {
			return _match_indexs(input);
		});

	}
}
