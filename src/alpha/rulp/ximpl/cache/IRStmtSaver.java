package alpha.rulp.ximpl.cache;

import java.io.IOException;
import java.util.List;

import alpha.rulp.lang.IRList;
import alpha.rulp.lang.RException;
import alpha.rulp.rule.IRReteNode;

public interface IRStmtSaver {

	public int save(IRReteNode node, List<IRList> stmtList) throws RException, IOException;

	public boolean needSave();
}