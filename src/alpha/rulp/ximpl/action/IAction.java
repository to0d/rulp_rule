package alpha.rulp.ximpl.action;

import java.util.List;

import alpha.rulp.lang.IRExpr;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IAction {

	public void doAction(IRReteEntry entry, IRContext context) throws RException;

	public RActionType getActionType();

	public IRExpr getExpr();

	public int getIndex();

	public List<IRExpr> getStmtExprList() throws RException;

	public void setIndex(int index);
}
