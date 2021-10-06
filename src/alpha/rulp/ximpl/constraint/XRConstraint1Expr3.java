package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_EXPRESSION;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
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
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		IRObject rst = context.getInterpreter().compute(context.getFrame(), expr);
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
	public String getConstraintName() {
		return A_EXPRESSION;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}
}
