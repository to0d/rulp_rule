package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_EXPRESSION;

import alpha.rulp.lang.IRExpr;

public abstract class AbsRConstraint1Expr extends AbsRConstraint1 implements IRConstraint1Expr {

	protected IRExpr expr;

	public AbsRConstraint1Expr(IRExpr expr) {
		super();
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
