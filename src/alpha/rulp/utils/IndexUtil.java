package alpha.rulp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.node.RUniqInfo;

public class IndexUtil {

	public static int[] clone(int[] indexs) {

		int[] newIndexs = new int[indexs.length];

		for (int i = 0; i < indexs.length; ++i) {
			newIndexs[i] = indexs[i];
		}

		return newIndexs;
	}

	public static List<RUniqInfo> clone(List<RUniqInfo> infoList) {

		List<RUniqInfo> newInfoList = new ArrayList<>();
		for (RUniqInfo info : infoList) {
			newInfoList.add(clone(info));
		}

		return newInfoList;
	}

	public static RUniqInfo clone(RUniqInfo info) {

		RUniqInfo newInfo = new RUniqInfo();
		newInfo.uniqIndexs = clone(info.uniqIndexs);

		return newInfo;
	}

	public static int compareIndexs(int[] indexs1, int[] indexs2) {

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

	public static int[] makeFullIndexs(int len) {

		int[] indexs = new int[len];
		for (int i = 0; i < len; ++i) {
			indexs[i] = i;
		}

		return indexs;
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

	public static void unify(List<RUniqInfo> list) {

		if (list.size() < 2) {
			return;
		}

		Collections.sort(list, (u1, u2) -> {
			return IndexUtil.compareIndexs(u1.uniqIndexs, u2.uniqIndexs);
		});

		int size = list.size();
		RUniqInfo last = list.get(0);
		int pos = 1;

		for (int i = 1; i < size; ++i) {

			RUniqInfo info = list.get(i);

			// Duplicated uniq indexs
			if (IndexUtil.compareIndexs(last.uniqIndexs, info.uniqIndexs) == 0) {
				continue;
			}

			// does not move
			if (pos == i) {
				++pos;
			} else {
				list.set(pos++, info);
			}

			last = info;
		}

		while (list.size() > pos) {
			list.remove(pos);
		}

	}

	public static RUniqInfo unify(RUniqInfo info) {

		RUniqInfo newInfo = new RUniqInfo();

		int uniqCount = 0;
		for (int i = 0; i < info.uniqIndexs.length; ++i) {
			if (info.uniqIndexs[i] != -1) {
				uniqCount++;
			}
		}

		newInfo.uniqIndexs = new int[uniqCount];
		int pos = 0;

		for (int i = 0; i < info.uniqIndexs.length; ++i) {
			if (info.uniqIndexs[i] != -1) {
				newInfo.uniqIndexs[pos++] = info.uniqIndexs[i];
			}
		}

		return newInfo;
	}
}
