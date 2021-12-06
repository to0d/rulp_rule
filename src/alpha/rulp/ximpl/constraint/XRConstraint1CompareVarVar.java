package alpha.rulp.ximpl.constraint;

import static alpha.rulp.rule.Constant.A_CMP_VAR_VAR;

import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.lang.RRelationalOperator;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1CompareVarVar extends AbsRConstraint1 implements IRConstraint1 {

	private String _constraintExpression = null;

	private RRelationalOperator op;

	private IRVar var1;

	private IRVar var2;

	public XRConstraint1CompareVarVar(RRelationalOperator op, IRVar var1, IRVar var2) {
		this.op = op;
		this.var1 = var1;
		this.var2 = var2;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {
		return RulpUtil.computeRelationalExpression(op, var1.getValue(), var2.getValue());
	}

	@Override
	public String getConstraintExpression() {

		if (_constraintExpression == null) {
			_constraintExpression = String.format("(%s %s %s %s)", getConstraintName(), op.getAtom(),
					"" + var1.getName(), "" + var2.getName());
		}

		return _constraintExpression;
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_CMP_VAR_VAR;
	}
}
