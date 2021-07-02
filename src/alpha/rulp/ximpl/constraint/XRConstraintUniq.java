package alpha.rulp.ximpl.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRRListener1;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.ReteUtil;
import alpha.rulp.utils.RulpFactory;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraintUniq extends AbsRConstraint1 implements IRConstraint1, IRRListener1<IRReteEntry> {

	private String _constraintExpression = null;

	private final int[] uniqColumnIndexs;

	private final IRObject[] uniqEntry;

	private Map<String, IRReteEntry> uniqEntryMap = null;

	public XRConstraintUniq(int[] uniqColumnIndexs) {
		
		super();

		int size = uniqColumnIndexs.length;

		ArrayList<Integer> indexs = new ArrayList<>();
		for (int index : uniqColumnIndexs) {
			indexs.add(index);
		}
		Collections.sort(indexs);

		this.uniqColumnIndexs = new int[size];
		for (int i = 0; i < size; ++i) {
			this.uniqColumnIndexs[i] = indexs.get(i);
		}

		this.uniqEntry = new IRObject[size];
	}

	protected String _getUniqString(IRReteEntry entry) throws RException {

		for (int i = 0; i < uniqEntry.length; ++i) {
			uniqEntry[i] = entry.get(uniqColumnIndexs[i]);
		}

		return ReteUtil.uniqName(uniqEntry);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {
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
			ArrayList<IRObject> indexs = new ArrayList<>();
			for (int i : uniqColumnIndexs) {
				indexs.add(RulpFactory.createInteger(i));
			}
			_constraintExpression = String.format("'(uniq on %s)", RulpFactory.createList(indexs));
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return uniqColumnIndexs;
	}

	@Override
	public RConstraintType getConstraintType() {
		return RConstraintType.UNIQ;
	}

	public int[] getUniqColumnIndexs() {
		return uniqColumnIndexs;
	}

	public int getUniqIndexCount() {
		return uniqColumnIndexs.length;
	}
}
