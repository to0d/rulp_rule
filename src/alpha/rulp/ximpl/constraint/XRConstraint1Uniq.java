package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Uniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Uniq extends AbsRConstraint1 implements IRConstraint1, IRRListener1<IRReteEntry> {

	private String _constraintExpression = null;

	private final int[] uniqColumnIndexs;

	private final IRObject[] uniqEntry;

	private Map<String, IRReteEntry> uniqEntryMap = null;

	public XRConstraint1Uniq(int[] uniqColumnIndexs) {
		super();
		this.uniqColumnIndexs = uniqColumnIndexs;
		this.uniqEntry = new IRObject[uniqColumnIndexs.length];
	}

	protected String _getUniqString(IRReteEntry entry) throws RException {

		for (int i = 0; i < uniqEntry.length; ++i) {
			uniqEntry[i] = entry.get(uniqColumnIndexs[i]);
		}

		return ReteUtil.uniqName(uniqEntry);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		String uniqName = _getUniqString(entry);

		if (uniqEntryMap == null) {
			uniqEntryMap = new HashMap<>();
		} else if (uniqEntryMap.containsKey(uniqName)) {
			return false;
		}

		uniqEntryMap.put(uniqName, entry);
		entry.addEntryRemovedListener(this);

		return true;
	}

	@Override
	public void close() {

		if (uniqEntryMap == null) {
			return;
		}

		for (IRReteEntry entry : uniqEntryMap.values()) {
			entry.removeEntryRemovedListener(this);
		}

		uniqEntryMap = null;
	}

	@Override
	public void doAction(IRReteEntry obj) throws RException {

		if (uniqEntryMap == null) {
			return;
		}

		uniqEntryMap.remove(_getUniqString(obj));
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {

			if (uniqColumnIndexs.length == 1) {
				_constraintExpression = String.format("'(%s on ?%d)", getConstraintName(), uniqColumnIndexs[0]);

			} else {

				ArrayList<IRObject> indexs = new ArrayList<>();
				for (int i : uniqColumnIndexs) {
					indexs.add(RulpFactory.createAtom("?" + i));
				}

				_constraintExpression = String.format("'(%s on %s)", getConstraintName(),
						RulpFactory.createList(indexs));
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

	public int[] getUniqColumnIndexs() {
		return uniqColumnIndexs;
	}

	public int getUniqIndexCount() {
		return uniqColumnIndexs.length;
	}
}
