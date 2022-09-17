package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_EXPRESSION;

import alpha.rulp.lang.IRExpr;

public abstract class AbsRConstraintXExpr extends AbsRConstraintX implements IRConstraintXExpr {

	protected IRExpr expr;

	public AbsRConstraintXExpr(int entryLength, IRExpr expr) {
		super(entryLength);
		this.expr = expr;
	}

	@Override
	public String getConstraintExpression() {
		return "" + expr;
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}

	@Override
	public String getConstraintName() {
		return A_EXPRESSION;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}
}
