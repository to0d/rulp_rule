package alpha.rulp.ximpl.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRNodeInher extends XRNodeRete1 {

	protected int[] inheritIndexs;

	protected Map<String, IRReteEntry> uniqEntryMap = new HashMap<>();

	public XRNodeInher(String instanceName) {
		super(instanceName);
	}

	@Override
	protected List<RUniqInfo> _rebuildParentUniqInfoList() throws RException {

		List<RUniqInfo> parentUniqList = new ArrayList<>();

		for (RUniqInfo info : parentNodes[0].listUniqInfos()) {

			ArrayList<Integer> uniqIndexs = new ArrayList<>();
			int pos1 = 0;
			int pos2 = 0;

			while (pos1 < info.uniqIndexs.length && pos1 < inheritIndexs.length) {

				int index1 = info.uniqIndexs[pos1];
				int index2 = inheritIndexs[pos2];

				if (index1 == index2) {
					uniqIndexs.add(pos2);
					++pos1;
					++pos2;
				} else if (index1 < index2) {
					++pos1;
				} else {
					++pos2;
				}
			}

			if (uniqIndexs.isEmpty()) {
				throw new RException("no uniq index found");
			}

			RUniqInfo newInfo = new RUniqInfo();
			newInfo.uniqIndexs = new int[uniqIndexs.size()];

			for (int i = 0; i < uniqIndexs.size(); ++i) {
				newInfo.uniqIndexs[i] = uniqIndexs.get(i);
			}
			
			parentUniqList.add(newInfo);
		}

//		int size = parentUniqList.size();
//
//		for (int pos = 0; pos < size; ++pos) {
//
//			RUniqInfo info = parentUniqList.get(pos);
//
//			boolean update = false;
//
//			for (int i = 0; i < info.uniqIndexs.length; ++i) {
//				int index = info.uniqIndexs[i];
//				// fixed element
//				if (index != -1 && this.getVarEntry()[index] == null) {
//					info.uniqIndexs[i] = -1;
//					update = true;
//				}
//			}
//
//			if (update) {
//				info = IndexUtil.unify(info);
//				parentUniqList.set(pos, info);
//			}
//		}

		return parentUniqList;
	}

	@Override
	public boolean addReteEntry(IRReteEntry entry) throws RException {

		int entryLength = inheritIndexs.length;

		IRObject[] newElements = new IRObject[entryLength];
		for (int i = 0; i < entryLength; ++i) {
			newElements[i] = entry.get(inheritIndexs[i]);
		}

		String uniqName = "'(" + ReteUtil.uniqName(newElements) + ")";

		// Check old entry
		{
			IRReteEntry oldEntry = uniqEntryMap.get(uniqName);

			// old entry exist
			if (!ReteUtil.isRemovedEntry(oldEntry)) {
				entryTable.addReference(oldEntry, this, entry);
				entryQueue.incNodeUpdateCount();
				entryQueue.incEntryRedundant();
				return false;
			}
		}

		IRReteEntry newEntry = entryTable.createEntry(null, newElements, entry.getStatus(), false);
		incEntryCreateCount();

		if (!super.addReteEntry(newEntry)) {
			entryTable.deleteEntry(newEntry);
			incEntryDeleteCount();
			return false;
		}

		entryTable.addReference(newEntry, this, entry);
		uniqEntryMap.put(uniqName, newEntry);
		return true;
	}

	public void setInheritIndexs(int[] inheritIndexs) {
		this.inheritIndexs = inheritIndexs;
	}
}
