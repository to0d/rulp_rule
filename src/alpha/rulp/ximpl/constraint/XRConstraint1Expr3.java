package alpha.rulp.ximpl.constraint;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.IRObject;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.utils.RulpUtil;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XRConstraint1Expr3 extends AbsRConstraint1Expr implements IRConstraint1Expr {

	public XRConstraint1Expr3(IRExpr expr) {
		super(expr);
	}

	@Override
	public boolean addEntry(IRReteEntry entry, IRContext context) throws RException {
		IRObject rst = context.getInterpreter().compute(context.getFrame(), expr);
		return RulpUtil.asBoolean(rst).asBoolean();
	}

}
