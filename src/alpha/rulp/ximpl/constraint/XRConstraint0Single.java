package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.*;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IREntryTable;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint0Single extends AbsRConstraint0 implements IRConstraint0 {

	private String _constraintExpression = null;

	private IRReteEntry lastEntry;

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s)", getConstraintName());
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_SINGLE;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		if (entry == null || entry.isDroped()) {
			return false;
		}

		if (lastEntry != null && !lastEntry.isDroped()) {
			context.getModel().getEntryTable().removeEntry(lastEntry);
		}

		lastEntry = entry;
		return false;

	}
}
