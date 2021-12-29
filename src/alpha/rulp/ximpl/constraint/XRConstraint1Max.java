package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_Max;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.MathUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Max extends AbsRConstraint1Index1 implements IRConstraint1OneValue {

	private String _constraintExpression = null;

	private final IRObject maxValue;

	public XRConstraint1Max(int columnIndex, IRObject maxValue) {
		super(columnIndex);
		this.maxValue = maxValue;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		return MathUtil.computeRelationalExpression(RRelationalOperator.LE, entry.get(index), maxValue);
	}

	@Override
	public int getColumnIndex() {
		return index;
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s on ?%d)", getConstraintName(), maxValue, index);
		}

		return _constraintExpression;
	}

	@Override
	public String getConstraintName() {
		return A_Max;
	}

	@Override
	public IRObject getValue() {
		return maxValue;
	}

}
