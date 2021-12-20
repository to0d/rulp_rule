package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRFrame;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.IRVar;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Expr0 extends AbsRConstraint1Expr implements IRConstraint1Expr {

	protected IRVar[] _vars = null;

	protected IRObject[] varEntry;

	public XRConstraint1Expr0(IRExpr expr, IRObject[] varEntry) {

		super(expr);
		this.varEntry = varEntry;
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
