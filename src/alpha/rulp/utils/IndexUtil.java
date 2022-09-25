package alpha.rulp.utils;

import alpha.rulp.lang.RException;

public class IndexUtil {
	
	public static int compare(int[] indexs1, int[] indexs2) {

		int d = indexs1.length - indexs2.length;
		if (d != 0) {
			return d;
		}

		for (int i = 0; i < indexs1.length; ++i) {
			d = indexs1[i] - indexs2[i];
			if (d != 0) {
				return d;
			}
		}

		return 0;
	}

	public static String formatIndexs(int[] indexs) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < indexs.length; ++i) {

			if (i != 0) {
				sb.append(' ');
			}

			sb.append('?');
			sb.append(indexs[i]);
		}

		return sb.toString();
	}

	public static boolean hasPartIndexs(int[] mainIndexs, int[] partIndexs) throws RException {

		if (partIndexs.length == 0 || mainIndexs.length < partIndexs.length) {
			return false;
		}

		// All parent indexs should be in asc order
		{
			int lastIdx = -2;
			for (int idx : mainIndexs) {
				if (idx < 0 || idx <= lastIdx) {
					throw new RException("invalid parent index: " + idx);
				}
				lastIdx = idx;
			}
		}

		// All sub indexs should be in asc order
		{
			int lastIdx = -2;
			for (int idx : partIndexs) {
				if (idx < 0 || idx <= lastIdx) {
					throw new RException("invalid sub index: " + idx);
				}
				lastIdx = idx;
			}
		}

		int pos1 = 0;
		int pos2 = 0;

		while (pos1 < mainIndexs.length && pos2 < partIndexs.length) {

			int v1 = mainIndexs[pos1];
			int v2 = partIndexs[pos2];

			if (v1 == v2) {
				pos1++;
				pos2++;
			} else if (v1 < v2) {
				pos1++;
			} else {
				return false;
			}
		}

		return pos2 == partIndexs.length;
	}

	public static boolean matchIndexs(int[] idx1, int[] idx2) throws RException {

		if (idx1.length != idx2.length) {
			return false;
		}

		if (idx1.length == 0) {
			return true;
		}

		// All index1 should be actual index number
		{
			int lastIdx = -2;
			for (int idx : idx1) {
				if (idx < 0 || idx == lastIdx) {
					throw new RException("invalid index1: " + idx);
				}
				lastIdx = idx;
			}
		}

		// All index2 should be in order
		int anyIdx2Count = 0;
		{
			int lastIdx = -2;
			for (int idx : idx2) {

				if (idx < lastIdx || (idx >= 0 && idx == lastIdx)) {
					throw new RException("invalid index2: " + idx);
				}

				if (idx == -1) {
					anyIdx2Count++;
				}

				lastIdx = idx;
			}
		}

		final int len = idx1.length;

		// match index one by one
		if (anyIdx2Count == 0) {

			for (int i = 0; i < len; ++i) {
				if (idx1[i] != idx2[i]) {
					return false;
				}
			}

			return true;
		}

		int i = 0;
		NEXT: for (int j = anyIdx2Count; j < len; ++j) {
			int idx = idx2[j];
			while (i < len) {
				if (idx1[i++] == idx) {
					continue NEXT;
				}
			}
			return false;
		}

		return true;
	}
}
