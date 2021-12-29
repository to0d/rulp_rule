package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_CMP_VAR_VALUE;

import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.MathUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1CompareVarValue extends AbsRConstraint1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private RRelationalOperator op;

	private IRObject val;

	private IRVar var;

	public XRConstraint1CompareVarValue(RRelationalOperator op, IRVar var, IRObject val) {
		this.op = op;
		this.var = var;
		this.val = val;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		return MathUtil.computeRelationalExpression(op, var.getValue(), val);
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s %s %s)", getConstraintName(), op.getAtom(),
					"" + var.getName(), "" + val);
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_CMP_VAR_VALUE;
	}
}
