package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Expr3 extends AbsRConstraint1 implements IRConstraint1Expr {

	protected IRExpr expr;

	public XRConstraint1Expr3(IRExpr expr) {
		super();
		this.expr = expr;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {
		IRObject rst = interpreter.compute(frame, expr);
		return RulpUtil.asBoolean(rst).asBoolean();
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
	public RConstraintType getConstraintType() {
		return RConstraintType.EXPR;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}
}
