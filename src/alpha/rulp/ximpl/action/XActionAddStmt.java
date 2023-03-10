package alpha.rulp.ximpl.action;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public class XActionAddStmt extends AbsActionSimpleStmt implements IAction {

	@Override
	protected void _doAction(IRReteEntry entry, IRContext context, IRList stmt) throws RException {
		context.getModel().addStatement(stmt);
	}

	@Override
	public RActionType getActionType() {
		return RActionType.ADD;
	}

}