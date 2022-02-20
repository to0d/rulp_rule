package alpha.rulp.ximpl.cache;

import java.io.IOException;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;
import alpha.rulp.runtime.IRListener1;

public interface IRStmtLoader {

	public void load(IRReteNode node, IRListener1<IRList> stmtListener) throws RException, IOException;

	public int getReadLines();

}
