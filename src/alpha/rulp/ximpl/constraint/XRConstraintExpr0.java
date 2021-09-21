package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.runtime.IRInterpreter;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraintExpr0 extends AbsRConstraint1 implements IRConstraintExpr {

	protected IRObject[] varEntry;

	protected IRVar[] _vars = null;

	protected IRExpr expr;

	public XRConstraintExpr0(IRExpr expr, IRObject[] varEntry) {

		super();
		this.expr = expr;
		this.varEntry = varEntry;
	}

	public IRVar[] getVars(IRFrame frame) throws RException {

		if (_vars == null) {

			_vars = new IRVar[varEntry.length];

			for (int i = 0; i < this.varEntry.length; ++i) {
				IRObject obj = varEntry[i];
				if (obj != null) {
					_vars[i] = frame.addVar(RulpUtil.asAtom(obj).getName());
				}
			}
		}

		return _vars;
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRInterpreter interpreter, IRFrame frame) throws RException {

		IRVar[] _vars = getVars(frame);

		/******************************************************/
		// Update variable value
		/******************************************************/
		for (int i = 0; i < entry.size(); ++i) {
			IRVar var = _vars[i];
			if (var != null) {
				var.setValue(entry.get(i));
			}
		}

		IRObject rst = interpreter.compute(frame, expr);

		return RulpUtil.asBoolean(rst).asBoolean();
	}

	@Override
	public String getConstraintExpression() {
		return "" + expr;
	}

	@Override
	public RConstraintType getConstraintType() {
		return RConstraintType.EXPR;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}

	@Override
	public int[] getConstraintIndex() {
		return null;
	}
}
