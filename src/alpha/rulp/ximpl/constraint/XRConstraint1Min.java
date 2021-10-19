package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Min;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Min extends AbsRConstraint1Index1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private final IRObject maxValue;

	public XRConstraint1Min(int columnIndex, IRObject maxValue) {
		super(columnIndex);
		this.maxValue = maxValue;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		return RulpUtil.computeRelationalExpression(RRelationalOperator.GE, entry.get(index), maxValue);
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("'(%s on %d)", A_Min, index);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_Min;
	}

}
