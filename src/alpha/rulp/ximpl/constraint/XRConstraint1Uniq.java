package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Uniq extends AbsRConstraint1 implements IRConstraint1Uniq {

	private String _constraintExpression = null;

	private final int[] uniqColumnIndexs;

	private final IRObject[] uniqEntry;

	private Map<String, IRReteEntry> uniqEntryMap = null;

	public XRConstraint1Uniq(int[] uniqColumnIndexs) {
		super();
		this.uniqColumnIndexs = uniqColumnIndexs;
		this.uniqEntry = new IRObject[uniqColumnIndexs.length];
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		String uniqName = getUniqString(entry);

		if (getReteEntry(uniqName) != null) {
			return false;
		}

		if (uniqEntryMap == null) {
			uniqEntryMap = new HashMap<>();
		}
		uniqEntryMap.put(uniqName, entry);
		return true;
	}

	@Override
	public void close() {
		uniqEntryMap = null;
	}

	public String getCacheInfo() {

		if (uniqEntryMap == null || uniqEntryMap.isEmpty()) {
			return super.getCacheInfo();
		}

		return ReteUtil.combine(super.getCacheInfo(), "uniqEntryMap: size=" + uniqEntryMap.size());
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {

			if (uniqColumnIndexs.length == 1) {
				_constraintExpression = String.format("(%s on ?%d)", getConstraintName(), uniqColumnIndexs[0]);

			} else {

				ArrayList<IRObject> indexs = new ArrayList<>();
				for (int i : uniqColumnIndexs) {
					indexs.add(RulpFactory.createAtom("?" + i));
				}

				try {
					_constraintExpression = String.format("(%s on %s)", getConstraintName(),
							RulpFactory.createNativeList(indexs));
				} catch (RException e) {
					e.printStackTrace();
				}
			}

		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return uniqColumnIndexs;
	}

	@Override
	public String getConstraintName() {
		return A_Uniq;
	}

	@Override
	public IRReteEntry getReteEntry(String uniqName) throws RException {

		if (uniqEntryMap != null) {
			IRReteEntry oldEntry = uniqEntryMap.get(uniqName);
			if (oldEntry != null && !oldEntry.isDroped()) {
				return oldEntry;
			}
		}

		return null;
	}

	public int[] getUniqColumnIndexs() {
		return uniqColumnIndexs;
	}

	public int getUniqIndexCount() {
		return uniqColumnIndexs.length;
	}

	@Override
	public String getUniqString(IRList stmt) throws RException {

		for (int i = 0; i < uniqEntry.length; ++i) {
			uniqEntry[i] = stmt.get(uniqColumnIndexs[i]);
		}

		return ReteUtil.uniqName(uniqEntry);
	}
}
