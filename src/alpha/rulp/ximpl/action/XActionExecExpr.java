package alpha.rulp.ximpl.action;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XActionExecExpr implements IAction {

	private IRExpr expr;

	private int index = -1;

	public XActionExecExpr(IRExpr expr) {
		super();
		this.expr = expr;
	}

	@Override
	public void doAction(IRReteEntry entry, IRContext context) throws RException {
		context.getInterpreter().compute(context.getFrame(), expr);
	}

	@Override
	public RActionType getActionType() {
		return RActionType.EXPR;
	}

	@Override
	public IRExpr getExpr() {
		return expr;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return expr.toString();
	}
}
