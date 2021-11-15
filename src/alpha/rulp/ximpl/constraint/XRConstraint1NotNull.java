package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_NOT_NULL;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RType;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1NotNull extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	public XRConstraint1NotNull(int index) {
		super(index);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		IRObject obj = entry.get(index);
		return obj != null && obj.getType() != RType.NIL;
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("'(%s on ?%d)", getConstraintName(), index);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_NOT_NULL;
	}
}
