package alpha.rulp.ximpl.action;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRContext;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IAction {

	public void doAction(IRReteEntry entry, IRContext context) throws RException;

	public RActionType getActionType();

}
