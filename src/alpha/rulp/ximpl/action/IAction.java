package alpha.rulp.ximpl.action;

import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.ximpl.entry.IRReteEntry;

public interface IAction {

	public void doAction(IRReteNode node, IRReteEntry entry) throws RException;

}
