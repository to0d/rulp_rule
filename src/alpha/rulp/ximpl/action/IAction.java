package alpha.rulp.ximpl.action;

import alpha.rulp.lang.RException;
import alpha.rulp.ximpl.entry.IRReteEntry;
import alpha.rulp.ximpl.node.IRReteNode;

public interface IAction {

	public void doAction(IRReteNode node, IRReteEntry entry) throws RException;

}
