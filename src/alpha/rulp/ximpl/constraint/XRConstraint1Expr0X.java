package alpha.rulp.ximpl.constraint;

import static alpha.rulp.lang.Constant.A_EXPRESSION;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Expr0X extends AbsRConstraint1 implements IRConstraint1Expr {

	protected IRVar[] _vars = null;

	protected int[] constraintIndex;

	protected IRExpr expr;

	protected int externalVarCount;

	protected IRObject[] varEntry;

	public XRConstraint1Expr0X(IRExpr expr, IRObject[] varEntry, int[] constraintIndex, int externalVarCount)
			throws RException {

		this.expr = expr;
		this.varEntry = varEntry;
		this.constraintIndex = constraintIndex;
		this.externalVarCount = externalVarCount;
	}

	@Override
	protected boolean _addEntry(IRReteEntry entry, IRContext context) throws RException {

		IRVar[] _vars = getVars(context.getFrame());

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int i = 0; i < entry.size(); ++i) {
			IRVar var = _vars[i];
			if (var != null) {
				var.setValue(entry.get(i));
			}
		}

		IRObject rst = context.getInterpreter().compute(context.getFrame(), expr);

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	@Override
	public String getConstraintExpression() {
		return "" + expr;
	}

	@Override
	public int[] getConstraintIndex() {
		return constraintIndex;
	}

	@Override
	public String getConstraintName() {
		return A_EXPRESSION;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}

	public int getExternalVarCount() {
		return externalVarCount;
	}

	public IRVar[] getVars(IRFrame frame) throws RException {

		if (_vars == null) {

			_vars = new IRVar[varEntry.length];

			for (int i = 0; i < this.varEntry.length; ++i) {
				IRObject obj = varEntry[i];
				if (obj != null) {
					_vars[i] = RulpUtil.addVar(frame, RulpUtil.asAtom(obj).getName());
				}

			}
		}

		return _vars;
	}
}
