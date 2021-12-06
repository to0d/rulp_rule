package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_SINGLE;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Single extends AbsRConstraint1 implements IRConstraint0 {

	private String _constraintExpression = null;

	private IRReteEntry lastEntry;

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		if (entry == null || entry.isDroped()) {
			return false;
		}

		if (lastEntry != null && !lastEntry.isDroped()) {
			context.getModel().getEntryTable().removeEntry(lastEntry);
		}

		lastEntry = entry;
		return true;

	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s)", getConstraintName());
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_SINGLE;
	}
}
